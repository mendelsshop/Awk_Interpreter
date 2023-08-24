import java.util.LinkedList;

public class Lexer {
    private StringHandler source;
    private int position = 1;
    private int lineNumber = 1;

    public Lexer(String input) {
        source = new StringHandler(input);
    }

    public LinkedList<Token> lex() {
        return null;
    }

    public Token ProcessWord() {
        return null;
    }

    public Token ProcessNumber() {
        return null;
    }
}
