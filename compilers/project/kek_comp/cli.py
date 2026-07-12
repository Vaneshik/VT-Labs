from __future__ import annotations

import argparse
from pathlib import Path

from .builtins import KEK_HELP
from .errors import KekError
from .generator import RandomProgramGenerator
from .interpreter import TreeInterpreter
from .pipeline import analyze_source
from .token import TokenType


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="kek-comp", description="Compile and run kek programs.")
    parser.add_argument("input", nargs="?", help="source file")
    parser.add_argument("--file", dest="file", help="source file")
    parser.add_argument("--generate", nargs="*", metavar=("OUTPUT", "COUNT"), help="generate a random .kek program")
    parser.add_argument("--tokens", action="store_true", help="print tokens")
    parser.add_argument("--ast", action="store_true", help="print AST")
    parser.add_argument("--kek", action="store_true", help="print kek-specific language features")
    args = parser.parse_args(argv)

    if args.kek:
        print(KEK_HELP.strip())
        return 0

    if args.generate is not None:
        return _generate(args.generate)

    path = Path(args.file or args.input or _default_input())
    try:
        source = path.read_text(encoding="utf-8-sig")
        result = analyze_source(source)
    except OSError as error:
        print(f"[IO Error] {error}")
        return 1
    except KekError as error:
        print(error)
        return 1

    if args.tokens:
        for token in result.tokens:
            if token.type != TokenType.EOF:
                print(f"{token.type.name} {token.value!r} [{token.line}:{token.column}]")
        return 0

    for error in result.parser_errors:
        print(error)
    for error in result.semantic.errors:
        print(error)
    for warning in result.semantic.warnings:
        print(warning)

    if result.parser_errors or result.semantic.errors:
        return 1

    if args.ast:
        for statement in result.statements:
            print(statement)
        return 0

    interpretation = TreeInterpreter(stdout=None).interpret(result.statements)
    for line in interpretation.output:
        print(line)
    return 1 if interpretation.error else 0


def _generate(values: list[str]) -> int:
    output = values[0] if values else "generated.kek"
    count = 10
    if len(values) >= 2:
        try:
            count = int(values[1])
        except ValueError:
            count = 10
    if count <= 0:
        count = 10
    Path(output).write_text(RandomProgramGenerator().generate(count), encoding="utf-8")
    print(f"Generated {output}")
    return 0


def _default_input() -> str:
    if Path("data.kek").exists():
        return "data.kek"
    return "data"
