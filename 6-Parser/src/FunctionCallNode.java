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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
        FunctionCallNode other = (FunctionCallNode) obj;
        if (functionName == null) {
            if (other.functionName != null)
                return false;
        } else if (!functionName.equals(other.functionName))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }
}
