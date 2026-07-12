from __future__ import annotations

from dataclasses import dataclass
from typing import TextIO

from .ast_nodes import (
    ArrayExpression,
    AssignExpression,
    BinaryExpression,
    BlockStatement,
    CallExpression,
    Expression,
    ExpressionStatement,
    FunctionStatement,
    IfStatement,
    IndexAssignExpression,
    IndexExpression,
    NumberExpression,
    PrintStatement,
    ReturnStatement,
    Statement,
    StringExpression,
    UnaryExpression,
    VarStatement,
    VariableExpression,
    WhileStatement,
)
from .errors import RuntimeKekError
from .token import TokenType


class ReturnSignal(Exception):
    def __init__(self, value: object | None):
        self.value = value


class RuntimeEnvironment:
    def __init__(self, parent: RuntimeEnvironment | None = None):
        self.parent = parent
        self.values: dict[str, object | None] = {}
        self.functions: dict[str, FunctionStatement] = {}

    def define(self, name: str, value: object | None) -> None:
        self.values[name] = value

    def assign(self, name: str, value: object | None) -> None:
        if name in self.values:
            self.values[name] = value
            return
        if self.parent is not None:
            self.parent.assign(name, value)
            return
        raise RuntimeKekError(f"[Runtime Error] Неизвестная переменная '{name}'.")

    def get(self, name: str) -> object | None:
        if name in self.values:
            return self.values[name]
        if self.parent is not None:
            return self.parent.get(name)
        raise RuntimeKekError(f"[Runtime Error] Неизвестная переменная '{name}'.")

    def define_function(self, name: str, function: FunctionStatement) -> None:
        self.functions[name] = function

    def get_function(self, name: str) -> FunctionStatement:
        if name in self.functions:
            return self.functions[name]
        if self.parent is not None:
            return self.parent.get_function(name)
        raise RuntimeKekError(f"[Runtime Error] Неизвестная функция '{name}'.")


@dataclass
class InterpreterResult:
    output: list[str]
    error: str | None = None


