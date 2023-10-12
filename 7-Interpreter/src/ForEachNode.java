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

    
}
