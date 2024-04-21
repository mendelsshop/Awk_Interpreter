import java.util.LinkedList;
import java.util.stream.Collectors;

public class BlockNode extends Node {
    private Optional<Node> Condition;

    public void setCondition(Optional<Node> condition) {
        Condition = condition;
    }

    private LinkedList<StatementNode> statements;

    public BlockNode(LinkedList<StatementNode> statements) {
        Condition = Optional.empty();
        this.statements = statements;
    }

    public Optional<Node> getCondition() {
        return Condition;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return Condition.map(c -> c.toString() + "? ").orElse("") + "{\n"
                + statements.stream().map(c -> c.toString()).collect(Collectors.joining("\n")) + "\n}";
    }
}