class TreeInterpreter:
    def __init__(self, stdout: TextIO | None = None):
        self.environment = RuntimeEnvironment()
        self.stdout = stdout
        self.output: list[str] = []

    def interpret(self, statements: list[Statement]) -> InterpreterResult:
        try:
            for statement in statements:
                self._execute(statement)
            return InterpreterResult(self.output)
        except RuntimeKekError as error:
            message = f"[CRITICAL RUNTIME ERROR]: {error}"
            self._write(message)
            return InterpreterResult(self.output, message)

    def _execute(self, statement: Statement) -> None:
        match statement:
            case PrintStatement():
                self._write(self._format_value(self._evaluate(statement.expression)))
            case VarStatement():
                value = self._evaluate(statement.initializer) if statement.initializer is not None else None
                self.environment.define(statement.name, value)
            case ExpressionStatement():
                self._evaluate(statement.expression)
            case BlockStatement():
                self._execute_block(statement.statements, RuntimeEnvironment(self.environment))
            case IfStatement():
                if self._truthy(self._evaluate(statement.condition)):
                    self._execute(statement.then_branch)
                elif statement.else_branch is not None:
                    self._execute(statement.else_branch)
            case WhileStatement():
                while self._truthy(self._evaluate(statement.condition)):
                    self._execute(statement.body)
            case FunctionStatement():
                self.environment.define_function(statement.name, statement)
            case ReturnStatement():
                value = self._evaluate(statement.value) if statement.value is not None else None
                raise ReturnSignal(value)
            case _:
                raise RuntimeKekError(f"[Runtime Error] Неизвестная инструкция: {type(statement).__name__}")

    def _execute_block(self, statements: list[Statement], environment: RuntimeEnvironment) -> None:
        previous = self.environment
        self.environment = environment
        try:
            for statement in statements:
                self._execute(statement)
        finally:
            self.environment = previous

    def _evaluate(self, expression: Expression) -> object | None:
        match expression:
            case NumberExpression():
                return expression.value
            case StringExpression():
                return expression.value
            case ArrayExpression():
                return [self._evaluate(element) for element in expression.elements]
            case VariableExpression():
                return self.environment.get(expression.name)
            case AssignExpression():
                value = self._evaluate(expression.value)
                self.environment.assign(expression.name, value)
                return value
            case IndexExpression():
                array = self._as_array(self._evaluate(expression.array))
                index = self._as_index(self._evaluate(expression.index), len(array))
                return array[index]
            case IndexAssignExpression():
                array = self._as_array(self._evaluate(expression.array))
                index = self._as_index(self._evaluate(expression.index), len(array))
                value = self._evaluate(expression.value)
                array[index] = value
                return value
            case BinaryExpression():
                return self._binary(expression)
            case UnaryExpression():
                right = self._evaluate(expression.right)
                if expression.operator == TokenType.MINUS:
                    return -self._as_number(right)
                if expression.operator == TokenType.EXCL:
                    return not self._truthy(right)
                return right
            case CallExpression():
                return self._call(expression)
            case _:
                raise RuntimeKekError(f"[Runtime Error] Неизвестное выражение: {type(expression).__name__}")

    def _binary(self, expression: BinaryExpression) -> object:
        if expression.operator == TokenType.OR:
            left = self._evaluate(expression.left)
            return True if self._truthy(left) else self._truthy(self._evaluate(expression.right))
        if expression.operator == TokenType.AND:
            left = self._evaluate(expression.left)
            return False if not self._truthy(left) else self._truthy(self._evaluate(expression.right))

        left = self._evaluate(expression.left)
        right = self._evaluate(expression.right)
        match expression.operator:
            case TokenType.MINUS:
                return self._as_number(left) - self._as_number(right)
            case TokenType.SLASH:
                divisor = self._as_number(right)
                if divisor == 0:
                    raise RuntimeKekError("[Runtime Error] Деление на ноль!")
                return self._as_number(left) / divisor
            case TokenType.STAR:
                return self._as_number(left) * self._as_number(right)
            case TokenType.PLUS:
                if isinstance(left, float) and isinstance(right, float):
                    return left + right
                if isinstance(left, str) or isinstance(right, str):
                    return self._format_value(left) + self._format_value(right)
                raise RuntimeKekError("[Runtime Error] Нельзя применить оператор '+' к этим значениям.")
            case TokenType.GT:
                return self._as_number(left) > self._as_number(right)
            case TokenType.GTEQ:
                return self._as_number(left) >= self._as_number(right)
            case TokenType.LT:
                return self._as_number(left) < self._as_number(right)
            case TokenType.LTEQ:
                return self._as_number(left) <= self._as_number(right)
            case TokenType.EQEQ:
                return left == right
            case TokenType.NEQ:
                return left != right
        raise RuntimeKekError("[Runtime Error] Неизвестный бинарный оператор.")

    def _call(self, expression: CallExpression) -> object | None:
        if expression.callee_name == "kek":
            arguments = [self._evaluate(argument) for argument in expression.arguments]
            value = arguments[0] if arguments else None
            self._write(f"kek: {self._format_value(value)}")
            return None

        function = self.environment.get_function(expression.callee_name)
        arguments = [self._evaluate(argument) for argument in expression.arguments]
        call_environment = RuntimeEnvironment(self.environment)
        for index, parameter in enumerate(function.parameters):
            call_environment.define(parameter, arguments[index] if index < len(arguments) else None)

        previous = self.environment
        self.environment = call_environment
        try:
            for statement in function.body.statements:
                self._execute(statement)
        except ReturnSignal as signal:
            return signal.value
        finally:
            self.environment = previous
        return None

    def _truthy(self, value: object | None) -> bool:
        if value is None:
            return False
        if isinstance(value, bool):
            return value
        return True

    def _as_number(self, value: object | None) -> float:
        if isinstance(value, float):
            return value
        if isinstance(value, int):
            return float(value)
        raise RuntimeKekError(f"[Runtime Error] Ожидалось число, получено: {self._format_value(value)}.")

    def _as_array(self, value: object | None) -> list[object | None]:
        if isinstance(value, list):
            return value
        raise RuntimeKekError(f"[Runtime Error] Ожидался массив, получено: {self._format_value(value)}.")

    def _as_index(self, value: object | None, array_length: int) -> int:
        raw = self._as_number(value)
        if not raw.is_integer():
            raise RuntimeKekError(f"[Runtime Error] Индекс массива должен быть целым числом, получено: {self._format_value(raw)}.")
        index = int(raw)
        if index < 0 or index >= array_length:
            raise RuntimeKekError(f"[Runtime Error] Индекс массива вне границ: {index}. Размер массива: {array_length}.")
        return index

    def _write(self, text: str) -> None:
        self.output.append(text)
        if self.stdout is not None:
            print(text, file=self.stdout)

    def _format_value(self, value: object | None) -> str:
        if value is None:
            return ""
        if isinstance(value, bool):
            return "true" if value else "false"
        if isinstance(value, float) and value.is_integer():
            return str(int(value))
        if isinstance(value, list):
            return "[" + ", ".join(self._format_value(item) for item in value) + "]"
        return str(value)
