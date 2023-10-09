public class OperationNode extends StatementNode {
    @Override
    public String toString() {
        return "("+  (Right.isPresent() ? Left + " " + operation + " " + Right.get() : operation + " " + Left) + ")";
    }

    // and a list of possible operations – use an enum for this. Mine are:
    // does this mean a list of operations
    // or single operation (i assume that it's a single operation)
    private Operation operation;
    private Node Left;

    public Operation getOperation() {
        return operation;
    }

    public Node getLeft() {
        return Left;
    }

    public OperationNode(OperationNode.Operation operation, Node left) {
            this.operation = operation;
        Left = left;

    }

    private Optional<Node> Right = Optional.empty();

    public Optional<Node> getRight() {
        return Right;
    }

    public OperationNode(OperationNode.Operation operation, Node left, Node right) {
        this.operation = operation;
        Left = left;
        Right = Optional.of(right);
    }

    public enum Operation {
        EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, CONCATENATION
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((Left == null) ? 0 : Left.hashCode());
        result = prime * result + ((Right == null) ? 0 : Right.hashCode());
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
        OperationNode other = (OperationNode) obj;
        if (operation != other.operation)
            return false;
        if (Left == null) {
            if (other.Left != null)
                return false;
        } else if (!Left.equals(other.Left))
            return false;
        if (Right == null) {
            if (other.Right != null)
                return false;
        } else if (!Right.equals(other.Right))
            return false;
        return true;
    }
}
