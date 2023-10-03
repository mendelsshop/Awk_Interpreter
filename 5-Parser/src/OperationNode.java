public class OperationNode extends StatementNode {
    @Override
    public String toString() {
        return Right.isPresent() ? Left + " " + operation + " " + Right.get() : operation + " " + Left;
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
}
