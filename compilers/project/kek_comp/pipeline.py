from __future__ import annotations

from dataclasses import dataclass

from .ast_nodes import Statement
from .lexer import Lexer
from .parser import Parser
from .semantic import SemanticAnalyzer, SemanticResult
from .token import Token


@dataclass
class PipelineResult:
    tokens: list[Token]
    statements: list[Statement]
    parser_errors: list[str]
    semantic: SemanticResult


def analyze_source(source: str) -> PipelineResult:
    tokens = Lexer(source).tokenize()
    parser = Parser(tokens)
    statements = parser.parse()
    semantic = SemanticAnalyzer().analyze(statements) if not parser.errors else SemanticResult([], [])
    return PipelineResult(tokens, statements, parser.errors, semantic)
