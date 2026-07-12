#!/usr/bin/env python3
"""
SSH helper for pg116/pg117 via helios jump host (variant 70219).

Usage:
  scripts/pg117 run <remote command>        # run command on pg117 (default)
  scripts/pg116 run <remote command>        # run command on pg116 (primary)
  scripts/pgNNN ssh                       # interactive shell
  scripts/pgNNN proxy start|stop|status   # SSH ControlMaster (reuse session)
  scripts/pgNNN forward start|stop|status # local TCP proxy
  scripts/pgNNN scp <local> pgNNN:<path>
  scripts/pgNNN scp pgNNN:<path> <local>

Select node via env: PG117_NODE=pg116 scripts/pg117 run 'hostname'
"""

from __future__ import annotations

import argparse
import os
import signal
import socket
import stat
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path


def _load_env() -> dict[str, str]:
    env_file = Path(__file__).parent.parent / ".env"
    result: dict[str, str] = {}
    if env_file.is_file():
        for line in env_file.read_text().splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            result[k.strip()] = v.strip()
    return result


_ENV = _load_env()


def _e(key: str) -> str:
    val = _ENV.get(key) or os.environ.get(key, "")
    if not val:
        sys.exit(f"pg117: missing {key} in .env")
    return val


JUMP = f"{_e('HELIOS_USER')}@{_e('HELIOS_HOST')}:{_e('HELIOS_PORT')}"
JUMP_PASSWORD = _e("HELIOS_PASSWORD")

NODE_PRESETS: dict[str, tuple[str, str]] = {
    "pg117": (f"{_e('PG117_USER')}@{_e('PG117_HOST')}", _e("PG117_PASSWORD")),
    "pg116": (f"{_e('PG116_USER')}@{_e('PG116_HOST')}", _e("PG116_PASSWORD")),
}

DEFAULT_NODE = "pg117"

DEFAULT_CONTROL_DIR = Path(f"/tmp/rshd-lab2-{os.environ.get('USER', 'user')}")
DEFAULT_LOCAL_PORT = 17019
CONTROL_PERSIST = "8h"

ASKPASS_SCRIPT = """\
#!/usr/bin/env python3
import os
from pathlib import Path
counter = Path(os.environ["PG117_ASKPASS_COUNTER"])
n = int(counter.read_text()) if counter.is_file() else 0
counter.write_text(str(n + 1))
passwords = [os.environ["PG117_JUMP_PASS"], os.environ["PG117_TARGET_PASS"]]
print(passwords[min(n, 1)])
"""


@dataclass
class Config:
    jump: str
    remote: str
    jump_password: str
    target_password: str
    control_dir: Path
    control_socket: Path
    local_port: int

    def ssh_base(self) -> list[str]:
        return [
            "ssh",
            "-o",
            "StrictHostKeyChecking=accept-new",
            "-o",
            "PreferredAuthentications=keyboard-interactive,password",
            "-o",
            "IdentitiesOnly=yes",
            "-o",
            "PubkeyAuthentication=no",
            "-J",
            self.jump,
        ]


def get_config() -> Config:
    node = os.environ.get("PG117_NODE", DEFAULT_NODE)
    if node not in NODE_PRESETS:
        sys.exit(f"pg117: unknown node '{node}'. Known nodes: {', '.join(NODE_PRESETS)}")
    remote, target_password = NODE_PRESETS[node]
    control_dir = Path(os.environ.get("PG117_CONTROL_DIR", str(DEFAULT_CONTROL_DIR / node)))
    local_port = int(os.environ.get("PG117_LOCAL_PORT", str(DEFAULT_LOCAL_PORT)))
    return Config(
        jump=JUMP,
        remote=remote,
        jump_password=JUMP_PASSWORD,
        target_password=target_password,
        control_dir=control_dir,
        control_socket=control_dir / f"{node}.sock",
        local_port=local_port,
    )


