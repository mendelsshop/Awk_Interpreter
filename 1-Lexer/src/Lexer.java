import java.util.LinkedList;
import java.util.Optional;

public class Lexer {
    // protected so I don't have to duplicate this for FunctionalLexer
    protected final StringHandler source;
    // position and line number are zero-based
    protected int position = 1;
    protected int lineNumber = 1;

    public Lexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() throws LexerException {
        var tokens = new LinkedList<Token>();
        while (!source.IsDone()) {
            var token = lexCharacter(source.Peek());
            if (token.isPresent()) {
                tokens.add(token.get());
            }
        }

        return tokens;
    }

    // Manages state switching of the Lexer
    // but also manages the output after of a state ie the token it may produce
    private Optional<Token> lexCharacter(Character current) throws LexerException {
        if (isLetter(current)) {
            return Optional.ofNullable(ProcessWord());
        } else if (isDigit(current) || current == '.') {
            return Optional.ofNullable(ProcessDigit());
        } else if (current == ' ') {
            source.Swallow(1);
            position++;
            return Optional.empty();
        } else if (current == '\t') {
            source.Swallow(1);
            // position+=4 because when we display errors \t generally makes a tab of (4
            // spaces)
            position += 4;
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
            throw new LexerException(lineNumber, position, "Character `" + current + "` not recognized");
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
    
    // Some methods are protected to avoid duplication with FunctionalLexer

    // ([0-9]+\.[0-9]+)|([0-9]+\.)|(\.[0-9]+)/[0-9]+)
    // you werent so specific about whats a valid number in the rubric
    // "Accepts required characters, creates a token, doesn’t accept characters it
    // shouldn’t (10)"
    // in the state machine you suggest that ([0-9]+)|([0-9]*\.[0-9]*) is what we
    // should accept, but in class you suggested otherwise
    // so I have gone according to the jdoodle AWK implentation which allows .0.8.8
    // and becomes Number(.0) Number(.8) Number(.8)
    // but dissallows (..) which could technically become Number(.) Number(.)
    // Number(.)
    // which follows the regex ([0-9]+\.[0-9]+)|([0-9]+\.)|(\.[0-9]+)/[0-9]+)
    protected Token ProcessDigit() throws LexerException {
        int startPosition = position;
        // lex before decimal point
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            // lex after decimal point
            number += "." + processInteger();
        }
        if (number.equals(".")) {
            throw new LexerException(lineNumber, startPosition,
                    "plain decimal point not valid as whole number, needs a digit before or after the decimal");
        }
        return new Token(startPosition, lineNumber, Token.TokenType.NUMBER, number);
    }

    // [a-zA-z][0-9a-zA-Z\-]*
    protected Token ProcessWord() {
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

    // used for processing newlines
    // reset position and gives back position from before reset
    // also increments line number
    private int ResetPositionIncrementLine() {
        lineNumber++;
        int previousPosition = position;
        position = 1;
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
