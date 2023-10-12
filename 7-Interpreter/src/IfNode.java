public class IfNode extends StatementNode {
    private Node condition;
    private BlockNode thenBlock;
    private Optional<Node> otherwise;
    public IfNode(Node condition, BlockNode thenBlock, Optional<Node> otherwise) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.otherwise = otherwise;
    }
    public Node getCondition() {
        return condition;
    }
    public BlockNode getThenBlock() {
        return thenBlock;
    }
    public Optional<Node> getOtherwise() {
        return otherwise;
    }
    @Override
    public String toString() {
        return "if (" + condition + ")" + thenBlock + otherwise.map(i->"else" + i).orElse("");
    }
}
