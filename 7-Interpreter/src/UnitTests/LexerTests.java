import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.LinkedList;
import java.util.stream.Stream;

import org.junit.Test;


public class LexerTests {
      // Lexer unittests


    @Test
    public void EmptyLex() throws Exception {
        var lexer = new Lexer("");
        assertEquals(lexer.lex(), new LinkedList<>());
    }

    public <T extends Throwable> void assertThrowsLexError(Class<T> expectedThrowable,
            String content) {
        // reason why it takes exception is b/c I was going to verify that error
        // messages match (but not doing that now b/c of AwkException)
        assertThrows(expectedThrowable, () -> UnitTests.testLexContent(content, new Token.TokenType[] {}));
    }

    @Test
    public void lexNewline() throws Exception {
        UnitTests.testLexContent("\r\n", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void BasicLex() throws Exception {
        UnitTests.testLexContent("111aAAA\taaazz\nZZ1Z.1zaaa", new Token.TokenType[] {
                Token.TokenType.NUMBER,
                Token.TokenType.WORD,
                Token.TokenType.WORD,
                Token.TokenType.SEPERATOR,
                Token.TokenType.WORD,
                Token.TokenType.NUMBER,
                Token.TokenType.WORD,
        });
    }

    @Test
    public void justAComent() throws Exception {
        UnitTests.testLexContent("# aaa dfdff", new Token.TokenType[] {});
    }

    @Test
    public void ComentFollowedByStuff() throws Exception {
        UnitTests.testLexContent("# aaa dfdff\n1. ax1 +\n { ) -;", new Token.TokenType[] {
                Token.TokenType.SEPERATOR,
                Token.TokenType.NUMBER,
                Token.TokenType.WORD,
                Token.TokenType.PLUS,
                Token.TokenType.SEPERATOR,
                Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEPAREN,
                Token.TokenType.MINUS,
                Token.TokenType.SEPERATOR,
        });
    }

    @Test
    public void LexDecimalNoNumber() throws Exception {
        assertThrowsLexError(AwkException.class, ".");
    }

    @Test
    public void LexUnderScoreWord() throws Exception {
        UnitTests.testLexContent("a_s", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void LexUnderScoreWordDot() throws Exception {
        UnitTests.testLexContent("a_ 5.", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.NUMBER });
    }

    @Test
    public void LexWeirdUnderScoreWord() throws Exception {
        UnitTests.testLexContent("a__a_a_a_s", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void LexWordWithAnyChar() throws Exception {
        Stream.iterate(0, n -> n < 256, n -> n + 1)
                .filter(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')).peek(System.out::println)
                .forEach(c -> {
                    try {
                        UnitTests.testLexContent(((Character) (char) (int) c).toString(),
                                new Token.TokenType[] { Token.TokenType.WORD });
                    } catch (Exception e) {
                        // should never happen
                        throw new RuntimeException(e);
                    }
                });

    }

    @Test
    public void LexUnderScoreWordWithStuff() throws Exception {
        UnitTests.testLexContent("a_s\r\n\t1234.5678 az__.5",
                new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                });
    }

    @Test
    public void LexDotDotDotEtc() throws Exception {
        assertThrowsLexError(AwkException.class, "....");
    }

    @Test
    public void LexWordedScheme() throws Exception {
        // (apply '(cons 1 5))
        UnitTests.testLexContent("open_paren apply quote open_paren cons 1 5 close_paren close_paren",
                new Token.TokenType[] {
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                });
    }

    @Test
    public void LexWordedSchemeFunction() throws Exception {
        // (define (list . l) l)
        UnitTests.testLexContent("open_paren\tdefine open_paren\rlist dot\r l close_paren l\tclose_paren",
                new Token.TokenType[] {
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                });
    }

    @Test
    public void LexWordedSchemeCons() throws Exception {
        // (define (cons x y)
        // (lambda (z)
        // (cond ((= z 0) x)
        // ((= z 1) y)
        // (else (error "cons not zero or one")))))
        UnitTests.testLexContent(
                "open_paren define open_paren cons x y close_paren\r\n\topen_paren lambda open_paren z close_paren\r\n\t\topen_paren cond open_paren open_paren equal z 0 close_paren x close_paren\r\n\t\t\topen_paren open_paren equal z 1 close_paren y close_paren\r\n\t\t\topen_paren else open_parent error quote cons not zero or one quote close_paren close_paren close_paren close_paren close_paren",
                new Token.TokenType[] {
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD,
                        Token.TokenType.ELSE,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,

                });
    }

    @Test
    public void LexNumberWithAnyDigitIsh() throws Exception {
        Stream.iterate(0, n -> n < 256, n -> n + 1)
                // we dont do `.` because it would attempt to lex an invalid number `.`
                .filter(c -> (c >= '0' && c <= '9')).peek(System.out::println)
                .forEach(c -> {
                    try {
                        UnitTests.testLexContent(((Character) (char) (int) c).toString(),
                                new Token.TokenType[] { Token.TokenType.NUMBER });
                    } catch (Exception e) {
                        // should never happen
                        throw new RuntimeException(e);
                    }
                });

    }

    @Test
    public void LexDecimalNumber() throws Exception {
        UnitTests.testLexContent("123.999", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void actualAwk() throws Exception {
        UnitTests.testLexContent("BEGIN {}",
                new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void actualAwkProgram() throws Exception {
        UnitTests.testLexContent(
                "BEGIN {FS=\",\"} # set field separator to `,` so it works for csv\n    {\n        sum  = 0 # reset sum for each line\n        for (i = 1; i <= NF; i++) sum += $i # sum up current line\n        total = sum + total # add sum of current line to total\n        print \"Line\", NR \":\", sum\n    }\nEND { print \"Grand total:\", total }",
                new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.OPENBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.LESSTHANEQUAL, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.WORD, Token.TokenType.PLUSEQUAL,
                        Token.TokenType.DOLLAR, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.WORD, Token.TokenType.PLUS,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR,
                        Token.TokenType.END, Token.TokenType.OPENBRACE, Token.TokenType.PRINT,
                        Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA, Token.TokenType.WORD,
                        Token.TokenType.CLOSEBRACE
                });
    }

    // chatjippty unit tests

    @Test
    public void testSingleWord() throws Exception {
        UnitTests.testLexContent("variable", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void testNumberWithWhitespace() throws Exception {
        UnitTests.testLexContent("  123  ", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void testStringLiteralWithQuotes() throws Exception {
        UnitTests.testLexContent("\"Hello, World!\"", new Token.TokenType[] { Token.TokenType.STRINGLITERAL });
    }

    @Test
    public void testPatternWithBackticks() throws Exception {
        UnitTests.testLexContent("`[0-9]+`", new Token.TokenType[] { Token.TokenType.PATTERN });
    }

    @Test
    public void testAssignment() throws Exception {
        UnitTests.testLexContent("x = 10;", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.ASSIGN,
                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR });
    }

    @Test
    public void testMultipleOperators() throws Exception {
        UnitTests.testLexContent("x += 10; y -= 5; z *= 2;", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.MULTIPLYEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR
        });
    }

    @Test
    public void testNestedBraces() throws Exception {
        UnitTests.testLexContent("{ { { } } }", new Token.TokenType[] {
                Token.TokenType.OPENBRACE, Token.TokenType.OPENBRACE, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE
        });
    }

    @Test
    public void testLogicalOperators() throws Exception {
        UnitTests.testLexContent("if (x > 5 && y < 10 || z == 3) { print \"True\" }", new Token.TokenType[] {
                Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.GREATERTHAN,
                Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.WORD, Token.TokenType.LESSTHAN,
                Token.TokenType.NUMBER, Token.TokenType.OR, Token.TokenType.WORD, Token.TokenType.EQUAL,
                Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.PRINT,
                Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEBRACE
        });
    }

    @Test
    public void testComment() throws Exception {
        UnitTests.testLexContent("x=5;# This is a comment\ny = 10;", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR
        });
    }

    @Test
    public void testBadlyFormattedProgram() throws Exception {
        UnitTests.testLexContent(
                "BEGIN\t {FS\t  =,\r}  {              sum += $1  \n\t\n    print \t \"Total: \", sum \t\t\t }                                        ",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.COMMA, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.PLUSEQUAL, Token.TokenType.DOLLAR, Token.TokenType.NUMBER,
                        Token.TokenType.SEPERATOR, Token.TokenType.SEPERATOR, Token.TokenType.PRINT,
                        Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA, Token.TokenType.WORD,
                        Token.TokenType.CLOSEBRACE,
                });
    }

    @Test
    public void testBadlyFormattedProgram1() throws Exception {
        UnitTests.testLexContent(
                "for( i = 1\t; i       <= NF ;\ti++ )  {  sum += $i                              \n total=sum    + total}\t",
                new Token.TokenType[] {
                        Token.TokenType.FOR, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                        Token.TokenType.WORD, Token.TokenType.LESSTHANEQUAL, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.PLUSPLUS,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.PLUSEQUAL, Token.TokenType.DOLLAR, Token.TokenType.WORD,
                        Token.TokenType.SEPERATOR, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.WORD, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.CLOSEBRACE
                });
    }

    @Test
    public void testBadlyFormattedProgram2() throws Exception {
        UnitTests.testLexContent("if\t(i\t>\t10)\t{print\t\"i is greater than 10\"}\nelse\n{print \"i is not greater than 10\"}",
                new Token.TokenType[] {
                        Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD,
                        Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.SEPERATOR, Token.TokenType.ELSE,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.OPENBRACE, Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.CLOSEBRACE
                });
    }

    @Test
    public void testInvalidCharacter() throws Exception {
        assertThrowsLexError(AwkException.class, "@"); // Invalid character
    }

    @Test
    public void testUnmatchedBacktick() throws Exception {
        assertThrowsLexError(AwkException.class, "`[0-9]+"); // Unmatched backtick
    }

    @Test
    public void testIncompleteStringLiteral() throws Exception {
        assertThrowsLexError(AwkException.class, "\"Incomplete String"); // Incomplete string literal
    }

    @Test
    public void testSchemeCode() throws Exception {
        UnitTests.testLexContent("(define (add a b) (+ a b))", new Token.TokenType[] {
                Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN,
                Token.TokenType.CLOSEPAREN
        });
    }

    @Test
    public void testMixedSchemeAndAwkCode() throws Exception {
        UnitTests.testLexContent("BEGIN {FS=,}  { (define (add a b) (+ a b)) print \"Total: \", add(NF, NS)  }",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.COMMA, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.OPENBRACE,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.CLOSEBRACE,

                });
    }

    @Test
    public void testMoreSchemeCode() throws Exception {
        UnitTests.testLexContent("(if (> x 10) (* x 2) (+ x 1))", new Token.TokenType[] {
                Token.TokenType.OPENPAREN, Token.TokenType.IF, Token.TokenType.OPENPAREN,
                Token.TokenType.GREATERTHAN, Token.TokenType.WORD, Token.TokenType.NUMBER,
                Token.TokenType.CLOSEPAREN, Token.TokenType.OPENPAREN, Token.TokenType.MULTIPLY,
                Token.TokenType.WORD, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.NUMBER,
                Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEPAREN
        });
    }

    @Test
    public void testMoreMixedCode() throws Exception {
        UnitTests.testLexContent("BEGIN {FS=,}  { x = (if (> x 10) (* x 2) (+ x 1)); print \"Result: \", x  }",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.WORD,
                        Token.TokenType.ASSIGN, Token.TokenType.COMMA, Token.TokenType.CLOSEBRACE,
                        Token.TokenType.OPENBRACE, Token.TokenType.WORD, Token.TokenType.ASSIGN,
                        Token.TokenType.OPENPAREN, Token.TokenType.IF, Token.TokenType.OPENPAREN,
                        Token.TokenType.GREATERTHAN, Token.TokenType.WORD, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.OPENPAREN, Token.TokenType.MULTIPLY,
                        Token.TokenType.WORD, Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL, Token.TokenType.COMMA,
                        Token.TokenType.WORD,
                        Token.TokenType.CLOSEBRACE,
                });
    }

    @Test
    public void testOpenBrace() throws Exception {
        UnitTests.testLexContent("{", new Token.TokenType[] { Token.TokenType.OPENBRACE });
    }

    @Test
    public void testCloseBrace() throws Exception {
        UnitTests.testLexContent("}", new Token.TokenType[] { Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void testOpenBracket() throws Exception {
        UnitTests.testLexContent("[", new Token.TokenType[] { Token.TokenType.OPENBRACKET });
    }

    @Test
    public void testCloseBracket() throws Exception {
        UnitTests.testLexContent("]", new Token.TokenType[] { Token.TokenType.CLOSEBRACKET });
    }

    @Test
    public void testOpenParen() throws Exception {
        UnitTests.testLexContent("(", new Token.TokenType[] { Token.TokenType.OPENPAREN });
    }

    @Test
    public void testCloseParen() throws Exception {
        UnitTests.testLexContent(")", new Token.TokenType[] { Token.TokenType.CLOSEPAREN });
    }

    @Test
    public void testDollar() throws Exception {
        UnitTests.testLexContent("$", new Token.TokenType[] { Token.TokenType.DOLLAR });
    }

    @Test
    public void testMatch() throws Exception {
        UnitTests.testLexContent("~", new Token.TokenType[] { Token.TokenType.MATCH });
    }

    @Test
    public void testAssign() throws Exception {
        UnitTests.testLexContent("=", new Token.TokenType[] { Token.TokenType.ASSIGN });
    }

    @Test
    public void testLessThan() throws Exception {
        UnitTests.testLexContent("<", new Token.TokenType[] { Token.TokenType.LESSTHAN });
    }

    @Test
    public void testGreaterThan() throws Exception {
        UnitTests.testLexContent(">", new Token.TokenType[] { Token.TokenType.GREATERTHAN });
    }

    @Test
    public void testNot() throws Exception {
        UnitTests.testLexContent("!", new Token.TokenType[] { Token.TokenType.NOT });
    }

    @Test
    public void testPlus() throws Exception {
        UnitTests.testLexContent("+", new Token.TokenType[] { Token.TokenType.PLUS });
    }

    @Test
    public void testExponent() throws Exception {
        UnitTests.testLexContent("^", new Token.TokenType[] { Token.TokenType.EXPONENT });
    }

    @Test
    public void testMinus() throws Exception {
        UnitTests.testLexContent("-", new Token.TokenType[] { Token.TokenType.MINUS });
    }

    @Test
    public void testQuestion() throws Exception {
        UnitTests.testLexContent("?", new Token.TokenType[] { Token.TokenType.QUESTION });
    }

    @Test
    public void testColon() throws Exception {
        UnitTests.testLexContent(":", new Token.TokenType[] { Token.TokenType.COLON });
    }

    @Test
    public void testMultiply() throws Exception {
        UnitTests.testLexContent("*", new Token.TokenType[] { Token.TokenType.MULTIPLY });
    }

    @Test
    public void testDivide() throws Exception {
        UnitTests.testLexContent("/", new Token.TokenType[] { Token.TokenType.DIVIDE });
    }

    @Test
    public void testModulo() throws Exception {
        UnitTests.testLexContent("%", new Token.TokenType[] { Token.TokenType.MODULO });
    }

    @Test
    public void testSemicolon() throws Exception {
        UnitTests.testLexContent(";", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void testNewline() throws Exception {
        UnitTests.testLexContent("\n", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void testVerticalBar() throws Exception {
        UnitTests.testLexContent("|", new Token.TokenType[] { Token.TokenType.VERTICALBAR });
    }

    @Test
    public void testComma() throws Exception {
        UnitTests.testLexContent(",", new Token.TokenType[] { Token.TokenType.COMMA });
    }

    @Test
    public void testGreaterThanOrEqual() throws Exception {
        UnitTests.testLexContent(">=", new Token.TokenType[] { Token.TokenType.GREATERTHANEQUAL });
    }

    @Test
    public void testPlusPlus() throws Exception {
        UnitTests.testLexContent("++", new Token.TokenType[] { Token.TokenType.PLUSPLUS });
    }

    @Test
    public void testMinusMinus() throws Exception {
        UnitTests.testLexContent("--", new Token.TokenType[] { Token.TokenType.MINUSMINUS });
    }

    @Test
    public void testLessThanOrEqual() throws Exception {
        UnitTests.testLexContent("<=", new Token.TokenType[] { Token.TokenType.LESSTHANEQUAL });
    }

    @Test
    public void testEqualEqual() throws Exception {
        UnitTests.testLexContent("==", new Token.TokenType[] { Token.TokenType.EQUAL });
    }

    @Test
    public void testNotEqual() throws Exception {
        UnitTests.testLexContent("!=", new Token.TokenType[] { Token.TokenType.NOTEQUAL });
    }

    @Test
    public void testExponentEqual() throws Exception {
        UnitTests.testLexContent("^=", new Token.TokenType[] { Token.TokenType.EXPONENTEQUAL });
    }

    @Test
    public void testModuloEqual() throws Exception {
        UnitTests.testLexContent("%=", new Token.TokenType[] { Token.TokenType.MODULOEQUAL });
    }

    @Test
    public void testMultiplyEqual() throws Exception {
        UnitTests.testLexContent("*=", new Token.TokenType[] { Token.TokenType.MULTIPLYEQUAL });
    }

    @Test
    public void testDivideEqual() throws Exception {
        UnitTests.testLexContent("/=", new Token.TokenType[] { Token.TokenType.DIVIDEEQUAL });
    }

    @Test
    public void testPlusEqual() throws Exception {
        UnitTests.testLexContent("+=", new Token.TokenType[] { Token.TokenType.PLUSEQUAL });
    }

    @Test
    public void testMinusEqual() throws Exception {
        UnitTests.testLexContent("-=", new Token.TokenType[] { Token.TokenType.MINUSEQUAL });
    }

    @Test
    public void testNotMatch() throws Exception {
        UnitTests.testLexContent("!~", new Token.TokenType[] { Token.TokenType.NOTMATCH });
    }

    @Test
    public void testLogicalAnd() throws Exception {
        UnitTests.testLexContent("&&", new Token.TokenType[] { Token.TokenType.AND });
    }

    @Test
    public void testShiftRight() throws Exception {
        UnitTests.testLexContent(">>", new Token.TokenType[] { Token.TokenType.APPEND });
    }

    @Test
    public void testLogicalOr() throws Exception {
        UnitTests.testLexContent("||", new Token.TokenType[] { Token.TokenType.OR });
    }

    @Test
    public void invalidNewlineString() throws Exception {
        assertThrowsLexError(AwkException.class, """
                aaaa "
                "
                """);
    }
}
