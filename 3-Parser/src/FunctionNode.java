import java.util.LinkedList;
import java.util.stream.Collectors;

public class FunctionNode extends Node {
    @Override
    public String toString() {

        return "Function " + name + "(" + parameters.stream().map(c -> c.toString()).collect(Collectors.joining(", "))
                + ") {\n" + statements.stream().map(c -> c.toString()).collect(Collectors.joining("\n")) + "\n}";
    }

    private String name;
    private LinkedList<String> parameters;
    private LinkedList<StatementNode> statements;

    public FunctionNode(String name, LinkedList<String> parameters, LinkedList<StatementNode> statements) {
        this.name = name;
        this.parameters = parameters;
        this.statements = statements;
    }

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
