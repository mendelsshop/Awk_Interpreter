import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Lexer {
    private StringHandler source;
    private int position = 0;
    private int lineNumber = 0;

    // tells the lexer what type of lexing it should do based on a given character
    // (if the character is not in the map, then it is not supported by the lexer)
    // the reason it is Supplier<Optional<Token>> as opposed to Supplier<Token> is
    // because characters like \r and \t do not give back tokens but are still valid
    // leading to 2 cases for lexing something at a given position either it gives
    // back token or not
    private HashMap<Character, Supplier<Optional<Token>>> dispatchTable = new HashMap<Character, Supplier<Optional<Token>>>() {
        {
            put('\n', () -> {
                source.GetChar();
                return Optional
                        .ofNullable(new Token(ResetPositionIncrementLine(), lineNumber, Token.TokenType.SEPERATOR));
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
        // TODO: I don't know which method to use here
        Stream.iterate(startInclusive, n -> n + 1).limit(endInclusive - startInclusive + 1).map(c -> (char) (int) c)
                .collect(Collectors.<Character, Character, Supplier<Optional<Token>>>toMap(c -> c,
                        c -> tokenMaker));
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed()
                .collect(Collectors.<Integer, Character, Supplier<Optional<Token>>>toMap((c) -> (char) ((int) c),
                        c -> tokenMaker));
    }

    public Lexer(String input) {
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
                    tokens.push(maybeToken.get());
                }
            } else {
                throw new Exception("Error: Character " + current + " not recognized");
            }
        }

        return tokens;
    }

    // "inner" method for ProcesDigit just lexes [0-9]*
    private String processInteger() {
        String number = "";
        // TODO: Charaacter.is* also includes unicode characters which are not allowed
        // to lex
        while (!source.IsDone() && Character.isDigit(source.Peek())) {
            position++;
            number += source.GetChar();
        }
        return number;
    }

    // [0-9]*\.[0-9]*
    public Token ProcessDigit() {
        // lex before decimal point
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            // lex after decimal point
            number += "." + processInteger();
        }
        return new Token(position, lineNumber, Token.TokenType.NUMBER, number);
    }

    // [a-zA-z][0-9a-zA-Z\-]*
    public Token ProcessWord() {
        String word = "";
        // TODO: Charaacter.is* also includes unicode characters which are not allowed
        // to lex
        while (!source.IsDone() && (Character.isLetterOrDigit(source.Peek())
                || source.Peek() == '_')) {
            position++;
            word += source.GetChar();
        }
        return new Token(position, lineNumber, Token.TokenType.WORD, word);
    }

    // used for processing newlines
    // reset position and gives back position from before reset
    // also increments line number
    private int ResetPositionIncrementLine() {
        lineNumber++;
        int previousPosition = position;
        position = 0;
        return previousPosition;
    }
}
