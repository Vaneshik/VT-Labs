import math
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np


OUT_DIR = Path(__file__).resolve().parent.parent / "imgs"
OUT_DIR.mkdir(exist_ok=True)


def split_by_jumps(x, y, limit=100):
    y = y.copy()
    y[~np.isfinite(y)] = np.nan
    y[np.abs(y) > limit] = np.nan
    jumps = np.abs(np.diff(y)) > limit
    y[1:][jumps] = np.nan
    return x, y


def save_plot(name, title, x, y, xlabel="x", ylabel="y", ylim=None, points=None, sample_x=None, asymptotes=None):
    plt.figure(figsize=(9, 5))
    plt.axhline(0, color="black", linewidth=0.8)
    plt.axvline(0, color="black", linewidth=0.8)
    plt.grid(True, linewidth=0.4, alpha=0.5)
    plt.plot(x, y, color="#1f77b4", linewidth=1.7)

    if asymptotes:
        for value in asymptotes:
            plt.axvline(value, color="#d62728", linestyle="--", linewidth=1)

    if points:
        px = np.array(points)
        py = np.interp(px, x[np.isfinite(y)], y[np.isfinite(y)])
        plt.scatter(px, py, color="#ff7f0e", zorder=3)

    if sample_x:
        for value in sample_x:
            plt.axvline(value, color="#ff7f0e", linestyle=":", linewidth=1)

    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    if ylim:
        plt.ylim(*ylim)
    plt.tight_layout()
    plt.savefig(OUT_DIR / f"{name}.png", dpi=180)
    plt.close()


def csc(x):
    return 1 / np.sin(x)


def sec(x):
    return 1 / np.cos(x)


def cot(x):
    return 1 / np.tan(x)


def trig_system(x):
    sin = np.sin(x)
    cos = np.cos(x)
    tan = np.tan(x)
    csc_v = 1 / sin
    sec_v = 1 / cos
    cot_v = 1 / tan

    left = (((((csc_v ** 3 + (cos - tan)) - tan) ** 2) ** 3) ** 2) * ((sin + tan) * sec_v)
    right = (((csc_v * sec_v + tan) - sin ** 3) ** 2) + ((csc_v + cot_v ** 3) ** 2) ** 2
    add = sin * tan - (((csc_v + sec_v) * csc_v) / sec_v)
    return left * right + add


def log_system(x):
    log10 = np.log10(x)
    log3 = np.log(x) / np.log(3)
    log5 = np.log(x) / np.log(5)
    return ((((log10 / log10) ** 3) * log10) / (log3 * (log10 * log5))) ** 2


def draw_trig_functions():
    x = np.linspace(-2 * math.pi, 2 * math.pi, 4000)
    save_plot("sin", "sin(x)", x, np.sin(x), ylim=(-1.2, 1.2))
    save_plot("cos", "cos(x)", x, np.cos(x), ylim=(-1.2, 1.2))

    tx, ty = split_by_jumps(x, np.tan(x), 20)
    save_plot("tan", "tan(x)", tx, ty, ylim=(-10, 10), asymptotes=[-3 * math.pi / 2, -math.pi / 2, math.pi / 2, 3 * math.pi / 2])

    cx, cy = split_by_jumps(x, cot(x), 20)
    save_plot("cot", "cot(x)", cx, cy, ylim=(-10, 10), asymptotes=[-2 * math.pi, -math.pi, 0, math.pi, 2 * math.pi])

    sx, sy = split_by_jumps(x, sec(x), 20)
    save_plot("sec", "sec(x)", sx, sy, ylim=(-10, 10), asymptotes=[-3 * math.pi / 2, -math.pi / 2, math.pi / 2, 3 * math.pi / 2])

    csx, csy = split_by_jumps(x, csc(x), 20)
    save_plot("csc", "csc(x)", csx, csy, ylim=(-10, 10), asymptotes=[-2 * math.pi, -math.pi, 0, math.pi, 2 * math.pi])


