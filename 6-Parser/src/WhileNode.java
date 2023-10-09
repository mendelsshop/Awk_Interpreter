public class WhileNode extends StatementNode {
    private Node condition;
    private BlockNode block;
    public WhileNode(Node condition, BlockNode block) {
        this.condition = condition;
        this.block = block;
    }
    public Node getCondition() {
        return condition;
    }
    public BlockNode getBlock() {
        return block;
    }
}
