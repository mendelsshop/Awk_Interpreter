public class ForNode extends StatementNode {
    private Node init;
    private Node condition;
    private Node increment;
    private BlockNode block;
    public ForNode(Node init, Node condition, Node increment, BlockNode block) {
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.block = block;
    }
    public Node getInit() {
        return init;
    }
    public Node getCondition() {
        return condition;
    }
    public Node getIncrement() {
        return increment;
    }
    public BlockNode getBlock() {
        return block;
    }
}
