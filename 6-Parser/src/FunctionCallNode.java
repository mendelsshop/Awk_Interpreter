import java.util.LinkedList;

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
}
