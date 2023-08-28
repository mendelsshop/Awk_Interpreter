import java.util.LinkedList;
import java.util.Optional;

public class Lexer {
    private StringHandler source;
    private int position = 0;
    private int lineNumber = 0;

    public Lexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() throws Exception {
        var tokens = new LinkedList<Token>();
        while (!source.IsDone()) {
            var current = source.Peek();
            var token = lexCharacter(current);
            if (token.isPresent()) {
                tokens.add(token.get());
            }
        }

        return tokens;
    }

    // Manages state switching of the Lexer
    // but also manages the output after of a state ie the token it may produce
    private Optional<Token> lexCharacter(Character current) throws Exception {
        if (isLetter(current)) {
            return Optional.ofNullable(ProcessWord());
        } else if (isDigit(current) || current == '.') {
            return Optional.ofNullable(ProcessDigit());
        } else if (current == ' ' || current == '\t') {
            source.Swallow(1);
            position++;
            return Optional.empty();
        } else if (current == '\n') {
            source.Swallow(1);
            int savePosition = ResetPositionIncrementLine();
            return Optional
                    // the reason for the lineNumber - 1 is that ResetPositionIncrementLine as the
                    // name suggests it increments the line (but I wold think the new line character
                    // is part of the previous line)
                    .ofNullable(new Token(savePosition, lineNumber - 1, Token.TokenType.SEPERATOR));
        } else if (current == '\r') {
            source.Swallow(1);
            return Optional.empty();
        } else {
            throw new Exception("Error: Character `" + current + "` not recognized");
        }

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
        // we can always use isAlphaNumeric as opposed to using isLetter the first time
        // and then isAlphaNumeric for the rest
        // because by the time we enter processWord the "state" already changed to
        // processing words, so we already know the first character was a letter
        while (!source.IsDone() && (isAlphaNumeric(source.Peek())
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

    private static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isAlphaNumeric(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
    }

}
