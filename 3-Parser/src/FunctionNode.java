import java.util.LinkedList;

public class FunctionNode extends Node {
    @Override
    public String toString() {
        return "Function" + name + parameters+ " " + statements;
    }
    private String name;
    private LinkedList<String> parameters;
    private LinkedList<StatementNode> statements;
    public String getName() {
        return name;
    }
    public LinkedList<String> getParameters() {
        return parameters;
    }
    public LinkedList<StatementNode> getStatements() {
        return statements;
    }
}
