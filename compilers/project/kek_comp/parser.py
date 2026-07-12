from __future__ import annotations

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
from .errors import ParseError
from .token import Token, TokenType


class Parser:
    def __init__(self, tokens: list[Token]):
        self.tokens = tokens
        self.position = 0
        self.errors: list[str] = []

    def parse(self) -> list[Statement]:
        statements: list[Statement] = []
        while not self._at_end:
            try:
                statements.append(self._declaration())
            except ParseError:
                self._synchronize()
        return statements

    def _declaration(self) -> Statement:
        if self._match(TokenType.FUN):
            return self._function()
        if self._match(TokenType.VAR):
            return self._var_declaration()
        return self._statement()

    def _function(self) -> FunctionStatement:
        keyword = self._previous()
        name = self._consume(TokenType.ID, "Ожидается имя функции.")
        self._consume(TokenType.LPAREN, "Ожидается '(' после имени функции.")
        parameters: list[str] = []
        if not self._check(TokenType.RPAREN):
            while True:
                parameters.append(self._consume(TokenType.ID, "Ожидается имя параметра.").value)
                if not self._match(TokenType.COMMA):
                    break
        self._consume(TokenType.RPAREN, "Ожидается ')' после параметров функции.")
        self._consume(TokenType.LBRACE, "Ожидается '{' перед телом функции.")
        return FunctionStatement(keyword.line, keyword.column, name.value, parameters, BlockStatement(keyword.line, keyword.column, self._block()))

    def _var_declaration(self) -> VarStatement:
        keyword = self._previous()
        name = self._consume(TokenType.ID, "Ожидается имя переменной.")
        initializer = self._expression() if self._match(TokenType.EQ) else None
        self._consume(TokenType.SEMICOLON, "Ожидается ';' после объявления переменной.")
        return VarStatement(keyword.line, keyword.column, name.value, initializer)

    def _statement(self) -> Statement:
        if self._match(TokenType.IF):
            return self._if_statement()
        if self._match(TokenType.WHILE):
            return self._while_statement()
        if self._match(TokenType.PRINT):
            return self._print_statement()
        if self._match(TokenType.RETURN):
            return self._return_statement()
        if self._match(TokenType.LBRACE):
            token = self._previous()
            return BlockStatement(token.line, token.column, self._block())
        return self._expression_statement()

    def _if_statement(self) -> IfStatement:
        keyword = self._previous()
        self._consume(TokenType.LPAREN, "Ожидается '(' после 'if'.")
        condition = self._expression()
        self._consume(TokenType.RPAREN, "Ожидается ')' после условия 'if'.")
        then_branch = self._statement()
        else_branch = self._statement() if self._match(TokenType.ELSE) else None
        return IfStatement(keyword.line, keyword.column, condition, then_branch, else_branch)

    def _while_statement(self) -> WhileStatement:
        keyword = self._previous()
        self._consume(TokenType.LPAREN, "Ожидается '(' после 'while'.")
        condition = self._expression()
        self._consume(TokenType.RPAREN, "Ожидается ')' после условия 'while'.")
        return WhileStatement(keyword.line, keyword.column, condition, self._statement())

    def _print_statement(self) -> PrintStatement:
        keyword = self._previous()
        value = self._expression()
        self._consume(TokenType.SEMICOLON, "Ожидается ';' после значения.")
        return PrintStatement(keyword.line, keyword.column, value)

    def _return_statement(self) -> ReturnStatement:
        keyword = self._previous()
        value = None if self._check(TokenType.SEMICOLON) else self._expression()
        self._consume(TokenType.SEMICOLON, "Ожидается ';' после оператора return.")
        return ReturnStatement(keyword.line, keyword.column, value)

    def _expression_statement(self) -> ExpressionStatement:
        expr = self._expression()
        token = self._previous()
        self._consume(TokenType.SEMICOLON, "Ожидается ';' после выражения.")
        return ExpressionStatement(token.line, token.column, expr)

    def _block(self) -> list[Statement]:
        statements: list[Statement] = []
        while not self._check(TokenType.RBRACE) and not self._at_end:
            statements.append(self._declaration())
        self._consume(TokenType.RBRACE, "Ожидается '}' после блока.")
        return statements

    def _expression(self) -> Expression:
        return self._assignment()

    def _assignment(self) -> Expression:
        expr = self._logical_or()
        if self._match(TokenType.EQ):
            equals = self._previous()
            value = self._assignment()
            if isinstance(expr, VariableExpression):
                return AssignExpression(equals.line, equals.column, expr.name, value)
            if isinstance(expr, IndexExpression):
                return IndexAssignExpression(equals.line, equals.column, expr.array, expr.index, value)
            self._error(equals, "Недопустимая цель для присваивания.")
            raise ParseError()
        return expr

    def _logical_or(self) -> Expression:
        expr = self._logical_and()
        while self._match(TokenType.OR):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._logical_and())
        return expr

    def _logical_and(self) -> Expression:
        expr = self._equality()
        while self._match(TokenType.AND):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._equality())
        return expr

    def _equality(self) -> Expression:
        expr = self._comparison()
        while self._match(TokenType.EQEQ, TokenType.NEQ):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._comparison())
        return expr

    def _comparison(self) -> Expression:
        expr = self._term()
        while self._match(TokenType.LT, TokenType.LTEQ, TokenType.GT, TokenType.GTEQ):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._term())
        return expr

    def _term(self) -> Expression:
        expr = self._factor()
        while self._match(TokenType.PLUS, TokenType.MINUS):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._factor())
        return expr

    def _factor(self) -> Expression:
        expr = self._unary()
        while self._match(TokenType.STAR, TokenType.SLASH):
            op = self._previous()
            expr = BinaryExpression(op.line, op.column, expr, op.type, self._unary())
        return expr

    def _unary(self) -> Expression:
        if self._match(TokenType.EXCL, TokenType.MINUS):
            op = self._previous()
            return UnaryExpression(op.line, op.column, op.type, self._unary())
        return self._postfix()

    def _postfix(self) -> Expression:
        expr = self._primary()
        while True:
            if self._match(TokenType.LBRACKET):
                bracket = self._previous()
                index = self._expression()
                self._consume(TokenType.RBRACKET, "Ожидается ']' после индекса.")
                expr = IndexExpression(bracket.line, bracket.column, expr, index)
            elif self._match(TokenType.LPAREN):
                paren = self._previous()
                arguments: list[Expression] = []
                if not self._check(TokenType.RPAREN):
                    while True:
                        arguments.append(self._expression())
                        if not self._match(TokenType.COMMA):
                            break
                self._consume(TokenType.RPAREN, "Ожидается ')' после аргументов вызова.")
                if not isinstance(expr, VariableExpression):
                    self._error(paren, "Вызвать можно только функцию по имени.")
                    raise ParseError()
                expr = CallExpression(paren.line, paren.column, expr.name, arguments)
            else:
                return expr

    def _primary(self) -> Expression:
        if self._match(TokenType.NUMBER):
            token = self._previous()
            return NumberExpression(token.line, token.column, float(token.value))
        if self._match(TokenType.STRING):
            token = self._previous()
            return StringExpression(token.line, token.column, token.value)
        if self._match(TokenType.ID):
            token = self._previous()
            return VariableExpression(token.line, token.column, token.value)
        if self._match(TokenType.LBRACKET):
            bracket = self._previous()
            elements: list[Expression] = []
            if not self._check(TokenType.RBRACKET):
                while True:
                    elements.append(self._expression())
                    if not self._match(TokenType.COMMA):
                        break
            self._consume(TokenType.RBRACKET, "Ожидается ']' после элементов массива.")
            return ArrayExpression(bracket.line, bracket.column, elements)
        if self._match(TokenType.LPAREN):
            expr = self._expression()
            self._consume(TokenType.RPAREN, "Ожидается ')' после выражения.")
            return expr

        self._error(self._peek(), "Ожидается выражение.")
        raise ParseError()

    def _match(self, *types: TokenType) -> bool:
        for token_type in types:
            if self._check(token_type):
                self._advance()
                return True
        return False

    def _check(self, token_type: TokenType) -> bool:
        return not self._at_end and self._peek().type == token_type

    def _advance(self) -> Token:
        if not self._at_end:
            self.position += 1
        return self._previous()

    @property
    def _at_end(self) -> bool:
        return self._peek().type == TokenType.EOF

    def _peek(self) -> Token:
        return self.tokens[self.position]

    def _previous(self) -> Token:
        return self.tokens[self.position - 1]

    def _consume(self, token_type: TokenType, message: str) -> Token:
        if self._check(token_type):
            return self._advance()
        self._error(self._peek(), message)
        raise ParseError()

    def _error(self, token: Token, message: str) -> None:
        self.errors.append(f"[Parser Error] Line {token.line}, Col {token.column}: {message}")

    def _synchronize(self) -> None:
        self._advance()
        while not self._at_end:
            if self._previous().type == TokenType.SEMICOLON:
                return
            if self._peek().type in (TokenType.FUN, TokenType.VAR, TokenType.PRINT, TokenType.IF, TokenType.WHILE, TokenType.RETURN):
                return
            self._advance()
