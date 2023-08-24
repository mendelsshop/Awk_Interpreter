public class StringHandler {
    // TODO: what type do use to store the source string Stream<Character> or String
    private String content;
    private int position = 0;

    public StringHandler(String content) {
        this.content = content;
    }

    public char Peek(int i) {
        return 0;
    }

    public String PeekString(int i) {
        return null;
    }

    public char GetChar() {
        return 0;
    }

    public void Swallow(int i) {

    }

    public boolean IsDone() {
        return false;
    }

    public String Remainder() {
        return null;
    }
}
