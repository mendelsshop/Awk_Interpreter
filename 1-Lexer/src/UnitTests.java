import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Test
    public void EmptyLex() throws Exception {
        testLexContent("", new Token.TokenType[] {});
    }

    @Test
    // Tests non allowed characters like `,`
    public void LexLoremIpsum() throws Exception {
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        var lexer = new Lexer(loremIpsum);
        assertThrows(new Exception("Error: Character `,` not recognized").getClass(), () -> {
            lexer.lex();
        });
    }

    public void testLexContent(String content, Token.TokenType[] expected) throws Exception {
        var lexer = new Lexer(content);
        var otherLexer = new FunctionalLexer(content);
        var lexed = lexer.lex();
        var otherLexed = otherLexer.lex();
        var lexedTokens = lexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
        assertEquals(expected.length, lexedTokens.length);
        assertArrayEquals(expected, lexedTokens);
        assertEquals(lexed.size(), otherLexed.size());
        // we just check for tokentype equality
        Stream.iterate(0, n -> n == lexed.size() - 1, n -> n + 1)
                .forEach(c -> assertEquals(lexed.get(c).getType(), otherLexed.get(c).getType()));
    }

    @Test
    public void testInvalidLexer1() {
        assertThrowsLexError(new Exception("Error: Character `@` not recognized").getClass(),
                "@==4 {}");
    }

    @Test
    public void testInvalidAwk() {
        assertThrowsLexError(new Exception("Error: Character `{` not recognized").getClass(),
                "{\na=4}");
    }

    public <T extends Throwable> void assertThrowsLexError(Class<T> expectedThrowable,
            String content) {
        assertThrows(expectedThrowable, () -> testLexContent(content, new Token.TokenType[] {}));
    }

    @Test
    public void lexNewline() throws Exception {
        testLexContent("\r\n", new Token.TokenType[] { Token.TokenType.SEPERATOR });
    }

    @Test
    public void BasicLex() throws Exception {
        testLexContent("111aAAA\taaazz\nZZ1Z.zaaa", new Token.TokenType[] {
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
    public void LexDecimalNumber() throws Exception {
        testLexContent("123.999", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void LexDecimalNoNumber() throws Exception {
        testLexContent(".", new Token.TokenType[] { Token.TokenType.NUMBER });
    }

    @Test
    public void LexUnderScoreWord() throws Exception {
        testLexContent("a_s", new Token.TokenType[] { Token.TokenType.WORD });
    }

    @Test
    public void LexUnderScoreWordDot() throws Exception {
        testLexContent("a_.", new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.NUMBER });
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
        testLexContent("a_s\r\n\t1234.5678 az__..",
                new Token.TokenType[] { Token.TokenType.WORD, Token.TokenType.SEPERATOR,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                        Token.TokenType.NUMBER,
                });
    }

    @Test
    public void LexDotDotDotEtc() throws Exception {
        testLexContent("....",
                new Token.TokenType[] {
                        Token.TokenType.NUMBER,
                        Token.TokenType.NUMBER,
                        Token.TokenType.NUMBER,
                        Token.TokenType.NUMBER,
                });
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
        testLexContent("open_paren\tdefine open_paren\rlist .\r l close_paren l\tclose_paren",
                new Token.TokenType[] {
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.NUMBER,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                        Token.TokenType.WORD,
                });
    }

    @Test
    public void LexWordedSchemeCons() throws Exception {
        // (define (cons x y)
        //     (lambda (z)
        //         (cond ((= z 0) x)
        //             ((= z 1) y)
        //             (else (error "cons not zero or one")))))
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
                        Token.TokenType.WORD,

                });
    }

    @Test
    public void LexNumberWithAnyDigitIsh() throws Exception {
        Stream.iterate(0, n -> n < 256, n -> n + 1)
                .filter(c -> (c >= '0' && c <= '9') || c == '.').peek(System.out::println)
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

    public String randomValidTokenString(int maxWordLength) {
        // create stream of random number with upperbound of 256 highest ascci value
        return rng.ints(0, 256)
                // filter out non valid characters (and `_` b/c we have no way of knowing if the
                // underscore happens at beginning of word)
                .filter(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '\n'
                        || c == '\r' || c == ' ' || c == '\t')
                // after filtering is done limit the length of the string to be somewhat
                // reasonable
                .limit(rng.nextInt(0, maxWordLength))
                // collect to string by casting to char
                .boxed().<String>map(c -> ((Character) (char) (int) c).toString()).collect(Collectors.joining());

    }

    public String randomInValidTokenString(int maxWordLength) {
        return rng.ints(0, 1000)
                .filter(c -> !((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '\n'
                        || c == '\r' || c == ' ' || c == '\t'))
                // after filtering is done limit the length of the string to be somewhat
                // reasonable
                .limit(maxWordLength)
                // collect to string by casting to char
                .boxed().<String>map(c -> ((Character) (char) (int) c).toString()).collect(Collectors.joining());

    }

    public void testRandomInvalid() throws Exception {
        var invalid = randomInValidTokenString(1);
        assertThrowsLexError(new Exception("Error: Character `" + invalid + "` not recognized").getClass(), invalid);
    }

    @Test
    public void fuzzRandomInvalidLex() throws Exception {
        int iterations = rng.nextInt(0, 5);
        for (int i = 0; i < iterations; i++) {
            testRandomInvalid();
        }
    }

    public void lexFuzzingIsh(int maxWordLength, int Times) throws Exception {
        for (int i = 0; i < Times; i++) {
            var word = randomValidTokenString(maxWordLength);
            var lexer = new Lexer(word);
            lexer.lex();
        }
    }

    // meant to catch weird edge cases where lexer doesn't recognize valid tokens
    // (no way to verify that the output is right).
    @Test
    public void fuzzLexIsh_1000_100() throws Exception {
        lexFuzzingIsh(1000, 100);
    }

    @Test
    public void fuzzLexIsh_1000_1000() throws Exception {
        lexFuzzingIsh(1000, 1000);
    }
}
