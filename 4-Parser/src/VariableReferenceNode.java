public class VariableReferenceNode extends StatementNode {
    private String name;

    public String getName() {
        return name;
    }

    public VariableReferenceNode(String name) {
        this.name = name;

    }

    // what does index mean?
    private Optional<Node> index = Optional.empty();

    public Optional<Node> getIndex() {
        return index;
    }

    public VariableReferenceNode(String name, Node index) {
        this.name = name;
        this.index = Optional.of(index);
    }

    @Override
    public String toString() {
        return index.isPresent() ? name + "[" + index.get() + "]" : name;
    }
}
