public class ReturnNode extends StatementNode {
    private Optional<Node> ReturnValue;

    public Optional<Node> getReturnValue() {
        return ReturnValue;
    }

    public ReturnNode(Optional<Node> returnValue) {
        ReturnValue = returnValue;
    }

    @Override
    public String toString() {
        return "return " + ReturnValue.map(i -> i.toString()).orElse("");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ReturnValue == null) ? 0 : ReturnValue.hashCode());
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
        ReturnNode other = (ReturnNode) obj;
        if (ReturnValue == null) {
            if (other.ReturnValue != null)
                return false;
        } else if (!ReturnValue.equals(other.ReturnValue))
            return false;
        return true;
    }
}
