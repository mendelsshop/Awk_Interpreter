import java.util.LinkedList;

public class DeleteNode extends StatementNode {
    private String name; // could this be any lvalue
    public DeleteNode(String name) {
        this.name = name;
    }
    private Optional<LinkedList<String>> indexs = Optional.empty();
    public DeleteNode(String name, LinkedList<String> indexs) {
        this.name = name;
        this.indexs = Optional.of(indexs);
    }
    public String getName() {
        return name;
    }
    public Optional<LinkedList<String>> getIndex() {
        return indexs;
    }
}
