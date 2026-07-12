from __future__ import annotations

from dataclasses import dataclass
from enum import Enum, auto

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
from .builtins import BUILTIN_ARITY
from .token import TokenType


class DataType(Enum):
    UNKNOWN = auto()
    NUMBER = auto()
    STRING = auto()
    BOOL = auto()
    ARRAY = auto()
    FUNCTION = auto()

    def __str__(self) -> str:
        return self.name.title()


@dataclass
class SymbolInfo:
    name: str
    initialized: bool
    used: bool
    data_type: DataType
    arity: int | None = None
    array_element_type: DataType | None = None


class SemanticEnvironment:
    def __init__(self, parent: SemanticEnvironment | None = None):
        self.parent = parent
        self.symbols: dict[str, SymbolInfo] = {}

    def define_variable(
        self,
        name: str,
        initialized: bool,
        data_type: DataType = DataType.UNKNOWN,
        array_element_type: DataType | None = None,
    ) -> bool:
        if name in self.symbols:
            return False
        self.symbols[name] = SymbolInfo(name, initialized, False, data_type, array_element_type=array_element_type)
        return True

    def define_function(self, name: str, arity: int) -> bool:
        if name in self.symbols:
            return False
        self.symbols[name] = SymbolInfo(name, True, True, DataType.FUNCTION, arity=arity)
        return True

    def get(self, name: str) -> SymbolInfo | None:
        if name in self.symbols:
            return self.symbols[name]
        if self.parent is not None:
            return self.parent.get(name)
        return None

    def local_symbols(self) -> list[SymbolInfo]:
        return list(self.symbols.values())


@dataclass
class SemanticResult:
    errors: list[str]
    warnings: list[str]


