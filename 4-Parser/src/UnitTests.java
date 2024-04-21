import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

public class UnitTests {
    // use get_awk_files.py, make sure to have root/tests directory
    // amd that the junit tests run in the root directory
    // in vscode use "java.test.config": {
    // "workingDirectory": "${workspaceFolder}"
    // }, in settings.json
    // to pull random awk code from the internet
    //
    // NOTE: about this test:
    // a) it does not mean the lexer works b/c
    // 1) if a test fails, the source file could be invalid we have no way of
    // verfiying a file
    // 2) if it doesnt fail who says lexer is correct as we have no way of verfiying
    // a file
    // b) this simple file = file.replaceAll("/", "`"); messes with with division,
    // even though it fixes regex pattern
    public Stream<String> get_awk_files() throws Exception {
        return Files.list(Paths.get("tests")).filter(file -> {
            try {
                new String(Files.readAllBytes(file));
                return true;
            } catch (IOException e) {
                return false;
            }
        }).map(file -> {
            try {
                return new String(Files.readAllBytes(file));
            } catch (IOException e) {
                return "";
            }
        });
    }

    public void assertWorks(String file) {
        file = file.replaceAll("/", "`");
        var lexer = new Lexer(file);
        var fpLexer = new FunctionalLexer(file);
        try {
            lexer.lex();
            fpLexer.lex();
        } catch (AwkException e) {
            System.out.println("error lexing file\n" + file + "\n" + e.message);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void TestRandomAwkFile() throws Exception {
        get_awk_files().forEach(this::assertWorks);
    }

    private static Random rng = new Random();
    private boolean debug = false;

    // StringHandler unittests
    @Test
    public void HandleLEmptyString() throws Exception {
        var handler = new StringHandler("");
        assertEquals(handler.IsDone(), true);
    }

    public void fuzz_string_handler(String input, int numberOfOperations) throws Exception {
        int length = input.length();
        int position = 0;
        if (debug) {
            System.out.println("Fuzzing " + numberOfOperations + "\nlength " + length + "\n" + input);
        }
        StringHandler handler = new StringHandler(input);
        for (int i = 0; i < numberOfOperations; i++) {

            int op = rng.nextInt(6);
            if (debug) {
                System.out.println("position:" + position + " op:" + op + " iteration:" + i);
            }
            switch (op) {
                // Peek
                case 0:
                    if (position == length && handler.IsDone()) {
                        break;
                    } else if (handler.IsDone()) {
                        throw new IllegalStateException("StringHandler claims to be done, but it still has "
                                + (length - position) + " characters remaining");
                    }
                    int index = rng.nextInt(length - position);
                    char peek = handler.Peek(index);
                    char check_peek = handler.Peek(index);
                    // check if StringHandler moved position
                    assertEquals(peek, check_peek);
                    assertEquals(position, handler.getPosition());
                    break;
                // PeekString
                case 1:
                    if (position == length && handler.IsDone()) {
                        break;
                    } else if (handler.IsDone()) {
                        throw new IllegalStateException("StringHandler claims to be done, but it still has "
                                + (length - position) + " characters remaining");
                    }
                    index = rng.nextInt(length - position);
                    String peekString = handler.PeekString(index);
                    String check_peekString = handler.PeekString(index);
                    // check if StringHandler moved position
                    assertEquals(peekString, check_peekString);
                    assertEquals(position, handler.getPosition());
                    break;
                // GetChar
                case 2:
                    // need to check if were at end of string b/c for all other tests we choose
                    // index s.t. it should be within range of length of input
                    // but getchar and peek dont give us tht control
                    if (position == length && handler.IsDone()) {
                        break;
                    } else if (handler.IsDone()) {
                        throw new IllegalStateException("StringHandler claims to be done, but it still has "
                                + (length - position) + " characters remaining");
                    }
                    handler.GetChar();
                    position++;
                    assertEquals(position, handler.getPosition());
                    if (position == length && handler.IsDone()) {
                        break;
                    } else if (handler.IsDone()) {
                        throw new IllegalStateException("StringHandler claims to be done, but it still has "
                                + (length - position) + " characters remaining");
                    }

                    break;
                // Swallow
                case 3:
                    if (position == length && handler.IsDone()) {
                        break;
                    } else if (handler.IsDone()) {
                        throw new IllegalStateException("StringHandler claims to be done, but it still has "
                                + (length - position) + " characters remaining");
                    }
                    index = rng.nextInt(length - position);
                    handler.Swallow(index);
                    position += index;
                    check_peek = handler.Peek();
                    char nextChar = input.charAt(position);
                    assertEquals(check_peek, nextChar);
                    assertEquals(position, handler.getPosition());
                    break;
                // IsDone
                case 4:
                    assertEquals((position == length), handler.IsDone());
                    break;
                // Remainder
                case 5:
                    String remainder = handler.Remainder();
                    String remainderCheck = input.substring(position);
                    assertEquals(remainder, remainderCheck);
                    assertEquals(handler.IsDone(), true);
                    position = length;
                    break;

                default:
            }
        }
    }

    @Test
    public void HandleLoremIpsum() throws Exception {
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        fuzz_string_handler(loremIpsum, 50);
    }

    public static String StringGenerator(int length) {
        byte[] bytes = new byte[length];
        rng.nextBytes(bytes);
        return new String(bytes, Charset.forName("UTF-8"));
    }

    @Test
    public void Fuzzer() throws Exception {
        int iterations = rng.nextInt(1_000);
        if (debug)
            System.out.println("iterations = " + iterations);
        for (int i = 0; i < iterations; i++) {
            int numberOfOperations = rng.nextInt(10_000);
            String string = StringGenerator(rng.nextInt(1, 100_000));
            fuzz_string_handler(string, numberOfOperations);
        }
    }

    // Lexer unittests
    public LinkedList<Token> testLexContent(String content, Token.TokenType[] expected) throws Exception {
        var lexer = new Lexer(content);
        var otherLexer = new FunctionalLexer(content);
        var lexed = lexer.lex();
        var otherLexed = otherLexer.lex();
        var lexedTokens = lexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
        var otherLexedTokens = otherLexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
        assertArrayEquals("lexer", expected, lexedTokens);
        assertArrayEquals("functional lexer", expected, otherLexedTokens);
        return lexed;

    }

    @Test
    public void EmptyLex() throws Exception {
        var lexer = new Lexer("");
        assertEquals(lexer.lex(), new LinkedList<>());
    }

    public <T extends Throwable> void assertThrowsLexError(Class<T> expectedThrowable,
            String content) {
        // reason why it takes exception is b/c I was going to verify that error
        // messages match (but not doing that now b/c of AwkException)
        assertThrows(expectedThrowable, () -> testLexContent(content, new Token.TokenType[] {}));
    }

    @Test
    public void lexNewline() throws Exception {
        testLexContent("\r\n", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void BasicLex() throws Exception {
        testLexContent("111aAAA\taaazz\nZZ1Z.1zaaa", new Token.TokenType[] {
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
        testLexContent("# aaa dfdff", new Token.TokenType[] {});
    }

    @Test
    public void ComentFollowedByStuff() throws Exception {
        testLexContent("# aaa dfdff\n1. ax1 +\n { ) -;", new Token.TokenType[] {
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
        testLexContent("a_s", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void LexUnderScoreWordDot() throws Exception {
        testLexContent("a_ 5.", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.NUMBER });
    }

    @Test
    public void LexWeirdUnderScoreWord() throws Exception {
        testLexContent("a__a_a_a_s", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void LexWordWithAnyChar() throws Exception {
        Stream.iterate(0, n -> n < 256, n -> n + 1)
                .filter(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')).peek(System.out::println)
                .forEach(c -> {
                    try {
                        testLexContent(((Character) (char) (int) c).toString(),
                                new Token.TokenType[] { Token.TokenType.WORD });
                    } catch (Exception e) {
                        // should never happen
                        throw new RuntimeException(e);
                    }
                });

    }

    @Test
    public void LexUnderScoreWordWithStuff() throws Exception {
        testLexContent("a_s\r\n\t1234.5678 az__.5",
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
        testLexContent("open_paren apply quote open_paren cons 1 5 close_paren close_paren",
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
        testLexContent("open_paren\tdefine open_paren\rlist dot\r l close_paren l\tclose_paren",
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
        testLexContent(
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
                        testLexContent(((Character) (char) (int) c).toString(),
                                new Token.TokenType[] { Token.TokenType.NUMBER });
                    } catch (Exception e) {
                        // should never happen
                        throw new RuntimeException(e);
                    }
                });

    }

    @Test
    public void LexDecimalNumber() throws Exception {
        testLexContent("123.999", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void actualAwk() throws Exception {
        testLexContent("BEGIN {}",
                new Token.TokenType[] { Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void actualAwkProgram() throws Exception {
        testLexContent(
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
        testLexContent("variable", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void testNumberWithWhitespace() throws Exception {
        testLexContent("  123  ", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void testStringLiteralWithQuotes() throws Exception {
        testLexContent("\"Hello, World!\"", new Token.TokenType[] { Token.TokenType.STRINGLITERAL });
    }

    @Test
    public void testPatternWithBackticks() throws Exception {
        testLexContent("`[0-9]+`", new Token.TokenType[] { Token.TokenType.PATTERN });
    }

    @Test
    public void testAssignment() throws Exception {
        testLexContent("x = 10;", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.ASSIGN,
                Token.TokenType.NUMBER, Token.TokenType.SEPERATOR });
    }

    @Test
    public void testMultipleOperators() throws Exception {
        testLexContent("x += 10; y -= 5; z *= 2;", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.PLUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.MINUSEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.MULTIPLYEQUAL, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR
        });
    }

    @Test
    public void testNestedBraces() throws Exception {
        testLexContent("{ { { } } }", new Token.TokenType[] {
                Token.TokenType.OPENBRACE, Token.TokenType.OPENBRACE, Token.TokenType.OPENBRACE,
                Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE
        });
    }

    @Test
    public void testLogicalOperators() throws Exception {
        testLexContent("if (x > 5 && y < 10 || z == 3) { print \"True\" }", new Token.TokenType[] {
                Token.TokenType.IF, Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.GREATERTHAN,
                Token.TokenType.NUMBER, Token.TokenType.AND, Token.TokenType.WORD, Token.TokenType.LESSTHAN,
                Token.TokenType.NUMBER, Token.TokenType.OR, Token.TokenType.WORD, Token.TokenType.EQUAL,
                Token.TokenType.NUMBER, Token.TokenType.CLOSEPAREN, Token.TokenType.OPENBRACE, Token.TokenType.PRINT,
                Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEBRACE
        });
    }

    @Test
    public void testComment() throws Exception {
        testLexContent("x=5;# This is a comment\ny = 10;", new Token.TokenType[] {
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR,
                Token.TokenType.SEPERATOR,
                Token.TokenType.WORD, Token.TokenType.ASSIGN, Token.TokenType.NUMBER, Token.TokenType.SEPERATOR
        });
    }

    @Test
    public void testBadlyFormattedProgram() throws Exception {
        testLexContent(
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
        testLexContent(
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
        testLexContent("if\t(i\t>\t10)\t{print\t\"i is greater than 10\"}\nelse\n{print \"i is not greater than 10\"}",
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
        testLexContent("(define (add a b) (+ a b))", new Token.TokenType[] {
                Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                Token.TokenType.OPENPAREN, Token.TokenType.PLUS, Token.TokenType.WORD, Token.TokenType.WORD,
                Token.TokenType.CLOSEPAREN,
                Token.TokenType.CLOSEPAREN
        });
    }

    @Test
    public void testMixedSchemeAndAwkCode() throws Exception {
        testLexContent("BEGIN {FS=,}  { (define (add a b) (+ a b)) print \"Total: \", add(NF, NS)  }",
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
        testLexContent("(if (> x 10) (* x 2) (+ x 1))", new Token.TokenType[] {
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
        testLexContent("BEGIN {FS=,}  { x = (if (> x 10) (* x 2) (+ x 1)); print \"Result: \", x  }",
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
        testLexContent("{", new Token.TokenType[] { Token.TokenType.OPENBRACE });
    }

    @Test
    public void testCloseBrace() throws Exception {
        testLexContent("}", new Token.TokenType[] { Token.TokenType.CLOSEBRACE });
    }

    @Test
    public void testOpenBracket() throws Exception {
        testLexContent("[", new Token.TokenType[] { Token.TokenType.OPENBRACKET });
    }

    @Test
    public void testCloseBracket() throws Exception {
        testLexContent("]", new Token.TokenType[] { Token.TokenType.CLOSEBRACKET });
    }

    @Test
    public void testOpenParen() throws Exception {
        testLexContent("(", new Token.TokenType[] { Token.TokenType.OPENPAREN });
    }

    @Test
    public void testCloseParen() throws Exception {
        testLexContent(")", new Token.TokenType[] { Token.TokenType.CLOSEPAREN });
    }

    @Test
    public void testDollar() throws Exception {
        testLexContent("$", new Token.TokenType[] { Token.TokenType.DOLLAR });
    }

    @Test
    public void testMatch() throws Exception {
        testLexContent("~", new Token.TokenType[] { Token.TokenType.MATCH });
    }

    @Test
    public void testAssign() throws Exception {
        testLexContent("=", new Token.TokenType[] { Token.TokenType.ASSIGN });
    }

    @Test
    public void testLessThan() throws Exception {
        testLexContent("<", new Token.TokenType[] { Token.TokenType.LESSTHAN });
    }

    @Test
    public void testGreaterThan() throws Exception {
        testLexContent(">", new Token.TokenType[] { Token.TokenType.GREATERTHAN });
    }

    @Test
    public void testNot() throws Exception {
        testLexContent("!", new Token.TokenType[] { Token.TokenType.NOT });
    }

    @Test
    public void testPlus() throws Exception {
        testLexContent("+", new Token.TokenType[] { Token.TokenType.PLUS });
    }

    @Test
    public void testExponent() throws Exception {
        testLexContent("^", new Token.TokenType[] { Token.TokenType.EXPONENT });
    }

    @Test
    public void testMinus() throws Exception {
        testLexContent("-", new Token.TokenType[] { Token.TokenType.MINUS });
    }

    @Test
    public void testQuestion() throws Exception {
        testLexContent("?", new Token.TokenType[] { Token.TokenType.QUESTION });
    }

    @Test
    public void testColon() throws Exception {
        testLexContent(":", new Token.TokenType[] { Token.TokenType.COLON });
    }

    @Test
    public void testMultiply() throws Exception {
        testLexContent("*", new Token.TokenType[] { Token.TokenType.MULTIPLY });
    }

    @Test
    public void testDivide() throws Exception {
        testLexContent("/", new Token.TokenType[] { Token.TokenType.DIVIDE });
    }

    @Test
    public void testModulo() throws Exception {
        testLexContent("%", new Token.TokenType[] { Token.TokenType.MODULO });
    }

    @Test
    public void testSemicolon() throws Exception {
        testLexContent(";", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void testNewline() throws Exception {
        testLexContent("\n", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void testVerticalBar() throws Exception {
        testLexContent("|", new Token.TokenType[] { Token.TokenType.VERTICALBAR });
    }

    @Test
    public void testComma() throws Exception {
        testLexContent(",", new Token.TokenType[] { Token.TokenType.COMMA });
    }

    @Test
    public void testGreaterThanOrEqual() throws Exception {
        testLexContent(">=", new Token.TokenType[] { Token.TokenType.GREATERTHANEQUAL });
    }

    @Test
    public void testPlusPlus() throws Exception {
        testLexContent("++", new Token.TokenType[] { Token.TokenType.PLUSPLUS });
    }

    @Test
    public void testMinusMinus() throws Exception {
        testLexContent("--", new Token.TokenType[] { Token.TokenType.MINUSMINUS });
    }

    @Test
    public void testLessThanOrEqual() throws Exception {
        testLexContent("<=", new Token.TokenType[] { Token.TokenType.LESSTHANEQUAL });
    }

    @Test
    public void testEqualEqual() throws Exception {
        testLexContent("==", new Token.TokenType[] { Token.TokenType.EQUAL });
    }

    @Test
    public void testNotEqual() throws Exception {
        testLexContent("!=", new Token.TokenType[] { Token.TokenType.NOTEQUAL });
    }

    @Test
    public void testExponentEqual() throws Exception {
        testLexContent("^=", new Token.TokenType[] { Token.TokenType.EXPONENTEQUAL });
    }

    @Test
    public void testModuloEqual() throws Exception {
        testLexContent("%=", new Token.TokenType[] { Token.TokenType.MODULOEQUAL });
    }

    @Test
    public void testMultiplyEqual() throws Exception {
        testLexContent("*=", new Token.TokenType[] { Token.TokenType.MULTIPLYEQUAL });
    }

    @Test
    public void testDivideEqual() throws Exception {
        testLexContent("/=", new Token.TokenType[] { Token.TokenType.DIVIDEEQUAL });
    }

    @Test
    public void testPlusEqual() throws Exception {
        testLexContent("+=", new Token.TokenType[] { Token.TokenType.PLUSEQUAL });
    }

    @Test
    public void testMinusEqual() throws Exception {
        testLexContent("-=", new Token.TokenType[] { Token.TokenType.MINUSEQUAL });
    }

    @Test
    public void testNotMatch() throws Exception {
        testLexContent("!~", new Token.TokenType[] { Token.TokenType.NOTMATCH });
    }

    @Test
    public void testLogicalAnd() throws Exception {
        testLexContent("&&", new Token.TokenType[] { Token.TokenType.AND });
    }

    @Test
    public void testShiftRight() throws Exception {
        testLexContent(">>", new Token.TokenType[] { Token.TokenType.APPEND });
    }

    @Test
    public void testLogicalOr() throws Exception {
        testLexContent("||", new Token.TokenType[] { Token.TokenType.OR });
    }

    @Test
    public void invalidNewlineString() throws Exception {
        assertThrowsLexError(AwkException.class, """
                aaaa "
                "
                """);
    }

    // parser tests
    // TokenHandler tests
    public void TokenHandlerFuzzer(LinkedList<Token> tokens, int numberOfOperations) {
        var handler = new TokenHandler(tokens);
        // deep clone via linkedlist b/c matchandremove modifies the original list
        tokens = new LinkedList<Token>(tokens);

        for (int i = 0; i < numberOfOperations; i++) {
            if (tokens.size() == 0) {
                assertEquals(handler.MoreTokens(), false);
                if (debug) {
                    System.out.println("finished tokens after " + i + " operation(s)");
                }
                break;
            }
            int op = rng.nextInt(5);
            switch (op) {
                // peek
                case 0:
                    int index = tokens.size() - 1 != 0 ? rng.nextInt(0, tokens.size() - 1) : 0;
                    Optional<Token> token = Optional.of(tokens.get(index));
                    assertEquals(handler.Peek(index), token);
                    break;

                // matchremove with actual token
                case 1:
                    var nextToken = tokens.getFirst();
                    assertEquals(handler.MatchAndRemove(nextToken.getType()).get().getType(), nextToken.getType());
                    tokens.pop();
                    break;

                // matchremove with mismatch token
                case 2:
                    nextToken = tokens.getFirst();
                    Supplier<Token.TokenType> new_tt = () -> {
                        int tt_index = rng.nextInt(3);
                        return tt_index == 0 ? Token.TokenType.AND
                                : tt_index == 1 ? Token.TokenType.OR : Token.TokenType.CONTINUE;
                    };
                    Token.TokenType not_match;
                    do {
                        not_match = new_tt.get();
                    } while (not_match == nextToken.getType());
                    assertEquals(handler.MatchAndRemove(not_match), Optional.empty());
                    break;
                // isDone
                case 3:
                    assertEquals(handler.MoreTokens(), true);
                    break;
                // peek ahead of end
                case 4:
                    index = tokens.size();
                    assertEquals(handler.Peek(index), Optional.empty());
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void fuzz_tokens() throws Exception {
        TokenHandlerFuzzer(new LinkedList<>() {
            {
                add(new Token(0, 0, Token.TokenType.AND));
            }
        }, 1);
    }

    @Test
    public void fuzz_tokens_100() throws Exception {
        var tokens = testLexContent(
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
        TokenHandlerFuzzer(tokens, 100);
    }

    @Test
    public void fuzz_tokens_1000() throws Exception {
        var tokens = testLexContent(
                "BEGIN {FS=\",\"} # set field separator to `,` so it works for csv\n    {\n        sum  = 0 # reset sum for each line\n        for (i = 1; i <= NF; i++) sum += $i # sum up current line\n        total = sum + total # add sum of current line to total\n        print \"Line\", NR \":\", sum\n    }\nEND { print \"Grand total:\", total } BEGIN {FS=,}  { x = (if (> x 10) (* x 2) (+ x 1)); print \"Result: \", x  }",
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
                        Token.TokenType.CLOSEBRACE,
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
        TokenHandlerFuzzer(tokens, 1000);
    }

    @Test
    public void testTokenHandler() throws Exception {
        var tokens = new TokenHandler(testLexContent("BEGIN \n {} a == 5 {print \"a is 5\"}", new Token.TokenType[] {
                Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.OPENBRACE,
                Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEBRACE
        }));

        // simulate parsing
        assertEquals(tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.SEPERATOR).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);

        if (!tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent()) {
            assertEquals(tokens.MatchAndRemove(Token.TokenType.WORD).isPresent(), true);
            if (tokens.Peek(0).map(Token::getType) != Optional.of(Token.TokenType.NOTEQUAL)
                    && (tokens.Peek(0).map(Token::getType) != Optional.of(Token.TokenType.NUMBER))) {
                assertEquals(tokens.MatchAndRemove(Token.TokenType.EQUAL).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.NUMBER).isPresent(), true);
            }
        }
        assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);
        if (tokens.Peek(0).map(Token::getType) != Optional.of(Token.TokenType.PRINT)
                && (tokens.Peek(0).map(Token::getType) != Optional.of(Token.TokenType.STRINGLITERAL))) {
            assertEquals(tokens.MatchAndRemove(Token.TokenType.PRINT).isPresent(), true);
            assertEquals(tokens.MatchAndRemove(Token.TokenType.STRINGLITERAL).isPresent(), true);
        }
        assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);

    }

    @Test
    public void testTokenHandler1() throws Exception {
        var tokens = new TokenHandler(testLexContent("BEGIN \n {} a == 5 {print \"a is 5\"}", new Token.TokenType[] {
                Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.OPENBRACE, Token.TokenType.CLOSEBRACE,
                Token.TokenType.WORD, Token.TokenType.EQUAL, Token.TokenType.NUMBER, Token.TokenType.OPENBRACE,
                Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL, Token.TokenType.CLOSEBRACE
        }));

        // Test matching and removing tokens
        assertEquals(tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.SEPERATOR).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);
        assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);

        // Test using Peek and MoreTokens
        while (tokens.MoreTokens()) {
            if (tokens.Peek(0).map(Token::getType).orElse(null) != Token.TokenType.BEGIN) {
                assertEquals(tokens.MatchAndRemove(Token.TokenType.WORD).isPresent(), true);
                if (!tokens.Peek(0).map(Token::getType).orElse(null).equals(Token.TokenType.NOTEQUAL)
                        && !tokens.Peek(0).map(Token::getType).orElse(null).equals(Token.TokenType.NUMBER)) {
                    assertEquals(tokens.MatchAndRemove(Token.TokenType.EQUAL).isPresent(), true);
                    assertEquals(tokens.MatchAndRemove(Token.TokenType.NUMBER).isPresent(), true);
                }
            }
            assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);

            assertEquals(tokens.MatchAndRemove(Token.TokenType.PRINT).isPresent(), true);
            assertEquals(tokens.MatchAndRemove(Token.TokenType.STRINGLITERAL).isPresent(), true);

            assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);
        }

        // Ensure no more tokens are left
        assertEquals(tokens.MoreTokens(), false);
    }

    @Test
    public void testTokenHandler5() throws Exception {
        var tokens = new TokenHandler(testLexContent("BEGIN { if (x > 5) { print \"x is greater than 5\"; } }",
                new Token.TokenType[] {
                        Token.TokenType.BEGIN, Token.TokenType.OPENBRACE, Token.TokenType.IF, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.GREATERTHAN, Token.TokenType.NUMBER,
                        Token.TokenType.CLOSEPAREN,
                        Token.TokenType.OPENBRACE, Token.TokenType.PRINT, Token.TokenType.STRINGLITERAL,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.CLOSEBRACE, Token.TokenType.CLOSEBRACE
                }));

        // Test using Peek and MoreTokens
        while (tokens.MoreTokens()) {
            assertEquals(tokens.MatchAndRemove(Token.TokenType.BEGIN).isPresent(), true);
            assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);
            if (tokens.Peek(0).map(Token::getType).orElse(null) == Token.TokenType.IF) {
                assertEquals(tokens.MatchAndRemove(Token.TokenType.IF).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENPAREN).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.WORD).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.GREATERTHAN).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.NUMBER).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEPAREN).isPresent(), true);
                assertEquals(tokens.MatchAndRemove(Token.TokenType.OPENBRACE).isPresent(), true);

                while (tokens.Peek(0).map(Token::getType).orElse(null) != Token.TokenType.CLOSEBRACE) {
                    assertEquals(tokens.MatchAndRemove(Token.TokenType.PRINT).isPresent(), true);
                    assertEquals(tokens.MatchAndRemove(Token.TokenType.STRINGLITERAL).isPresent(), true);
                    assertEquals(tokens.MatchAndRemove(Token.TokenType.SEPERATOR).isPresent(), true);
                }

                assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);

            }
            assertEquals(tokens.MatchAndRemove(Token.TokenType.CLOSEBRACE).isPresent(), true);
        }

        // Ensure no more tokens are left
        assertEquals(tokens.MoreTokens(), false);
    }

    @Test
    public void ParseBasicFunction() throws Exception {
        var lexer = testLexContent("function function_name(argument1, argument2, a) BEGIN END BEGIN ",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.BEGIN, Token.TokenType.END,
                        Token.TokenType.BEGIN });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();
        assertEquals(parsed.getEndBlocks().size(), 1);
        assertEquals(parsed.getBeginBlocks().size(), 2);
        assertEquals(parsed.getFunctions().size(), 1);
        assertEquals(parsed.getFunctions().get(0).getName(), "function_name");
        assertEquals(parsed.getFunctions().get(0).getParameters().size(), 3);
    }

    // CHATGPT TEST

    @Test
    public void ParseBasicFunctionSignature() throws Exception {
        var lexer = testLexContent("function function_name(argument1, argument2, a) \n BEGIN \n END \n BEGIN",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(2, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("function_name", parsed.getFunctions().get(0).getName());
        assertEquals(3, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseFunctionWithNoParameters() throws Exception {
        var lexer = testLexContent("function func_no_params() \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(1, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("func_no_params", parsed.getFunctions().get(0).getName());
        assertEquals(0, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseFunctionWithMultipleParameters() throws Exception {
        var lexer = testLexContent("function func_multi_params(param1, param2, param3) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.COMMA,
                        Token.TokenType.WORD, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(1, parsed.getEndBlocks().size());
        assertEquals(1, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(1, parsed.getFunctions().size());
        assertEquals("func_multi_params", parsed.getFunctions().get(0).getName());
        assertEquals(3, parsed.getFunctions().get(0).getParameters().size());
    }

    @Test
    public void ParseMultipleFunctions() throws Exception {
        var lexer = testLexContent("function func1() \n BEGIN \n END \n function func2(param1) \n BEGIN \n END END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR, Token.TokenType.BEGIN,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.END, Token.TokenType.SEPERATOR, Token.TokenType.FUNCTION, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END,
                        Token.TokenType.END });
        var parser = new Parser(lexer);
        var parsed = parser.Parse();

        // Check the number of BEGIN and END tokens
        assertEquals(3, parsed.getEndBlocks().size());
        assertEquals(2, parsed.getBeginBlocks().size());

        // Check the number of functions and their details
        assertEquals(2, parsed.getFunctions().size());
        assertEquals("func1", parsed.getFunctions().get(0).getName());
        assertEquals(0, parsed.getFunctions().get(0).getParameters().size());
        assertEquals("func2", parsed.getFunctions().get(1).getName());
        assertEquals(1, parsed.getFunctions().get(1).getParameters().size());
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature1() throws Exception {
        var lexer = testLexContent("function () \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.OPENPAREN,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature2() throws Exception {
        var lexer = testLexContent("function a a( \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.WORD,
                        Token.TokenType.OPENPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature3() throws Exception {
        var lexer = testLexContent("function a(a,) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.CLOSEPAREN,
                        Token.TokenType.SEPERATOR, Token.TokenType.BEGIN, Token.TokenType.SEPERATOR,
                        Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature4() throws Exception {
        var lexer = testLexContent("function func(,) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.COMMA, Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature5() throws Exception {
        var lexer = testLexContent("function func(a,,b) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.COMMA, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignature6() throws Exception {
        var lexer = testLexContent("function func(a,b c) \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.WORD,
                        Token.TokenType.CLOSEPAREN, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    @Test(expected = AwkException.class)
    public void ParseInvalidFunctionSignatureWithoutClosingParenthesis() throws Exception {
        var lexer = testLexContent("function func(a, b \n BEGIN \n END",
                new Token.TokenType[] { Token.TokenType.FUNCTION, Token.TokenType.WORD, Token.TokenType.OPENPAREN,
                        Token.TokenType.WORD, Token.TokenType.COMMA, Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.BEGIN, Token.TokenType.SEPERATOR, Token.TokenType.END });
        var parser = new Parser(lexer);
        parser.Parse(); // This should throw an AwkException
    }

    // parser 2 tests
    // these tests should use instanceof pattern matching (java 21 preview)
    @Test
    public void predecparse() throws Exception {
        var parser = new Parser(
                testLexContent("--a", new Token.TokenType[] { Token.TokenType.MINUSMINUS, Token.TokenType.WORD }));
        var res = parser.ParseOperation().get();
        if (res instanceof OperationNode op && op.getLeft() instanceof VariableReferenceNode variable) {
            System.out.println(op);
            assertEquals(op.getOperation(), OperationNode.Operation.PREDEC);
            assertEquals(variable.getName(), "a");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void preincparse() throws Exception {
        var parser = new Parser(
                testLexContent("++a", new Token.TokenType[] { Token.TokenType.PLUSPLUS, Token.TokenType.WORD }));
        var res = parser.ParseOperation().get();
        if (res instanceof OperationNode op && op.getLeft() instanceof VariableReferenceNode variable) {
            System.out.println(op);
            assertEquals(op.getOperation(), OperationNode.Operation.PREINC);
            assertEquals(variable.getName(), "a");
        } else {
            throw new Exception("test failed");
        }

    }

    @Test
    public void constantparse() throws Exception {
        var parser = new Parser(
                testLexContent("1.75 \"a\\\"aa\" `a[0]*`",
                        new Token.TokenType[] { Token.TokenType.NUMBER, Token.TokenType.STRINGLITERAL,
                                Token.TokenType.PATTERN }));
        var num = parser.ParseOperation().get();
        var word = parser.ParseOperation().get();
        var pat = parser.ParseOperation().get();
        if (num instanceof ConstantNode number && word instanceof ConstantNode string
                && pat instanceof PatternNode pattern) {
            assertEquals(number.getValue(), "1.75");
            assertEquals(string.getValue(), "a\"aa");
            assertEquals(pattern.getPattern(), "a[0]*");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void dollarparse() throws Exception {
        var parser = new Parser(
                testLexContent("$4 $-1",
                        new Token.TokenType[] { Token.TokenType.DOLLAR, Token.TokenType.NUMBER,
                                Token.TokenType.DOLLAR, Token.TokenType.MINUS, Token.TokenType.NUMBER }));
        var d1 = parser.ParseOperation().get();
        var d2 = parser.ParseOperation().get();
        if (d1 instanceof OperationNode d11 && d11.getLeft() instanceof ConstantNode i1
                && d2 instanceof OperationNode d22 && d22.getLeft() instanceof OperationNode i2
                && i2.getLeft() instanceof ConstantNode i22) {
            assertEquals(d11.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(i1.getValue(), "4");
            assertEquals(d22.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(i2.getOperation(), OperationNode.Operation.UNARYNEG);
            assertEquals(i22.getValue(), "1");
        } else {
            throw new Exception("test failed");
        }
    }

    @Test
    public void indexparse() throws Exception {
        var parser = new Parser(
                testLexContent("   variable\t[++(\t+ $ u ) ]  \t",
                        new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                                Token.TokenType.PLUSPLUS, Token.TokenType.OPENPAREN, Token.TokenType.PLUS,
                                Token.TokenType.DOLLAR, Token.TokenType.WORD, Token.TokenType.CLOSEPAREN,
                                Token.TokenType.CLOSEBRACKET }));
        var v = parser.ParseOperation().get();
        System.out.println(v);
        if (v instanceof VariableReferenceNode v1 && v1.getIndex().get() instanceof OperationNode index
                && index.getLeft() instanceof OperationNode index1 && index1.getLeft() instanceof OperationNode index2
                && index2.getLeft() instanceof VariableReferenceNode index3) {
            assertEquals(v1.getName(), "variable");
            assertEquals(index.getOperation(), OperationNode.Operation.PREINC);
            assertEquals(index1.getOperation(), OperationNode.Operation.UNARYPOS);
            assertEquals(index2.getOperation(), OperationNode.Operation.DOLLAR);
            assertEquals(index3.getName(), "u");

        } else {
            throw new Exception("test failed");
        }
    }

    // testing invalid parsing
    private void testInvalidOperation(String content, Token.TokenType[] expected) throws Exception {
        var parser = new Parser(
                testLexContent(content, expected));
        assertThrows(AwkException.class, () -> parser.ParseOperation());

    }

    @Test
    public void noexpr() throws Exception {
        testInvalidOperation("()", new Token.TokenType[] { Token.TokenType.OPENPAREN, Token.TokenType.CLOSEPAREN });
        testInvalidOperation("+", new Token.TokenType[] { Token.TokenType.PLUS });
        testInvalidOperation("-", new Token.TokenType[] { Token.TokenType.MINUS });
        testInvalidOperation("!", new Token.TokenType[] { Token.TokenType.NOT });
        testInvalidOperation("++", new Token.TokenType[] { Token.TokenType.PLUSPLUS });
        testInvalidOperation("--", new Token.TokenType[] { Token.TokenType.MINUSMINUS });
        testInvalidOperation("var[]", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,
                Token.TokenType.CLOSEBRACKET });
    }

    @Test
    public void unbalancedbrace() throws Exception {
        testInvalidOperation("($--4", new Token.TokenType[] { Token.TokenType.OPENPAREN,Token.TokenType.DOLLAR, Token.TokenType.MINUSMINUS, Token.TokenType.NUMBER, });
        testInvalidOperation("var[`5#`", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.OPENBRACKET,Token.TokenType.PATTERN
                });

    }
}
