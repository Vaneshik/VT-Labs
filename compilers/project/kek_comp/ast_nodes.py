from __future__ import annotations

from dataclasses import dataclass

from .token import TokenType


@dataclass(frozen=True)
class Node:
    line: int
    column: int


class Expression(Node):
    pass


class Statement(Node):
    pass


@dataclass(frozen=True)
class NumberExpression(Expression):
    value: float


@dataclass(frozen=True)
class StringExpression(Expression):
    value: str


@dataclass(frozen=True)
class ArrayExpression(Expression):
    elements: list[Expression]


@dataclass(frozen=True)
class VariableExpression(Expression):
    name: str


@dataclass(frozen=True)
class BinaryExpression(Expression):
    left: Expression
    operator: TokenType
    right: Expression


@dataclass(frozen=True)
class UnaryExpression(Expression):
    operator: TokenType
    right: Expression


@dataclass(frozen=True)
class AssignExpression(Expression):
    name: str
    value: Expression


@dataclass(frozen=True)
class CallExpression(Expression):
    callee_name: str
    arguments: list[Expression]


@dataclass(frozen=True)
class IndexExpression(Expression):
    array: Expression
    index: Expression


@dataclass(frozen=True)
class IndexAssignExpression(Expression):
    array: Expression
    index: Expression
    value: Expression


@dataclass(frozen=True)
class ExpressionStatement(Statement):
    expression: Expression


@dataclass(frozen=True)
class PrintStatement(Statement):
    expression: Expression


@dataclass(frozen=True)
class VarStatement(Statement):
    name: str
    initializer: Expression | None


@dataclass(frozen=True)
class BlockStatement(Statement):
    statements: list[Statement]


@dataclass(frozen=True)
class IfStatement(Statement):
    condition: Expression
    then_branch: Statement
    else_branch: Statement | None


@dataclass(frozen=True)
class WhileStatement(Statement):
    condition: Expression
    body: Statement


@dataclass(frozen=True)
class FunctionStatement(Statement):
    name: str
    parameters: list[str]
    body: BlockStatement


@dataclass(frozen=True)
class ReturnStatement(Statement):
    value: Expression | None
