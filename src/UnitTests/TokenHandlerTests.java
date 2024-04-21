import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.function.Supplier;

import org.junit.Test;

public class TokenHandlerTests {
    // TokenHandler tests
    public void TokenHandlerFuzzer(LinkedList<Token> tokens, int numberOfOperations) {
        var handler = new TokenHandler(tokens);
        // deep clone via linkedlist b/c matchandremove modifies the original list
        tokens = new LinkedList<Token>(tokens);

        for (int i = 0; i < numberOfOperations; i++) {
            if (tokens.size() == 0) {
                assertEquals(handler.MoreTokens(), false);
                if (UnitTests.debug) {
                    System.out.println("finished tokens after " + i + " operation(s)");
                }
                break;
            }
            int op = UnitTests.rng.nextInt(5);
            switch (op) {
                // peek
                case 0:
                    int index = tokens.size() - 1 != 0 ? UnitTests.rng.nextInt(0, tokens.size() - 1) : 0;
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
                        int tt_index = UnitTests.rng.nextInt(3);
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
        var tokens = UnitTests.testLexContent(
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
        var tokens = UnitTests.testLexContent(
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
        var tokens = new TokenHandler(UnitTests.testLexContent("BEGIN \n {} a == 5 {print \"a is 5\"}", new Token.TokenType[] {
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
        var tokens = new TokenHandler(UnitTests.testLexContent("BEGIN \n {} a == 5 {print \"a is 5\"}", new Token.TokenType[] {
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
        var tokens = new TokenHandler(UnitTests.testLexContent("BEGIN { if (x > 5) { print \"x is greater than 5\"; } }",
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
}
