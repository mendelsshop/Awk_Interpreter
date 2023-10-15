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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result + ((thenBlock == null) ? 0 : thenBlock.hashCode());
        result = prime * result + ((otherwise == null) ? 0 : otherwise.hashCode());
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
        IfNode other = (IfNode) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (thenBlock == null) {
            if (other.thenBlock != null)
                return false;
        } else if (!thenBlock.equals(other.thenBlock))
            return false;
        if (otherwise == null) {
            if (other.otherwise != null)
                return false;
        } else if (!otherwise.equals(other.otherwise))
            return false;
        return true;
    }
}