class SemanticAnalyzer:
    def __init__(self):
        self.environment = SemanticEnvironment()
        self.errors: list[str] = []
        self.warnings: list[str] = []
        for name, arity in BUILTIN_ARITY.items():
            self.environment.define_function(name, arity)

    def analyze(self, statements: list[Statement]) -> SemanticResult:
        for statement in statements:
            self._statement(statement)
        self._check_unused_variables()
        return SemanticResult(self.errors, self.warnings)

    def _statement(self, statement: Statement) -> None:
        match statement:
            case VarStatement():
                self._var_statement(statement)
            case PrintStatement():
                self._expression(statement.expression)
            case ExpressionStatement():
                self._expression(statement.expression)
            case BlockStatement():
                self._block_statement(statement)
            case IfStatement():
                self._if_statement(statement)
            case WhileStatement():
                self._while_statement(statement)
            case FunctionStatement():
                self._function_statement(statement)
            case ReturnStatement():
                if statement.value is not None:
                    self._expression(statement.value)
            case _:
                self._error(statement, f"Неподдерживаемая инструкция: {type(statement).__name__}")

    def _function_statement(self, statement: FunctionStatement) -> None:
        if not self.environment.define_function(statement.name, len(statement.parameters)):
            self._error(statement, f"Функция '{statement.name}' уже объявлена в этой области видимости.")
            return

        previous = self.environment
        self.environment = SemanticEnvironment(previous)
        for parameter in statement.parameters:
            if not self.environment.define_variable(parameter, True, DataType.UNKNOWN):
                self._error(statement, f"Параметр '{parameter}' уже объявлен в функции '{statement.name}'.")
        for inner in statement.body.statements:
            self._statement(inner)
        self._check_unused_variables()
        self.environment = previous

    def _var_statement(self, statement: VarStatement) -> None:
        init_type = DataType.UNKNOWN
        element_type = None
        if statement.initializer is not None:
            init_type = self._expression(statement.initializer)
            element_type = self._array_element_type(statement.initializer)

        if not self.environment.define_variable(statement.name, statement.initializer is not None, init_type, element_type):
            self._error(statement, f"Переменная '{statement.name}' уже объявлена в этой области видимости.")

    def _block_statement(self, statement: BlockStatement) -> None:
        previous = self.environment
        self.environment = SemanticEnvironment(previous)
        for inner in statement.statements:
            self._statement(inner)
        self._check_unused_variables()
        self.environment = previous

    def _if_statement(self, statement: IfStatement) -> None:
        condition_type = self._expression(statement.condition)
        if condition_type not in (DataType.BOOL, DataType.UNKNOWN):
            self._error(statement, f"Условие 'if' должно быть логическим выражением (Bool), а получено: {condition_type}.")
        if self._is_always_false(statement.condition):
            self._warning(statement, "Обнаружен недостижимый код: ветка 'then' (if) никогда не выполнится.")
        self._statement(statement.then_branch)
        if statement.else_branch is not None:
            self._statement(statement.else_branch)

    def _while_statement(self, statement: WhileStatement) -> None:
        condition_type = self._expression(statement.condition)
        if condition_type not in (DataType.BOOL, DataType.UNKNOWN):
            self._error(statement, f"Условие 'while' должно быть логическим выражением (Bool), а получено: {condition_type}.")
        if self._is_always_false(statement.condition):
            self._warning(statement, "Обнаружен недостижимый код: тело цикла 'while' никогда не выполнится.")
        self._statement(statement.body)

    def _expression(self, expression: Expression) -> DataType:
        match expression:
            case NumberExpression():
                return DataType.NUMBER
            case StringExpression():
                return DataType.STRING
            case ArrayExpression():
                return self._array_expression(expression)
            case VariableExpression():
                return self._variable_expression(expression)
            case AssignExpression():
                return self._assign_expression(expression)
            case IndexExpression():
                return self._index_expression(expression)
            case IndexAssignExpression():
                return self._index_assign_expression(expression)
            case BinaryExpression():
                return self._binary_expression(expression)
            case UnaryExpression():
                return self._unary_expression(expression)
            case CallExpression():
                return self._call_expression(expression)
            case _:
                self._error(expression, f"Неподдерживаемое выражение: {type(expression).__name__}")
                return DataType.UNKNOWN

    def _variable_expression(self, expression: VariableExpression) -> DataType:
        symbol = self.environment.get(expression.name)
        if symbol is None:
            self._error(expression, f"Использование необъявленной переменной '{expression.name}'.")
            return DataType.UNKNOWN
        symbol.used = True
        if not symbol.initialized:
            self._error(expression, f"Использование неинициализированной переменной '{expression.name}'.")
        return symbol.data_type

    def _assign_expression(self, expression: AssignExpression) -> DataType:
        value_type = self._expression(expression.value)
        symbol = self.environment.get(expression.name)
        if symbol is None:
            self._error(expression, f"Попытка записи в необъявленную переменную '{expression.name}'.")
            return value_type

        if symbol.data_type not in (DataType.UNKNOWN, value_type) and value_type != DataType.UNKNOWN:
            self._error(expression, f"Ошибка типов: нельзя присвоить значение типа {value_type} переменной '{expression.name}' (ожидался тип {symbol.data_type}).")
        elif symbol.data_type == DataType.UNKNOWN and value_type != DataType.UNKNOWN:
            symbol.data_type = value_type

        if symbol.data_type == DataType.ARRAY and symbol.array_element_type is None:
            symbol.array_element_type = self._array_element_type(expression.value)
        symbol.initialized = True
        return symbol.data_type

    def _array_expression(self, expression: ArrayExpression) -> DataType:
        types = [self._expression(element) for element in expression.elements]
        known = [data_type for data_type in types if data_type != DataType.UNKNOWN]
        if len(set(known)) > 1:
            self._error(expression, f"Все элементы массива должны быть одного типа. Получено: {', '.join(str(t) for t in sorted(set(known), key=str))}.")
        return DataType.ARRAY

    def _index_expression(self, expression: IndexExpression) -> DataType:
        array_type = self._expression(expression.array)
        index_type = self._expression(expression.index)
        if array_type not in (DataType.ARRAY, DataType.UNKNOWN):
            self._error(expression, f"Индексировать можно только массив, получено: {array_type}.")
        if index_type not in (DataType.NUMBER, DataType.UNKNOWN):
            self._error(expression, f"Индекс массива должен быть числом (Number), получено: {index_type}.")
        return self._array_element_type(expression.array) or DataType.UNKNOWN

    def _index_assign_expression(self, expression: IndexAssignExpression) -> DataType:
        array_type = self._expression(expression.array)
        index_type = self._expression(expression.index)
        value_type = self._expression(expression.value)
        if array_type not in (DataType.ARRAY, DataType.UNKNOWN):
            self._error(expression, f"Записывать по индексу можно только в массив, получено: {array_type}.")
        if index_type not in (DataType.NUMBER, DataType.UNKNOWN):
            self._error(expression, f"Индекс массива должен быть числом (Number), получено: {index_type}.")
        element_type = self._array_element_type(expression.array)
        if element_type not in (None, DataType.UNKNOWN, value_type) and value_type != DataType.UNKNOWN:
            self._error(expression, f"Ошибка типов: нельзя записать значение типа {value_type} в массив с элементами типа {element_type}.")
        return value_type

    def _call_expression(self, expression: CallExpression) -> DataType:
        symbol = self.environment.get(expression.callee_name)
        if symbol is None or symbol.data_type != DataType.FUNCTION:
            self._error(expression, f"Вызов неопределенной функции '{expression.callee_name}'.")
        else:
            symbol.used = True
            if symbol.arity != len(expression.arguments):
                self._error(expression, f"Неверное количество аргументов при вызове функции '{expression.callee_name}'. Ожидалось: {symbol.arity}, получено: {len(expression.arguments)}.")
        for argument in expression.arguments:
            self._expression(argument)
        return DataType.UNKNOWN

    def _binary_expression(self, expression: BinaryExpression) -> DataType:
        left_type = self._expression(expression.left)
        right_type = self._expression(expression.right)
        if DataType.UNKNOWN in (left_type, right_type):
            return DataType.UNKNOWN

        match expression.operator:
            case TokenType.PLUS:
                if left_type == DataType.STRING or right_type == DataType.STRING:
                    return DataType.STRING
                if left_type == right_type == DataType.NUMBER:
                    return DataType.NUMBER
                self._error(expression, f"Ошибка типов: нельзя применить оператор '+' к {left_type} и {right_type}.")
            case TokenType.MINUS | TokenType.STAR | TokenType.SLASH:
                if left_type == right_type == DataType.NUMBER:
                    return DataType.NUMBER
                self._error(expression, f"Ошибка типов: оператор '{expression.operator.name}' работает только с числами (Number). Получено: {left_type} и {right_type}.")
            case TokenType.LT | TokenType.GT | TokenType.LTEQ | TokenType.GTEQ:
                if left_type == right_type == DataType.NUMBER:
                    return DataType.BOOL
                self._error(expression, f"Ошибка типов: операторы сравнения работают только с числами (Number). Получено: {left_type} и {right_type}.")
            case TokenType.EQEQ | TokenType.NEQ:
                if left_type != right_type:
                    self._warning(expression, f"Сравнение на равенство разных типов ({left_type} и {right_type}) всегда будет ложным.")
                return DataType.BOOL
            case TokenType.AND | TokenType.OR:
                if left_type == right_type == DataType.BOOL:
                    return DataType.BOOL
                self._error(expression, f"Ошибка типов: логические операторы (&&, ||) требуют тип Bool. Получено: {left_type} и {right_type}.")
        return DataType.UNKNOWN

    def _unary_expression(self, expression: UnaryExpression) -> DataType:
        right_type = self._expression(expression.right)
        if right_type == DataType.UNKNOWN:
            return DataType.UNKNOWN
        if expression.operator == TokenType.MINUS:
            if right_type != DataType.NUMBER:
                self._error(expression, f"Ошибка типов: унарный минус применяется только к числам. Получено: {right_type}.")
                return DataType.UNKNOWN
            return DataType.NUMBER
        if expression.operator == TokenType.EXCL:
            if right_type != DataType.BOOL:
                self._error(expression, f"Ошибка типов: оператор '!' применяется только к Bool. Получено: {right_type}.")
                return DataType.UNKNOWN
            return DataType.BOOL
        return right_type

    def _array_element_type(self, expression: Expression) -> DataType | None:
        match expression:
            case ArrayExpression():
                known = [self._static_type(element) for element in expression.elements]
                known = [data_type for data_type in known if data_type not in (None, DataType.UNKNOWN)]
                return known[0] if known else None
            case VariableExpression():
                symbol = self.environment.get(expression.name)
                return symbol.array_element_type if symbol else None
            case IndexExpression():
                return self._array_element_type(expression.array)
            case _:
                return None

    def _static_type(self, expression: Expression) -> DataType | None:
        match expression:
            case NumberExpression():
                return DataType.NUMBER
            case StringExpression():
                return DataType.STRING
            case ArrayExpression():
                return DataType.ARRAY
            case VariableExpression():
                symbol = self.environment.get(expression.name)
                return symbol.data_type if symbol else None
            case IndexExpression():
                return self._array_element_type(expression.array)
            case _:
                return None

    def _check_unused_variables(self) -> None:
        for symbol in self.environment.local_symbols():
            if symbol.data_type != DataType.FUNCTION and not symbol.used:
                self.warnings.append(f"[Semantic Warning] Переменная '{symbol.name}' объявлена, но ни разу не использована.")

    def _is_always_false(self, expression: Expression) -> bool:
        if isinstance(expression, BinaryExpression):
            left = expression.left
            right = expression.right
            if isinstance(left, NumberExpression) and isinstance(right, NumberExpression):
                if expression.operator == TokenType.EQEQ:
                    return left.value != right.value
                if expression.operator == TokenType.NEQ:
                    return left.value == right.value
        return False

    def _error(self, node: Expression | Statement, message: str) -> None:
        self.errors.append(f"[Semantic Error] [{node.line}:{node.column}] {message}")

    def _warning(self, node: Expression | Statement, message: str) -> None:
        self.warnings.append(f"[Semantic Warning] [{node.line}:{node.column}] {message}")
