public class ReturnNode extends StatementNode {
    private Optional<Node> ReturnValue;

    public Optional<Node> getReturnValue() {
        return ReturnValue;
    }

    public ReturnNode(Optional<Node> returnValue) {
        ReturnValue = returnValue;
    }
}
