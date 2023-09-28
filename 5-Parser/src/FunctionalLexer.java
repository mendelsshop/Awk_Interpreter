import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunctionalLexer extends Lexer {

    // https://stackoverflow.com/questions/22687943/is-it-possible-to-declare-that-a-suppliert-needs-to-throw-an-exception
    // since this is anyways not following the rules why not just use result instead
    // of exception
    @FunctionalInterface
    public interface LexerSupplier {
        public Optional<Token> get() throws AwkException;
    }

    // tells the lexer what type of lexing it should do based on a given character
    // (if the character is not in the map, then it is not supported by the lexer)
    // the reason it is Supplier<Optional<Token>> as opposed to Supplier<Token> is
    // because characters like \r and \t do not give back tokens but are still valid
    // leading to 2 cases for lexing something at a given position either it gives
    // back token or not
    private HashMap<Character, LexerSupplier> dispatchTable = new HashMap<Character, LexerSupplier>() {
        {
            // \r is part of windows return (cariage return \r\n) so we dont want to fail on
            // \r but rather ignore it (who doesn't like type writers)
            put('\r', () -> absorbAndDo(() -> {
            }));
            put(' ', () -> absorbAndDo(() -> {
                position++;
            }));
            put('\t', () -> absorbAndDo(() -> {
                position += 4;
            }));

            put('.', () -> Optional.ofNullable(ProcessDigit()));
            // we have to use intervals b/c the the key is not some sort of regex/character
            // range of the supported characters
            // so we end up having to insert an entry for each character in the list of
            // characters that use the specified operation

            // numbers
            // [0-9]
            putAll(MapFromInterval(48, 57, () -> Optional.ofNullable(ProcessDigit())));
            // uppercase letters
            // [A-Z]
            putAll(MapFromInterval(65, 90, () -> Optional.ofNullable(ProcessWord())));
            // lowercase letters
            // [a-z]
            putAll(MapFromInterval(97, 122, () -> Optional.ofNullable(ProcessWord())));

            put('"', () -> Optional.ofNullable(HandleStringLiteral()));
            put('`', () -> Optional.ofNullable(HandlePattern()));
            put('#', () -> {
                HanldeComment();
                return Optional.empty();
            });
            put('{', makeSingleymbolProcessor(Token.TokenType.OPENBRACE));
            put('}', makeSingleymbolProcessor(Token.TokenType.CLOSEBRACE));
            put('[', makeSingleymbolProcessor(Token.TokenType.OPENBRACKET));
            put(']', makeSingleymbolProcessor(Token.TokenType.CLOSEBRACKET));
            put('(', makeSingleymbolProcessor(Token.TokenType.OPENPAREN));
            put(')', makeSingleymbolProcessor(Token.TokenType.CLOSEPAREN));
            put('$', makeSingleymbolProcessor(Token.TokenType.DOLLAR));
            put('~', makeSingleymbolProcessor(Token.TokenType.MATCH));
            put('?', makeSingleymbolProcessor(Token.TokenType.QUESTION));
            put(':', makeSingleymbolProcessor(Token.TokenType.COLON));
            put(';', makeSingleymbolProcessor(Token.TokenType.SEPERATOR));
            put(',', makeSingleymbolProcessor(Token.TokenType.COMMA));

            put('\n', () -> {
                int start = position;
                position = 1;
                source.Swallow(1);
                return Optional.ofNullable(new Token(start, currentLine++, Token.TokenType.SEPERATOR));
            });

            put('=', makeTwoSymbolProccesor(Optional.of(Token.TokenType.ASSIGN),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.EQUAL : null)));
            put('<', makeTwoSymbolProccesor(Optional.of(Token.TokenType.LESSTHAN),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.LESSTHANEQUAL : null)));
            put('>', makeTwoSymbolProccesor(Optional.of(Token.TokenType.GREATERTHAN), c -> Optional
                    .ofNullable(
                            c == '=' ? Token.TokenType.GREATERTHANEQUAL : c == '>' ? Token.TokenType.APPEND : null)));
            put('!', makeTwoSymbolProccesor(Optional.of(Token.TokenType.NOT), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.NOTEQUAL : c == '~' ? Token.TokenType.NOTMATCH : null)));
            put('+', makeTwoSymbolProccesor(Optional.of(Token.TokenType.PLUS), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.PLUSEQUAL : c == '+' ? Token.TokenType.PLUSPLUS : null)));
            put('^', makeTwoSymbolProccesor(Optional.of(Token.TokenType.EXPONENT),
                    c -> Optional.of(c == '=' ? Token.TokenType.EXPONENTEQUAL : null)));
            put('-', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MINUS), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.MINUSEQUAL : c == '-' ? Token.TokenType.MINUSMINUS : null)));
            put('*', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MULTIPLY),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.MULTIPLYEQUAL : null)));
            put('/', makeTwoSymbolProccesor(Optional.of(Token.TokenType.DIVIDE),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.DIVIDEEQUAL : null)));
            put('%', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MODULO),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.MODULOEQUAL : null)));
            put('|', makeTwoSymbolProccesor(Optional.of(Token.TokenType.VERTICALBAR),
                    c -> Optional.ofNullable(c == '|' ? Token.TokenType.OR : null)));
            put('&', makeTwoSymbolProccesor(Optional.of(Token.TokenType.AND),
                    c -> Optional.ofNullable(c == '&' ? Token.TokenType.AND : null)));

        }

    };

    private LexerSupplier makeTwoSymbolProccesor(Optional<Token.TokenType> single,
            Function<Character, Optional<Token.TokenType>> matchesSecond) {
        return () -> {
            int start = position++;
            char first = source.GetChar();
            if (!source.IsDone() && matchesSecond.apply(source.Peek()).isPresent()) {
                position++;
                return Optional.ofNullable(new Token(start, currentLine, matchesSecond.apply(source.GetChar()).get()));
            } else if (single.isPresent()) {
                return Optional.ofNullable(new Token(start, currentLine, single.get()));
            } else {
                throw new AwkException(currentLine, position, "Character `" + first + "` not recognized",
                        AwkException.ExceptionType.LexicalError);
            }
        };

    }

    // Used for lexing operations that do not give back tokens, but everey operation
    // still needs to swallow at least one character for source or else we would end
    // up with infinite loop in lex b/c lex peeks and doesnt't swallow (using
    // GetChar)
    private Optional<Token> absorbAndDo(Runnable doer) {
        source.Swallow(1);
        doer.run();
        return Optional.empty();
    }

    private LexerSupplier makeSingleymbolProcessor(Token.TokenType token) {
        return () -> {
            source.Swallow(1);
            return Optional.ofNullable(new Token(position++, currentLine, token));
        };
    }

    // takes a an "interval" of letters (in number format ie: A=65,..,a=97) and
    // creates a map on that
    // interval where each value is the same (the TokenMakr Supplier)
    private Map<Character, LexerSupplier> MapFromInterval(int startInclusive,
            int endInclusive,
            LexerSupplier tokenMaker) {
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed()
                .collect(Collectors.toMap((c) -> (char) ((int) c),
                        c -> tokenMaker));
    }

    public FunctionalLexer(String input) {
        super(input);
    }

    public LinkedList<Token> lex() throws AwkException {
        var tokens = new LinkedList<Token>();
        // go through source peek on next character dispath usig dispatchTable to do the
        // right lexical analysis reqiured for the source at the current position
        // also check that the source contains only valid characters based on whether
        // the given character is in the dispatchTable or not
        while (!source.IsDone()) {
            var current = source.Peek();
            if (dispatchTable.containsKey(current)) {
                var maybeToken = dispatchTable.get(current).get();
                if (maybeToken.isPresent()) {
                    tokens.add(maybeToken.get());
                }
            } else {
                throw new AwkException(currentLine, position, "Character `" + current + "` not recognized",
                        AwkException.ExceptionType.LexicalError);
            }
        }

        return tokens;
    }
}
