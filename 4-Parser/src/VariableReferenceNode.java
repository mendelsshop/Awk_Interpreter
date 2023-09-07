import java.util.Optional;

public class VariableReferenceNode extends Node {
    private String name;

    public VariableReferenceNode(String name) {
        this(name, Optional.empty());
    }

    // what does index mean?
    private Optional<Node> index;

    public VariableReferenceNode(String name, Optional<Node> index) {
        this.name = name;
        this.index = index;
    }
}
