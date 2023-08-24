public class StringHandler {
    // TODO: what type do use to store the source string Stream<Character> or String
    private String content;
    private int position = 0;

    // Methods added for unit tests
    public int getPosition() {
        return position;
    }

    public char Peek() {
        return content.charAt(position);
    }

    // TODO: are supposed to be checking for end of string
    public StringHandler(String content) {
        this.content = content;
    }

    public char Peek(int i) {
        return content.charAt(position + i);
    }

    public String PeekString(int i) {
        return content.substring(position, position + i);
    }

    public char GetChar() {
        // post increment so its fine to inline in method call (ie it will get the
        // current position not position++)
        return content.charAt(position++);
    }

    public void Swallow(int i) {
        position += i;
    }

    public boolean IsDone() {
        return content.length() - 1 == position;
    }

    // TODO: Does it consume
    public String Remainder() {
        String substring = content.substring(position);
        // consume remaining characters
        position = content.length() - 1;
        return substring;
    }
}
