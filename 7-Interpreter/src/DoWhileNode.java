public class DoWhileNode extends StatementNode {
    private Node condition;
    private BlockNode block;

    public DoWhileNode(Node condition, BlockNode block) {
        this.condition = condition;
        this.block = block;
    }

    public Node getCondition() {
        return condition;
    }

    public BlockNode getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "do" + block + " while " + condition;
    }

}
