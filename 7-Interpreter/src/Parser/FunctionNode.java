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
    protected FunctionNode(String name, LinkedList<String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((statements == null) ? 0 : statements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionNode other = (FunctionNode) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (statements == null) {
            if (other.statements != null)
                return false;
        } else if (!statements.equals(other.statements))
            return false;
        return true;
    }
}
