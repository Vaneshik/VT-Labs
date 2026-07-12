from __future__ import annotations

import random
from enum import Enum, auto


class ValueKind(Enum):
    NUMBER = auto()
    STRING = auto()
    NUMBER_ARRAY = auto()
    STRING_ARRAY = auto()


class RandomProgramGenerator:
    def __init__(self, rng: random.Random | None = None):
        self.rng = rng or random.Random()
        self.names = ["x", "y", "z", "alpha", "beta", "count", "total", "index", "sum", "items"]
        self.declared: dict[str, ValueKind] = {}
        self.used: set[str] = set()

    def generate(self, statement_count: int = 10) -> str:
        self.declared.clear()
        self.used.clear()
        lines: list[str] = []
        for _ in range(3):
            lines.append(self._var_declaration(0))
        self._block(lines, statement_count, 0)
        return "\n".join(lines) + "\n"

    def _block(self, lines: list[str], count: int, indent_level: int) -> None:
        for _ in range(count):
            choice = self.rng.randrange(5 if indent_level > 2 else 6)
            if choice == 0:
                lines.append(self._var_declaration(indent_level))
            elif choice == 1:
                lines.append(self._assignment(indent_level))
            elif choice == 2:
                lines.append(f"{self._indent(indent_level)}print {self._printable_expression()};")
            elif choice == 3:
                lines.append(f"{self._indent(indent_level)}if ({self._condition()}) {{")
                self._block(lines, self.rng.randrange(1, 4), indent_level + 1)
                if self.rng.random() > 0.5:
                    lines.append(f"{self._indent(indent_level)}}} else {{")
                    self._block(lines, self.rng.randrange(1, 3), indent_level + 1)
                lines.append(f"{self._indent(indent_level)}}}")
            elif choice == 4:
                lines.append(f"{self._indent(indent_level)}while ({self._mostly_false_condition()}) {{")
                self._block(lines, self.rng.randrange(1, 3), indent_level + 1)
                lines.append(f"{self._indent(indent_level)}}}")
            else:
                lines.append(self._array_mutation(indent_level))

    def _var_declaration(self, indent_level: int) -> str:
        name = self._fresh_name()
        kind = self._random_kind()
        initializer = self._expression(kind)
        if indent_level == 0:
            self.declared[name] = kind
        return f"{self._indent(indent_level)}var {name} = {initializer};"

    def _assignment(self, indent_level: int) -> str:
        if not self.declared:
            return self._var_declaration(indent_level)
        name, kind = self._random_var()
        return f"{self._indent(indent_level)}{name} = {self._expression(kind)};"

    def _array_mutation(self, indent_level: int) -> str:
        arrays = [(name, kind) for name, kind in self.declared.items() if kind in (ValueKind.NUMBER_ARRAY, ValueKind.STRING_ARRAY)]
        if not arrays:
            return self._var_declaration(indent_level)
        name, kind = self.rng.choice(arrays)
        element_kind = ValueKind.NUMBER if kind == ValueKind.NUMBER_ARRAY else ValueKind.STRING
        return f"{self._indent(indent_level)}{name}[{self.rng.randrange(3)}] = {self._expression(element_kind)};"

    def _expression(self, kind: ValueKind) -> str:
        if kind == ValueKind.NUMBER:
            return self._number_expression()
        if kind == ValueKind.STRING:
            return self._string_expression()
        if kind == ValueKind.NUMBER_ARRAY:
            return self._array_literal(ValueKind.NUMBER)
        return self._array_literal(ValueKind.STRING)

    def _printable_expression(self) -> str:
        if self.declared and self.rng.random() > 0.35:
            name, kind = self._random_var()
            if kind in (ValueKind.NUMBER_ARRAY, ValueKind.STRING_ARRAY) and self.rng.choice([True, False]):
                return f"{name}[{self.rng.randrange(3)}]"
            return name
        return self._expression(self._random_kind())

    def _number_expression(self) -> str:
        number_vars = [name for name, kind in self.declared.items() if kind == ValueKind.NUMBER]
        if number_vars and self.rng.random() > 0.45:
            if self.rng.random() > 0.55:
                return self.rng.choice(number_vars)
            left = self._number_atom(number_vars)
            right = self._number_atom(number_vars, non_zero=True)
            return f"{left} {self.rng.choice(['+', '-', '*', '/'])} {right}"
        return str(self.rng.randrange(1, 100))

    def _string_expression(self) -> str:
        string_vars = [name for name, kind in self.declared.items() if kind == ValueKind.STRING]
        if string_vars and self.rng.random() > 0.6:
            return self.rng.choice(string_vars)
        return '"' + self.rng.choice(["kek", "compiler", "token", "array", "python"]) + '"'

    def _array_literal(self, element_kind: ValueKind) -> str:
        return "[" + ", ".join(self._expression(element_kind) for _ in range(3)) + "]"

    def _condition(self) -> str:
        condition = self._comparison()
        if self.rng.random() > 0.7:
            condition = f"({condition}) {self.rng.choice(['&&', '||'])} ({self._comparison()})"
        return condition

    def _mostly_false_condition(self) -> str:
        return f"{self.rng.randrange(1, 40)} == {self.rng.randrange(41, 80)}"

    def _comparison(self) -> str:
        return f"{self._number_expression()} {self.rng.choice(['==', '!=', '<', '>', '<=', '>='])} {self._number_expression()}"

    def _number_atom(self, number_vars: list[str], non_zero: bool = False) -> str:
        if number_vars and self.rng.choice([True, False]):
            return self.rng.choice(number_vars)
        return str(self.rng.randrange(1 if non_zero else 0, 100))

    def _random_kind(self) -> ValueKind:
        return self.rng.choice(list(ValueKind))

    def _random_var(self) -> tuple[str, ValueKind]:
        return self.rng.choice(list(self.declared.items()))

    def _fresh_name(self) -> str:
        base = self.rng.choice(self.names)
        if base not in self.used:
            self.used.add(base)
            return base
        name = f"{base}{len(self.used)}"
        self.used.add(name)
        return name

    def _indent(self, level: int) -> str:
        return " " * (level * 4)
