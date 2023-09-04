import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Random;

import org.junit.Test;

public class UnitTests {
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
    public void testLexContent(String content, Token.TokenType[] expected) throws Exception {
        var lexer = new Lexer(content);
        var otherLexer = new FunctionalLexer(content);
        var lexed = lexer.lex();
        var otherLexed = otherLexer.lex();
        var lexedTokens = lexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
        var otherLexedTokens = otherLexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
        assertArrayEquals(expected, lexedTokens);
        assertArrayEquals(expected, otherLexedTokens);

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

    @Test
    public void EmptyLex() throws Exception {
        var lexer = new Lexer("");
        assertEquals(lexer.lex(), new LinkedList<>());
    }

}
