import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FunctionalLexer {
    private StringHandler source;
    private int position = 1;
    private int lineNumber = 1;

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

    // tells the lexer what type of lexing it should do based on a given character
    // (if the character is not in the map, then it is not supported by the lexer)
    // the reason it is Supplier<Optional<Token>> as opposed to Supplier<Token> is
    // because characters like \r and \t do not give back tokens but are still valid
    // leading to 2 cases for lexing something at a given position either it gives
    // back token or not
    private HashMap<Character, Supplier<Optional<Token>>> dispatchTable = new HashMap<Character, Supplier<Optional<Token>>>() {
        {
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
            put('=', makeSingleymbolProcessor(Token.TokenType.ASSIGN));
            put('?', makeSingleymbolProcessor(Token.TokenType.QUESTION));
            put(':', makeSingleymbolProcessor(Token.TokenType.COLON));
            put(';', makeSingleymbolProcessor(Token.TokenType.SEPERATOR));
            put(',', makeSingleymbolProcessor(Token.TokenType.COMMA));

            put('\n', () -> {
                int start = position;
                position = 1;
                source.Swallow(1);
                return Optional.ofNullable(new Token(start, lineNumber++, Token.TokenType.SEPERATOR));
            });

            put('<', makeTwoSymbolProccesor(Optional.of(Token.TokenType.LESSTHAN),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.LESSTHANEQUAL : null)));
            put('>', makeTwoSymbolProccesor(Optional.of(Token.TokenType.GREATERTHAN), c -> Optional
                    .ofNullable(
                            c == '=' ? Token.TokenType.GREATERTHANEQUAL : c == '>' ? Token.TokenType.APPEND : null)));
            put('!', makeTwoSymbolProccesor(Optional.of(Token.TokenType.NOT), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.NOTEQUAL : c == '~' ? Token.TokenType.NOTMATCH : null)));
            put('+', makeTwoSymbolProccesor(Optional.of(Token.TokenType.PLUS), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.PLUSEQUAL : c == '>' ? Token.TokenType.PLUSPLUS : null)));
            put('^', makeTwoSymbolProccesor(Optional.of(Token.TokenType.EXPONENT),
                    c -> Optional.of(c == '=' ? Token.TokenType.LESSTHANEQUAL : null)));
            put('-', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MINUS), c -> Optional
                    .ofNullable(c == '=' ? Token.TokenType.MINUSEQUAL : c == '>' ? Token.TokenType.MINUSMINUS : null)));
            put('*', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MULTIPLY),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.LESSTHANEQUAL : null)));
            put('/', makeTwoSymbolProccesor(Optional.of(Token.TokenType.DIVIDE),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.DIVIDEQUAL : null)));
            put('%', makeTwoSymbolProccesor(Optional.of(Token.TokenType.MODULO),
                    c -> Optional.ofNullable(c == '=' ? Token.TokenType.MODULOEQUAL : null)));
            put('|', makeTwoSymbolProccesor(Optional.of(Token.TokenType.VERTICALBAR),
                    c -> Optional.ofNullable(c == '|' ? Token.TokenType.OR : null)));
            put('&', makeTwoSymbolProccesor(Optional.of(Token.TokenType.AND),
                    c -> Optional.ofNullable(c == '&' ? Token.TokenType.AND : null)));

        }

    };

    private Supplier<Optional<Token>> makeTwoSymbolProccesor(Optional<Token.TokenType> single,
            Function<Character, Optional<Token.TokenType>> matchesSecond) {
        return () -> {
            int start = position++;
            char first = source.GetChar();
            if (!source.IsDone() && matchesSecond.apply(source.Peek()).isPresent()) {
                position++;
                return Optional.ofNullable(new Token(start, lineNumber, matchesSecond.apply(source.GetChar()).get()));
            } else if (single.isPresent()) {
                return Optional.ofNullable(new Token(start, lineNumber, single.get()));
            } else {
                throw new RuntimeException("Error: Character `" + first + "` not recognized");
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

    private Supplier<Optional<Token>> makeSingleymbolProcessor(Token.TokenType token) {
        return () -> {
            source.Swallow(1);
            return Optional.ofNullable(new Token(position++, lineNumber, token));
        };
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
                    tokens.push(maybeToken.get());
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
            number += '.' + processInteger();
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
        if (keywords.containsKey(word)) {
            return new Token(startPosition, lineNumber, keywords.get(word));
        }
        return new Token(position, lineNumber, Token.TokenType.WORD, word);
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
        int startLine = lineNumber;
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
        return new Token(startPosition, startLine, type, word);
    }

    private void checkUpdateLine() {
        if (source.Peek(0) == '\n') {
            lineNumber += 1;
            position = 1;
        }
    }

}
