public class ForEachNode extends StatementNode {
    private String index;

    private Node iterable;
    private BlockNode block;

    public ForEachNode(String index, Node iterable, BlockNode block) {
        this.index = index;
        this.iterable = iterable;
        this.block = block;
    }

    public Node getIterable() {
        return iterable;
    }

    public BlockNode getBlock() {
        return block;
    }

    public String getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "for (" + index + " in " + iterable + ")" + block;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((iterable == null) ? 0 : iterable.hashCode());
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
        ForEachNode other = (ForEachNode) obj;
        if (index == null) {
            if (other.index != null)
                return false;
        } else if (!index.equals(other.index))
            return false;
        if (iterable == null) {
            if (other.iterable != null)
                return false;
        } else if (!iterable.equals(other.iterable))
            return false;
        if (block == null) {
            if (other.block != null)
                return false;
        } else if (!block.equals(other.block))
            return false;
        return true;
    }

    
}
