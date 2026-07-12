from __future__ import annotations

from .errors import LexerError
from .token import Token, TokenType


KEYWORDS = {
    "var": TokenType.VAR,
    "print": TokenType.PRINT,
    "if": TokenType.IF,
    "else": TokenType.ELSE,
    "while": TokenType.WHILE,
    "fun": TokenType.FUN,
    "return": TokenType.RETURN,
}

OPERATORS = {
    "==": TokenType.EQEQ,
    "!=": TokenType.NEQ,
    "<=": TokenType.LTEQ,
    ">=": TokenType.GTEQ,
    "&&": TokenType.AND,
    "||": TokenType.OR,
    "+": TokenType.PLUS,
    "-": TokenType.MINUS,
    "*": TokenType.STAR,
    "/": TokenType.SLASH,
    "=": TokenType.EQ,
    "<": TokenType.LT,
    ">": TokenType.GT,
    "!": TokenType.EXCL,
    "(": TokenType.LPAREN,
    ")": TokenType.RPAREN,
    "{": TokenType.LBRACE,
    "}": TokenType.RBRACE,
    "[": TokenType.LBRACKET,
    "]": TokenType.RBRACKET,
    ";": TokenType.SEMICOLON,
    ",": TokenType.COMMA,
}


class Lexer:
    def __init__(self, source: str | None):
        self.source = source or ""
        self.position = 0
        self.line = 1
        self.column = 1

    def tokenize(self) -> list[Token]:
        tokens: list[Token] = []
        while not self._at_end:
            current = self._peek()
            if current.isspace():
                self._advance()
            elif current == "/" and self._peek_next() == "/":
                self._skip_comment()
            elif current.isdigit():
                tokens.append(self._read_number())
            elif current.isalpha() or current == "_":
                tokens.append(self._read_word())
            elif current == '"':
                tokens.append(self._read_string())
            else:
                tokens.append(self._read_operator())

        tokens.append(Token(TokenType.EOF, "\0", self.position, self.line, self.column))
        return tokens

    @property
    def _at_end(self) -> bool:
        return self.position >= len(self.source)

    def _peek(self) -> str:
        if self._at_end:
            return "\0"
        return self.source[self.position]

    def _peek_next(self) -> str:
        next_position = self.position + 1
        if next_position >= len(self.source):
            return "\0"
        return self.source[next_position]

    def _advance(self) -> str:
        if self._at_end:
            return "\0"
        char = self.source[self.position]
        self.position += 1
        if char == "\n":
            self.line += 1
            self.column = 1
        else:
            self.column += 1
        return char

    def _skip_comment(self) -> None:
        while self._peek() not in ("\n", "\0"):
            self._advance()

    def _read_number(self) -> Token:
        start = self._mark()
        while self._peek().isdigit():
            self._advance()
        if self._peek() == "." and self._peek_next().isdigit():
            self._advance()
            while self._peek().isdigit():
                self._advance()
        return self._token(TokenType.NUMBER, start)

    def _read_word(self) -> Token:
        start = self._mark()
        while self._peek().isalnum() or self._peek() == "_":
            self._advance()
        text = self.source[start[0] : self.position]
        return Token(KEYWORDS.get(text, TokenType.ID), text, *start)

    def _read_string(self) -> Token:
        start = self._mark()
        self._advance()
        chars: list[str] = []
        while self._peek() not in ('"', "\0"):
            if self._peek() == "\\":
                self._advance()
                chars.append(self._read_escape(start))
            else:
                chars.append(self._advance())

        if self._peek() == "\0":
            raise LexerError(f"[Lexer Error] Unterminated string at Line {start[1]}, Column {start[2]}")

        self._advance()
        return Token(TokenType.STRING, "".join(chars), *start)

    def _read_escape(self, start: tuple[int, int, int]) -> str:
        char = self._advance()
        escapes = {"n": "\n", "t": "\t", '"': '"', "\\": "\\"}
        if char in escapes:
            return escapes[char]
        raise LexerError(f"[Lexer Error] Unknown escape '\\{char}' at Line {start[1]}, Column {start[2]}")

    def _read_operator(self) -> Token:
        start = self._mark()
        two_chars = self.source[self.position : self.position + 2]
        if two_chars in OPERATORS:
            self._advance()
            self._advance()
            return Token(OPERATORS[two_chars], two_chars, *start)

        one_char = self._peek()
        if one_char in OPERATORS:
            self._advance()
            return Token(OPERATORS[one_char], one_char, *start)

        raise LexerError(f"[Lexer Error] Unexpected character '{one_char}' at Line {start[1]}, Column {start[2]}")

    def _mark(self) -> tuple[int, int, int]:
        return self.position, self.line, self.column

    def _token(self, token_type: TokenType, start: tuple[int, int, int]) -> Token:
        text = self.source[start[0] : self.position]
        return Token(token_type, text, *start)
