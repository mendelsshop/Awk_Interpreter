import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunctionalLexer extends Lexer {

    // https://stackoverflow.com/questions/22687943/is-it-possible-to-declare-that-a-suppliert-needs-to-throw-an-exception
    @FunctionalInterface
    public interface LexerSupplier {
        public Optional<Token> get() throws LexerException;
    }

    // tells the lexer what type of lexing it should do based on a given character
    // (if the character is not in the map, then it is not supported by the lexer)
    // the reason it is LexerSupplier<Optional<Token>> as opposed to
    // LexerSupplier<Token> is
    // because characters like \r and \t do not give back tokens but are still valid
    // leading to 2 cases for lexing something at a given position either it gives
    // back token or not
    private HashMap<Character, LexerSupplier> dispatchTable = new HashMap<Character, LexerSupplier>() {
        {
            put('\n', () -> {
                int start = position;
                position = 1;
                source.Swallow(1);
                return Optional.ofNullable(new Token(start, lineNumber++, Token.TokenType.SEPERATOR));
            });
            // \r is part of windows return (cariage return \r\n) so we dont want to fail on
            // \r but rather ignore it (who doesn't like type writers)
            put('\r', () -> absorbAndDo(() -> {
            }));
            put(' ', () -> absorbAndDo(() -> {
                position++;
            }));
            // position+=4 because when we display errors \t generally makes a tab of (4
            // spaces)
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
        }

    };

    // Used for lexing operations that do not give back tokens, but everey operation
    // still needs to swallow at least one character for source or else we would end
    // up with infinite loop in lex b/c lex peeks and doesnt't swallow (using
    // GetChar)
    private Optional<Token> absorbAndDo(Runnable doer) {
        source.Swallow(1);
        doer.run();
        return Optional.empty();
    }

    // takes a an "interval" of letters (in number format ie: A=65,..,a=97) and
    // creates a map on that
    // interval where each value is the same (the TokenMakr LexerSupplier)
    private Map<Character, LexerSupplier> MapFromInterval(int startInclusive, int endInclusive,
            LexerSupplier tokenMaker) {
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed()
                .collect(Collectors.<Integer, Character, LexerSupplier>toMap((c) -> (char) ((int) c),
                        c -> tokenMaker));
    }

    public FunctionalLexer(String input) {
        super(input);
    }

    public LinkedList<Token> lex() throws LexerException {
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
                throw new LexerException(lineNumber, position, "Character `" + current + "` not recognized");
            }
        }

        return tokens;
    }
}
