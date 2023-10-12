import java.util.LinkedList;
import java.util.stream.Collectors;

public class FunctionCallNode extends StatementNode {
    private String functionName;
    private LinkedList<Node> parameters;
    public FunctionCallNode(String functionName, LinkedList<Node> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }
    public String getFunctionName() {
        return functionName;
    }
    public LinkedList<Node> getParameters() {
        return parameters;
    }

    @Override 
    public String toString() {
        return functionName + "(" + parameters.stream().map(c -> c.toString()).collect(Collectors.joining(", ")) + ")";
    }
}