def draw_log_functions():
    x = np.linspace(0.02, 10, 3000)
    save_plot("ln", "ln(x)", x, np.log(x), asymptotes=[0])
    save_plot("log3", "log3(x)", x, np.log(x) / np.log(3), asymptotes=[0])
    save_plot("log5", "log5(x)", x, np.log(x) / np.log(5), asymptotes=[0])
    save_plot("log10", "log10(x)", x, np.log10(x), asymptotes=[0])


def draw_system_parts():
    x_trig = np.linspace(-2 * math.pi + 0.001, -0.001, 7000)
    y_trig = trig_system(x_trig)
    x_trig, y_trig = split_by_jumps(x_trig, y_trig, 100)
    save_plot(
        "system_trig_part",
        "System, x <= 0",
        x_trig,
        y_trig,
        ylim=(-100, 100),
        sample_x=[-6.2, -6.0, -5.5, -4.0, -3.3, -2.0, -1.0, -0.5],
        asymptotes=[-3 * math.pi / 2, -math.pi, -math.pi / 2, 0],
    )

    x_log = np.linspace(0.02, 10, 7000)
    y_log = log_system(x_log)
    x_log, y_log = split_by_jumps(x_log, y_log, 100)
    save_plot(
        "system_log_part",
        "System, x > 0",
        x_log,
        y_log,
        ylim=(0, 100),
        sample_x=[0.1, 0.5, 2.0, 3.0, 8.0, 10.0],
        asymptotes=[0, 1],
    )

    x_full_left = np.linspace(-2 * math.pi + 0.001, -0.001, 6000)
    y_full_left = trig_system(x_full_left)
    x_full_right = np.linspace(0.02, 10, 6000)
    y_full_right = log_system(x_full_right)
    _, y_full_left = split_by_jumps(x_full_left, y_full_left, 100)
    _, y_full_right = split_by_jumps(x_full_right, y_full_right, 100)

    plt.figure(figsize=(10, 5))
    plt.axhline(0, color="black", linewidth=0.8)
    plt.axvline(0, color="black", linewidth=0.8)
    plt.grid(True, linewidth=0.4, alpha=0.5)
    plt.plot(x_full_left, y_full_left, color="#1f77b4", linewidth=1.5, label="x <= 0")
    plt.plot(x_full_right, y_full_right, color="#2ca02c", linewidth=1.5, label="x > 0")
    for value in [-3 * math.pi / 2, -math.pi, -math.pi / 2, 0, 1]:
        plt.axvline(value, color="#d62728", linestyle="--", linewidth=1)
    plt.title("Full system")
    plt.xlabel("x")
    plt.ylabel("f(x)")
    plt.ylim(-100, 100)
    plt.legend()
    plt.tight_layout()
    plt.savefig(OUT_DIR / "system_full.png", dpi=180)
    plt.close()


def draw_equivalence():
    x_log = np.linspace(0.02, 10, 6000)
    y_log = log_system(x_log)
    x_log, y_log = split_by_jumps(x_log, y_log, 100)
    save_plot(
        "equivalence_log",
        "Equivalence classes, x > 0",
        x_log,
        y_log,
        ylim=(0, 100),
        sample_x=[0.1, 0.5, 2.0, 3.0, 8.0, 10.0],
        asymptotes=[0, 1],
    )

    x_trig = np.linspace(-2 * math.pi + 0.001, -0.001, 6000)
    y_trig = trig_system(x_trig)
    x_trig, y_trig = split_by_jumps(x_trig, y_trig, 100)
    save_plot(
        "equivalence_trig",
        "Equivalence classes, -2pi <= x <= 0",
        x_trig,
        y_trig,
        ylim=(-100, 100),
        sample_x=[-6.2, -6.0, -5.5, -4.0, -3.3, -2.0, -1.0, -0.5],
        asymptotes=[-3 * math.pi / 2, -math.pi, -math.pi / 2, 0],
    )


def main():
    draw_trig_functions()
    draw_log_functions()
    draw_system_parts()
    draw_equivalence()
    print(f"Saved graphs to {OUT_DIR}")


if __name__ == "__main__":
    main()
