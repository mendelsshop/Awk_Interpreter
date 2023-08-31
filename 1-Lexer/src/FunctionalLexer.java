import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunctionalLexer {
    private StringHandler source;
    private int position = 1;
    private int lineNumber = 1;

    // tells the lexer what type of lexing it should do based on a given character
    // (if the character is not in the map, then it is not supported by the lexer)
    // the reason it is Supplier<Optional<Token>> as opposed to Supplier<Token> is
    // because characters like \r and \t do not give back tokens but are still valid
    // leading to 2 cases for lexing something at a given position either it gives
    // back token or not
    private HashMap<Character, Supplier<Optional<Token>>> dispatchTable = new HashMap<Character, Supplier<Optional<Token>>>() {
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
            // TODO: maybe position+=4
            put('\t', () -> absorbAndDo(() -> {
                position++;
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
    // interval where each value is the same (the TokenMakr Supplier)
    private Map<Character, Supplier<Optional<Token>>> MapFromInterval(int startInclusive, int endInclusive,
            Supplier<Optional<Token>> tokenMaker) {
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed()
                .collect(Collectors.<Integer, Character, Supplier<Optional<Token>>>toMap((c) -> (char) ((int) c),
                        c -> tokenMaker));
    }

    public FunctionalLexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() throws Exception {
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
                    System.out.println(maybeToken.get());
                    tokens.add(maybeToken.get());
                }
            } else {
                throw new Exception("Error: Character `" + current + "` not recognized");
            }
        }

        return tokens;
    }

    // "inner" method for ProcesDigit just lexes [0-9]*
    private String processInteger() {
        String number = "";
        while (!source.IsDone() && isDigit(source.Peek())) {
            position++;
            number += source.GetChar();
        }
        return number;
    }

    // [0-9]*\.[0-9]*
    public Token ProcessDigit() {
        int startPosition = position;
        // lex before decimal point
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            // lex after decimal point
            number += "." + processInteger();
        }
        return new Token(startPosition, lineNumber, Token.TokenType.NUMBER, number);
    }

    // [a-zA-z][0-9a-zA-Z\-]*
    public Token ProcessWord() {
        int startPosition = position;
        String word = "";
        // we can always use isAlphaNumeric as opposed to using isLetter the first time
        // and then isAlphaNumeric for the rest
        // because by the time we enter processWord the "state" already changed to
        // processing words, so we already know the first character was a letter
        while (!source.IsDone() && (isAlphaNumeric(source.Peek())
                || source.Peek() == '_')) {
            position++;
            word += source.GetChar();
        }
        return new Token(startPosition, lineNumber, Token.TokenType.WORD, word);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlphaNumeric(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

}
