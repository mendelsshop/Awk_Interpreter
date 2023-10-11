public class ForNode extends StatementNode {
    // TODO: allow emtpy expression
    private Optional<Node> init;
    private Optional<Node> condition;
    private Optional<Node> increment;
    private BlockNode block;

    public ForNode(Optional<Node> init, Optional<Node> condition, Optional<Node> increment, BlockNode block) {
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.block = block;
    }

    public Optional<Node> getInit() {
        return init;
    }

    public Optional<Node> getCondition() {
        return condition;
    }

    public Optional<Node> getIncrement() {
        return increment;
    }

    public BlockNode getBlock() {
        return block;
    }

    @Override
    public String toString() {
        return "for (" + init.map(i -> i.toString()).orElse("") + "; " + condition.map(i -> i.toString()).orElse("")
                + "; " + increment.map(i -> i.toString()).orElse("") + ")" + block;
    }
}
