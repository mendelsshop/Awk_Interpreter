public class ForNode extends StatementNode {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((init == null) ? 0 : init.hashCode());
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((increment == null) ? 0 : increment.hashCode());
        result = prime * result + ((block == null) ? 0 : block.hashCode());
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
        ForNode other = (ForNode) obj;
        if (init == null) {
            if (other.init != null)
                return false;
        } else if (!init.equals(other.init))
            return false;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (increment == null) {
            if (other.increment != null)
                return false;
        } else if (!increment.equals(other.increment))
            return false;
        if (block == null) {
            if (other.block != null)
                return false;
        } else if (!block.equals(other.block))
            return false;
        return true;
    }
}