def ssh_env(cfg: Config) -> dict[str, str]:
    askpass_dir = cfg.control_dir / "askpass"
    askpass_dir.mkdir(parents=True, exist_ok=True)
    counter = askpass_dir / "counter"
    counter.write_text("0")

    script = askpass_dir / "askpass.py"
    script.write_text(ASKPASS_SCRIPT)
    script.chmod(script.stat().st_mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

    env = os.environ.copy()
    env["PG117_JUMP_PASS"] = cfg.jump_password
    env["PG117_TARGET_PASS"] = cfg.target_password
    env["PG117_ASKPASS_COUNTER"] = str(counter)
    env["SSH_ASKPASS"] = str(script)
    env["SSH_ASKPASS_REQUIRE"] = "force"
    env.setdefault("DISPLAY", ":0")
    return env


def run_ssh(cfg: Config, argv: list[str], timeout: int | None = None) -> int:
    try:
        r = subprocess.run(
            argv,
            env=ssh_env(cfg),
            stdin=subprocess.DEVNULL,
            timeout=timeout,
            check=False,
        )
        return r.returncode
    except subprocess.TimeoutExpired:
        return 124


def control_active(cfg: Config) -> bool:
    if not cfg.control_socket.is_socket():
        return False
    r = subprocess.run(
        ["ssh", "-S", str(cfg.control_socket), "-O", "check", cfg.remote],
        capture_output=True,
        text=True,
    )
    return r.returncode == 0


def cmd_run(cfg: Config, remote_cmd: str) -> int:
    if control_active(cfg):
        return subprocess.run(
            [
                "ssh",
                "-S",
                str(cfg.control_socket),
                "-o",
                "StrictHostKeyChecking=accept-new",
                cfg.remote,
                remote_cmd,
            ],
            check=False,
        ).returncode

    return run_ssh(cfg, [*cfg.ssh_base(), cfg.remote, remote_cmd])


def cmd_ssh(cfg: Config) -> int:
    if control_active(cfg):
        os.execvp(
            "ssh",
            [
                "ssh",
                "-t",
                "-S",
                str(cfg.control_socket),
                "-o",
                "StrictHostKeyChecking=accept-new",
                cfg.remote,
            ],
        )
    os.environ.update(ssh_env(cfg))
    os.execvp("ssh", [*cfg.ssh_base(), "-t", cfg.remote])


def cmd_proxy_start(cfg: Config) -> int:
    cfg.control_dir.mkdir(parents=True, exist_ok=True)
    if control_active(cfg):
        print(f"pg117: control master already active ({cfg.control_socket})")
        return 0

    code = run_ssh(
        cfg,
        [
            "ssh",
            "-fNM",
            "-o",
            f"ControlPath={cfg.control_socket}",
            "-o",
            f"ControlPersist={CONTROL_PERSIST}",
            "-o",
            "StrictHostKeyChecking=accept-new",
            "-o",
            "PreferredAuthentications=keyboard-interactive,password",
            "-o",
            "IdentitiesOnly=yes",
            "-o",
            "PubkeyAuthentication=no",
            "-J",
            cfg.jump,
            cfg.remote,
        ],
        timeout=60,
    )
    time.sleep(0.3)
    if control_active(cfg):
        print(f"pg117: control master started ({cfg.control_socket})")
        print("pg117: reuse with: scripts/pg117 run '<cmd>'")
        return 0
    print("pg117: failed to start control master", file=sys.stderr)
    return code or 1


def cmd_proxy_stop(cfg: Config) -> int:
    if cfg.control_socket.is_socket():
        subprocess.run(
            ["ssh", "-S", str(cfg.control_socket), "-O", "exit", cfg.remote],
            capture_output=True,
        )
        cfg.control_socket.unlink(missing_ok=True)
        print("pg117: control master stopped")
    else:
        print("pg117: no active control master")
    return 0


def cmd_proxy_status(cfg: Config) -> int:
    if control_active(cfg):
        subprocess.run(
            ["ssh", "-S", str(cfg.control_socket), "-O", "check", cfg.remote]
        )
        print(f"socket: {cfg.control_socket}")
        return 0
    print("pg117: control master not running")
    return 1


def forward_pidfile(cfg: Config) -> Path:
    return cfg.control_dir / "forward.pid"


def port_in_use(port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(("127.0.0.1", port)) == 0


def cmd_forward_start(cfg: Config) -> int:
    cfg.control_dir.mkdir(parents=True, exist_ok=True)
    pidfile = forward_pidfile(cfg)
    if pidfile.is_file():
        try:
            os.kill(int(pidfile.read_text().strip()), 0)
            print(
                f"pg117: forward already running on 127.0.0.1:{cfg.local_port} "
                f"(pid {pidfile.read_text().strip()})"
            )
            return 0
        except OSError:
            pidfile.unlink(missing_ok=True)

    bind = f"127.0.0.1:{cfg.local_port}"
    code = run_ssh(
        cfg,
        [
            "ssh",
            "-fN",
            "-o",
            "ExitOnForwardFailure=yes",
            "-o",
            "StrictHostKeyChecking=accept-new",
            "-o",
            "PreferredAuthentications=keyboard-interactive,password",
            "-o",
            "IdentitiesOnly=yes",
            "-o",
            "PubkeyAuthentication=no",
            "-L",
            f"{bind}:pg117:22",
            "-J",
            cfg.jump,
            cfg.remote,
        ],
        timeout=60,
    )
    time.sleep(0.5)
    if port_in_use(cfg.local_port):
        r = subprocess.run(
            ["pgrep", "-f", f"{bind}:pg117:22"],
            capture_output=True,
            text=True,
        )
        if r.stdout.strip():
            pidfile.write_text(r.stdout.strip().splitlines()[0])
        print(f"pg117: forward started {bind} -> pg117:22")
        print(f"pg117: direct: ssh -p {cfg.local_port} postgres4@127.0.0.1")
        return code
    print("pg117: forward may have failed; check forward status", file=sys.stderr)
    return code or 1


def cmd_forward_stop(cfg: Config) -> int:
    pidfile = forward_pidfile(cfg)
    if pidfile.is_file():
        pid = int(pidfile.read_text().strip())
        try:
            os.kill(pid, signal.SIGTERM)
            print(f"pg117: forward stopped (pid {pid})")
        except OSError:
            print("pg117: forward process not found")
        pidfile.unlink(missing_ok=True)
        return 0

    bind = f"127.0.0.1:{cfg.local_port}"
    r = subprocess.run(["pgrep", "-f", f"{bind}:pg117:22"], capture_output=True, text=True)
    if r.stdout.strip():
        for pid in r.stdout.strip().splitlines():
            try:
                os.kill(int(pid), signal.SIGTERM)
            except OSError:
                pass
        print("pg117: forward stopped")
        return 0
    print("pg117: forward not running")
    return 0


def cmd_forward_status(cfg: Config) -> int:
    pidfile = forward_pidfile(cfg)
    if pidfile.is_file():
        pid = pidfile.read_text().strip()
        try:
            os.kill(int(pid), 0)
            print(f"pg117: forward active on 127.0.0.1:{cfg.local_port} (pid {pid})")
            return 0
        except OSError:
            pass
    if port_in_use(cfg.local_port):
        print(f"pg117: forward active on 127.0.0.1:{cfg.local_port}")
        return 0
    print("pg117: forward not running")
    return 1


def parse_remote_path(arg: str) -> tuple[bool, str]:
    if arg.startswith("pg117:"):
        return True, arg[6:]
    if "@" in arg and ":" in arg:
        return True, arg.split(":", 1)[1]
    if ":" in arg and not Path(arg).exists():
        return True, arg.split(":", 1)[1]
    return False, arg


def cmd_scp(cfg: Config, local: str, remote_arg: str) -> int:
    is_remote, rpath = parse_remote_path(remote_arg)
    if is_remote:
        src, dst = local, f"{cfg.remote}:{rpath}"
    else:
        is_remote, rpath = parse_remote_path(local)
        if not is_remote:
            sys.exit("pg117 scp: use pg117:path for remote side")
        src, dst = f"{cfg.remote}:{rpath}", remote_arg

    if control_active(cfg):
        return subprocess.run(
            ["scp", "-o", f"ControlPath={cfg.control_socket}", src, dst],
            check=False,
        ).returncode

    return run_ssh(
        cfg,
        [
            "scp",
            "-o",
            "StrictHostKeyChecking=accept-new",
            "-o",
            "PreferredAuthentications=keyboard-interactive,password",
            "-o",
            "IdentitiesOnly=yes",
            "-o",
            "PubkeyAuthentication=no",
            "-J",
            cfg.jump,
            src,
            dst,
        ],
    )


def main() -> int:
    cfg = get_config()

    parser = argparse.ArgumentParser(description="SSH helper for pg117 via helios")
    sub = parser.add_subparsers(dest="command", required=True)

    run_p = sub.add_parser("run", help="run remote command")
    run_p.add_argument("remote_cmd", nargs=argparse.REMAINDER)

    sub.add_parser("ssh", help="interactive shell")

    proxy_p = sub.add_parser("proxy", help="SSH ControlMaster session")
    proxy_p.add_argument(
        "action", choices=["start", "stop", "status"], nargs="?", default="status"
    )

    fwd_p = sub.add_parser("forward", help="local TCP proxy to pg117:22")
    fwd_p.add_argument(
        "action", choices=["start", "stop", "status"], nargs="?", default="status"
    )

    scp_p = sub.add_parser("scp", help="copy files")
    scp_p.add_argument("src")
    scp_p.add_argument("dst")

    args = parser.parse_args()

    if args.command == "run":
        if not args.remote_cmd:
            parser.error("run requires a command")
        return cmd_run(cfg, " ".join(args.remote_cmd))
    if args.command == "ssh":
        return cmd_ssh(cfg)
    if args.command == "proxy":
        if args.action == "start":
            return cmd_proxy_start(cfg)
        if args.action == "stop":
            return cmd_proxy_stop(cfg)
        return cmd_proxy_status(cfg)
    if args.command == "forward":
        if args.action == "start":
            return cmd_forward_start(cfg)
        if args.action == "stop":
            return cmd_forward_stop(cfg)
        return cmd_forward_status(cfg)
    if args.command == "scp":
        return cmd_scp(cfg, args.src, args.dst)
    return 1


if __name__ == "__main__":
    sys.exit(main())
