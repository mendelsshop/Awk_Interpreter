import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Lexer {
    private StringHandler source;
    private int position = 0;
    private int lineNumber = 0;

    private HashMap<Character, Supplier<Optional<Token>>> dispatchTable = new HashMap<Character, Supplier<Optional<Token>>>() {
        {
            put('\n', () -> {
                source.GetChar();
                return Optional.ofNullable(new Token(ResetPosition(), lineNumber++, Token.TokenType.SEPERATOR));
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
            // TODO: process number might be split into 2 parts
            put('.', () -> Optional.ofNullable(ProcessDigit()));
            // numbers
            putAll(MapFromInterval(48, 57, () -> Optional.ofNullable(ProcessDigit())));
            // uppercase letters
            putAll(MapFromInterval(65, 90, () -> Optional.ofNullable(ProcessWord())));
            // lowercase letters
            putAll(MapFromInterval(97, 122, () -> Optional.ofNullable(ProcessWord())));
        }

    };

    private Optional<Token> absorbAndDo(Runnable doer) {
        source.Swallow(1);
        doer.run();
        return Optional.empty();
    }

    private Map<Character, Supplier<Optional<Token>>> MapFromInterval(int startInclusive, int endInclusive,
            Supplier<Optional<Token>> tokenMaker) {
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed()
                .collect(Collectors.<Integer, Character, Supplier<Optional<Token>>>toMap((c) -> (char) ((int) c),
                        c -> tokenMaker));
    }

    public Lexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() throws Exception {
        var tokens = new LinkedList<Token>();
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

    public Token ProcessDigit() {
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            number += "." + processInteger();
        }
        return new Token(position, lineNumber, Token.TokenType.NUMBER, number);
    }

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

    private int ResetPosition() {
        int previousPosition = position;
        position = 0;
        return previousPosition;
    }
}
