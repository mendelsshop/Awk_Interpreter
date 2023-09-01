import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node {
    private Optional<Node> Condition;
    public void setCondition(Optional<Node> condition) {
        Condition = condition;
    }

    private LinkedList<StatementNode> statements;

    public Optional<Node> getCondition() {
        return Condition;
    }

    public LinkedList<StatementNode> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return Condition.map(c -> c.toString() + "?") + " " + statements;
    }
}
