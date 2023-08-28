import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class Lexer {
    private StringHandler source;
    private int position = 0;
    private int lineNumber = 0;
    private HashMap<String, Token.TokenType> keywords = new HashMap<String, Token.TokenType>() {
        {
            put("while", Token.TokenType.WHILE);
            put("if", Token.TokenType.IF);
            put("do", Token.TokenType.DO);
            put("for", Token.TokenType.FOR);
            put("break", Token.TokenType.BREAK);
            put("continue", Token.TokenType.CONTINUE);
            put("else", Token.TokenType.ELSE);
            put("return", Token.TokenType.RETURN);
            put("BEGIN", Token.TokenType.BEGIN);
            put("END", Token.TokenType.END);
            put("print", Token.TokenType.PRINT);
            put("printf", Token.TokenType.PRINTF);
            put("next", Token.TokenType.NEXT);
            put("in", Token.TokenType.IN);
            put("delete", Token.TokenType.DELETE);
            put("getline", Token.TokenType.GETLINE);
            put("exit", Token.TokenType.EXIT);
            put("nextfile", Token.TokenType.NEXTFILE);
            put("function", Token.TokenType.FUNCTION);

        }
    };

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
        } else if (current == '"') {
            return Optional.ofNullable(HandleStringLiteral());
        }
        else if (current == ' ' || current == '\t') {
            source.Swallow(1);
            position++;
            return Optional.empty();
        }else if (current == '#') {
            HanldeComment();
            return Optional.empty();
        } 
         else if (current == '\n') {
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
        if (keywords.containsKey(word)) {
            return new Token(position, lineNumber, keywords.get(word));
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

    private Token HandleStringLiteral() {
        // swallow the start quote
        source.GetChar();
        Character lastChar = ' ';
        String word = "";
        while (!source.IsDone() && !(source.Peek() == '"' && lastChar != '\\')) {
            position++;
            char currentChar = source.GetChar();
            lastChar = currentChar;
            word += currentChar;
        }
        // swallow the end quote
        source.GetChar();
        return new Token(position, lineNumber, Token.TokenType.StringLiteral, word);
    }


    private void HanldeComment() {
        // are we suposing to put a separator for the \n?
        while (!source.IsDone() && !(source.Peek() == '\n')) {
            source.GetChar();
        }
        ResetPositionIncrementLine();
        // were not swallowing the \n because we want to create a new separator line token on the next iteration of lex
    }

    private Token HandlePattern() {
        // swallow the start backtick
        source.GetChar();
        return null;
    }
}
