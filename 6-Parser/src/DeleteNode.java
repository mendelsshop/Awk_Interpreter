import java.util.LinkedList;

public class DeleteNode extends StatementNode {
    private String name; // could this be any lvalue
    private Optional<LinkedList<String>> index;
    public DeleteNode(String name, Optional<LinkedList<String>> index) {
        this.name = name;
        this.index = index;
    }
    public String getName() {
        return name;
    }
    public Optional<LinkedList<String>> getIndex() {
        return index;
    }
}
