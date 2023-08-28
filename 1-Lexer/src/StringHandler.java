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

    // peeking index starts at 0 Peek(0) peeks at the next character
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

    // Swallowing index starst at 1
    public void Swallow(int i) {
        position += i;
    }

    public boolean IsDone() {
        // we check content.length() == position and not content.length() - 1 ==
        // position
        // because of the way GetChar works in that it increments position not for
        // itself but for the next time the StringHandler is used
        // so let say where at the second to last character and we GetChar so we
        // increment postion and now posistion = content.length - 1 so it would ignore
        // the last character if we checked for content.length() - 1 == position
        // another problem with content.length() - 1 == position is that when you have
        // empty string position has to be negative in order for it to be done (which it
        // will never been)
        return content.length() == position;
    }

    // TODO: Does it consume
    public String Remainder() {
        String substring = content.substring(position);
        // consume remaining characters
        position = content.length();
        return substring;
    }
}
