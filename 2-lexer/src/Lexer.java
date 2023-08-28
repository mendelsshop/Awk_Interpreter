import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class Lexer {
    private StringHandler source;
    // position and line number are zero-based
    private int position = 0;
    private int currentLine = 0;
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
            put("%=", Token.TokenType.DIVIDEQUAL);
            put("*=", Token.TokenType.MULTIPLYEQUAL);
            put("/=", Token.TokenType.DIVIDEQUAL);
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

    public LinkedList<Token> lex() throws Exception {
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
    private Optional<Token> lexCharacter(Character current) throws Exception {
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
        } else if (current == ' ' || current == '\t') {
            source.Swallow(1);
            position++;
            return Optional.empty();
        } else if (current == '#') {
            HanldeComment();
            return Optional.empty();
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
    private Token ProcessDigit() {
        int startPosition = position;
        // lex before decimal point
        String number = processInteger();
        if (!source.IsDone() && source.Peek() == '.') {
            source.Swallow(1);
            position++;
            // lex after decimal point
            number += "." + processInteger();
        }
        return new Token(startPosition, currentLine, Token.TokenType.NUMBER, number);
    }

    // [a-zA-z][0-9a-zA-Z\-]*
    private Token ProcessWord() {
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

    private Token HandleStringLiteral() {
        return HandleQuotedIsh('"', Token.TokenType.STRINGLITERAL);
    }

    private void HanldeComment() {
        // TODO: are we suposing to put a separator for the \n?
        while (!source.IsDone() && !(source.Peek() == '\n')) {
            source.GetChar();
        }
        // were not swallowing the \n because we want to create a new separator line
        // token on the next iteration of lex
    }

    private Token HandlePattern() {
        return HandleQuotedIsh('`', Token.TokenType.PATTERN);
    }

    private Token HandleQuotedIsh(char quote, Token.TokenType type) {
        int startLine = currentLine;
        int startPosition = position;
        // swallow the start backtick
        source.GetChar();
        position++;
        Character lastChar = ' ';
        String word = "";
        while (!source.IsDone() && !(source.Peek() == quote && lastChar != '\\')) {
            // we increment position before line number so if we hit newline then position
            // will be overwritten to 0
            position++;
            checkUpdateLine();
            char currentChar = source.GetChar();
            lastChar = currentChar;
            word += currentChar;
        }
        // swallow the end backtick
        source.GetChar();
        position++;
        return new Token(startPosition, startLine,type , word);
    }

    private Optional<Token> ProcessSymbol() {
        // we dont have to save position b/c we know how much further we have gone into
        // the source
        // but we save line number just in case we encounter \n
        int startLine = currentLine;
        if (twoSymbols.containsKey(source.PeekString(1))) {
            source.Swallow(2);
            position += 2;
            return Optional.ofNullable(new Token(position - 2, startLine, twoSymbols.get(source.PeekString(1))));
        } else if (oneSymbol.containsKey(source.PeekString(0))) {
            checkUpdateLine();
            position++;
            source.Swallow(1);
            return Optional.ofNullable(new Token(position, startLine, oneSymbol.get(source.PeekString(0))));
        } else {
            return Optional.empty();
        }

    }

    private void checkUpdateLine() {
        if (source.Peek(0) == '\n') {
            currentLine += 1;
            position = 0;
        }
    }
}
