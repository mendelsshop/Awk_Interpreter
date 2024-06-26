import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {
    // protected so I don't have to duplicate this for FunctionalLexer
    protected StringHandler source;
    // position and line number are zero-based
    protected int position = 1;
    protected int currentLine = 1;
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

    private HashMap<String, Token.TokenType> oneSymbol = new HashMap<String, Token.TokenType>() {
        {
            put("{", Token.TokenType.OPENBRACE);
            put("}", Token.TokenType.CLOSEBRACE);
            put("[", Token.TokenType.OPENBRACKET);
            put("]", Token.TokenType.CLOSEBRACKET);
            put("(", Token.TokenType.OPENPAREN);
            put(")", Token.TokenType.CLOSEPAREN);
            put("$", Token.TokenType.DOLLAR);
            put("~", Token.TokenType.MATCH);
            put("=", Token.TokenType.ASSIGN);
            put("<", Token.TokenType.LESSTHAN);
            put(">", Token.TokenType.GREATERTHAN);
            put("!", Token.TokenType.NOT);
            put("+", Token.TokenType.PLUS);
            put("^", Token.TokenType.EXPONENT);
            put("-", Token.TokenType.MINUS);
            put("?", Token.TokenType.QUESTION);
            put(":", Token.TokenType.COLON);
            put("*", Token.TokenType.MULTIPLY);
            put("/", Token.TokenType.DIVIDE);
            put("%", Token.TokenType.MODULO);
            put(";", Token.TokenType.SEPERATOR);
            put("\n", Token.TokenType.SEPERATOR);
            put("|", Token.TokenType.VERTICALBAR);
            put(",", Token.TokenType.COMMA);

        }
    };

    private HashMap<String, Token.TokenType> twoSymbols = new HashMap<String, Token.TokenType>() {
        {
            put(">=", Token.TokenType.GREATERTHANEQUAL);
            put("++", Token.TokenType.PLUSPLUS);
            put("--", Token.TokenType.MINUSMINUS);
            put("<=", Token.TokenType.LESSTHANEQUAL);
            put("==", Token.TokenType.EQUAL);
            put("!=", Token.TokenType.NOTEQUAL);
            put("^=", Token.TokenType.EXPONENTEQUAL);
            put("%=", Token.TokenType.MODULOEQUAL);
            put("*=", Token.TokenType.MULTIPLYEQUAL);
            put("/=", Token.TokenType.DIVIDEEQUAL);
            put("+=", Token.TokenType.PLUSEQUAL);
            put("-=", Token.TokenType.MINUSEQUAL);
            put("!~", Token.TokenType.NOTMATCH);
            put("&&", Token.TokenType.AND);
            put(">>", Token.TokenType.APPEND);
            put("||", Token.TokenType.OR);

        }
    };

    public Lexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() throws AwkException {
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
    private Optional<Token> lexCharacter(Character current) throws AwkException {
        var maybeSymbol = ProcessSymbol();
        // we check for if it is present instead of blindly returning
        // because each symbol ProcessSymbol tries to match on is the same thing as
        // having another condition in this if else chain
        if (maybeSymbol.isPresent()) {
            return maybeSymbol;
        } else if (isLetter(current)) {
            return Optional.ofNullable(ProcessWord());
        } else if (isDigit(current) || current == '.') {
            return Optional.ofNullable(ProcessDigit());
        } else if (current == '"') {
            return Optional.ofNullable(HandleStringLiteral());
        } else if (current == '`') {
            return Optional.ofNullable(HandlePattern());
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
        } else if (current == '#') {
            HanldeComment();
            return Optional.empty();
        } else if (current == '\r') {
            source.Swallow(1);
            return Optional.empty();
        } else {
            throw new AwkException(currentLine, position, "Character `" + current + "` not recognized",
                    AwkException.ExceptionType.LexicalError);
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

    protected Token ProcessDigit() throws AwkException {
        int startPosition = position;
        // lex before decimal point
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            // lex after decimal point
            // number += "." + processInteger();
            var decimal = processInteger();
            // in awk if we have a number with number after the decimal point that is all 0s awk treats that as a whole number
            // so stuff like `a[0] = 1; a[1] = 2; for (i = 0.0; i < 10; i++) {print a[i]}` doesnt just create a bunch of empty entries in the array but rather since 0.0 truncates to 0
            // once it the index get stringified it becomes "0" as opposed to "0.0" which are two different keys
            // in real awk this is done by tracking types of the values
            // but "0.0" is literally 0.0 (unless you do some math on it at which point it might truncate to 0 if you do something like +"0.0") 
            if (!decimal.matches("0+")) {
                number += "." + decimal;
            } 
            if (!source.IsDone() && source.Peek() == '.') {
                throw new AwkException(currentLine, position, "a number cannot have more than one decimal point",
                        AwkException.ExceptionType.LexicalError);
            }
        }
        if (number.equals(".")) {
            throw new AwkException(currentLine, startPosition,
                    "plain decimal point not valid as whole number, needs a digit before or after the decimal",
                    AwkException.ExceptionType.LexicalError);
        }
        return new Token(startPosition, currentLine, Token.TokenType.NUMBER, number);
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
        if (keywords.containsKey(word)) {
            return new Token(startPosition, currentLine, keywords.get(word));
        }
        return new Token(startPosition, currentLine, Token.TokenType.WORD, word);
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

    protected Token HandleStringLiteral() throws AwkException {
        return HandleQuotedIsh('"', Token.TokenType.STRINGLITERAL, "string");
    }

    protected void HanldeComment() {
        // TODO: are we suposing to put a separator for the \n?
        while (!source.IsDone() && !(source.Peek() == '\n')) {
            source.GetChar();
        }
        // were not swallowing the \n because we want to create a new separator line
        // token on the next iteration of lex
    }

    protected Token HandlePattern() throws AwkException {
        // should multiline patterns work
        return HandleQuotedIsh('`', Token.TokenType.PATTERN, "pattern");
    }

    protected Token HandleQuotedIsh(char quote, Token.TokenType type, String name) throws AwkException {
        int startLine = currentLine;
        int startPosition = position;
        // swallow the start backtick
        source.GetChar();
        position++;
        Character lastChar = ' ';
        String word = "";
        boolean atEnd = false;
        while (!source.IsDone()) {
            if (source.Peek() == quote && lastChar != '\\') {
                atEnd = true;
                break;
                // if we have a escaped quote cut off the last character (\)
            } else if (source.Peek() == quote && lastChar == '\\') {
                word = word.substring(0, word.length() - 1);
            }
            // we increment position before line number so if we hit newline then position
            // will be overwritten to 0
            position++;
            // awk dowsnt allow nulti line strings
            if (source.Peek() == '\n') {
                throw new AwkException(currentLine, position,
                        name + " does not have an end found newline before end quote" + quote,
                        AwkException.ExceptionType.LexicalError);
            }
            char currentChar = source.GetChar();
            lastChar = currentChar;
            word += currentChar;
        }
        // swallow the end backtick
        if (!atEnd) {
            throw new AwkException(currentLine, position,
                    name + " does not have an end " + quote, AwkException.ExceptionType.LexicalError);
        }
        source.GetChar();
        position++;
        return new Token(startPosition, startLine, type, word);
    }

    private Optional<Token> ProcessSymbol() {
        // we dont have to save position b/c we know how much further we have gone into
        // the source
        // but we save line number just in case we encounter \n
        int startLine = currentLine;
        var isTwoLetterSymbol = false;
        try {
            isTwoLetterSymbol = twoSymbols.containsKey(source.PeekString(2));
        } catch (StringIndexOutOfBoundsException e) {
            // do nothing because we could still be one letter symbol or something else
        }
        if (isTwoLetterSymbol) {
            Token.TokenType type = twoSymbols.get(source.PeekString(2));
            source.Swallow(2);
            position += 2;
            return Optional.ofNullable(new Token(position - 2, startLine, type));
        } else if (oneSymbol.containsKey(source.PeekString(1))) {
            Token.TokenType type = oneSymbol.get(source.PeekString(1));
            checkUpdateLine();
            position++;
            source.Swallow(1);
            return Optional.ofNullable(new Token(position, startLine, type));
        } else {
            return Optional.empty();
        }

    }

    private void checkUpdateLine() {
        if (source.Peek(0) == '\n') {
            currentLine += 1;
            position = 1;
        }
    }
}
