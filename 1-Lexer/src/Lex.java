import java.util.LinkedList;

public class Lex {
    private StringHandler source;
    private int position = 1;
    private int lineNumber = 1;

    public Lex(String input) {
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
