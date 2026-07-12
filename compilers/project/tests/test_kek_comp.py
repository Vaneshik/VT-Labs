from __future__ import annotations

from contextlib import redirect_stdout
from io import StringIO

from kek_comp.cli import main
from kek_comp.interpreter import TreeInterpreter
from kek_comp.lexer import Lexer
from kek_comp.pipeline import analyze_source
from kek_comp.token import TokenType


def run(source: str) -> tuple[list[str], list[str], list[str]]:
    result = analyze_source(source)
    if result.parser_errors or result.semantic.errors:
        return [], result.parser_errors, result.semantic.errors
    interpretation = TreeInterpreter().interpret(result.statements)
    return interpretation.output, [], []


def test_lexer_skips_comments_and_tracks_tokens():
    tokens = Lexer('var x = 10; // comment\nprint x;').tokenize()
    types = [token.type for token in tokens]
    assert types == [
        TokenType.VAR,
        TokenType.ID,
        TokenType.EQ,
        TokenType.NUMBER,
        TokenType.SEMICOLON,
        TokenType.PRINT,
        TokenType.ID,
        TokenType.SEMICOLON,
        TokenType.EOF,
    ]
    assert tokens[5].line == 2


def test_arithmetic_logic_and_print():
    output, parser_errors, semantic_errors = run(
        """
        print "--- Arithmetic & Logic Test ---";
        var a = 10;
        var b = 20;
        var c = (a + b) * 2 - 10 / 2;
        print c;
        var logic1 = (a < b) && (b == 20);
        var logic2 = (a > b) || (b != 20);
        var logic3 = !logic2;
        print logic1;
        print logic2;
        print logic3;
        """
    )
    assert parser_errors == []
    assert semantic_errors == []
    assert output == ["--- Arithmetic & Logic Test ---", "55", "true", "false", "true"]


def test_control_flow_and_fibonacci():
    output, parser_errors, semantic_errors = run(
        """
        var n = 10;
        var a = 0;
        var b = 1;
        var i = 2;
        var temp;

        if (n == 0) {
            print a;
        } else {
            if (n == 1) {
                print b;
            } else {
                while (i <= n) {
                    temp = a + b;
                    a = b;
                    b = temp;
                    i = i + 1;
                }
                print b;
            }
        }
        """
    )
    assert parser_errors == []
    assert semantic_errors == []
    assert output == ["55"]


def test_functions_and_return():
    output, parser_errors, semantic_errors = run(
        """
        fun add(x, y) {
            return x + y;
        }
        var result = add(5, 10);
        print result;
        """
    )
    assert parser_errors == []
    assert semantic_errors == []
    assert output == ["15"]


def test_arrays_indexing_and_assignment():
    output, parser_errors, semantic_errors = run(
        """
        var words = ["kek", "python", "compiler"];
        var nums = [1, 2, 3];
        print words[0];
        nums[1] = 42;
        print nums;
        """
    )
    assert parser_errors == []
    assert semantic_errors == []
    assert output == ["kek", "[1, 42, 3]"]


def test_kek_builtin_prints_with_prefix():
    output, parser_errors, semantic_errors = run(
        """
        var memes = ["lexer", "parser", "semantic"];
        kek(memes[1]);
        """
    )
    assert parser_errors == []
    assert semantic_errors == []
    assert output == ["kek: parser"]


def test_tokens_mode_does_not_run_program():
    stream = StringIO()
    with redirect_stdout(stream):
        exit_code = main(["--tokens", "examples/arrays.kek"])
    text = stream.getvalue()
    assert exit_code == 0
    assert "LBRACKET '['" in text
    assert "[1, 9, 3]" not in text


def test_semantic_errors_from_lab_examples():
    result = analyze_source(
        """
        var age = 20;
        var name = "Ivan";
        var result = age - name;
        age = "Twenty";
        if (age + 5) {
            print "Age is good";
        }
        var logic = (10 > 5) && 42;
        """
    )
    text = "\n".join(result.semantic.errors)
    assert "оператор 'MINUS' работает только с числами" in text
    assert "нельзя присвоить значение типа String" in text
    assert "Условие 'if' должно быть логическим" in text
    assert "логические операторы" in text


def test_array_semantic_errors():
    result = analyze_source(
        """
        var values = [1, "two"];
        values["x"] = 3;
        values[0] = "bad";
        """
    )
    text = "\n".join(result.semantic.errors)
    assert "Все элементы массива должны быть одного типа" in text
    assert "Индекс массива должен быть числом" in text
    assert "нельзя записать значение типа String" in text
