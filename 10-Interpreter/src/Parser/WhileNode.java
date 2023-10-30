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

    @Override
    public String toString() {
        return "while (" + condition + ")" + block;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
        WhileNode other = (WhileNode) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (block == null) {
            if (other.block != null)
                return false;
        } else if (!block.equals(other.block))
            return false;
        return true;
    }
}
