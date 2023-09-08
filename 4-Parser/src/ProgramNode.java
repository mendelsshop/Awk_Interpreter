import java.util.LinkedList;

public class ProgramNode extends Node {
    @Override
    public String toString() {
        return "functions:\n" + functions + "\nBEGIN:\n" + BeginBlocks + "\n" + RestBlocks + "\nEND:\n" + EndBlocks;
    }

    private LinkedList<BlockNode> BeginBlocks;
    private LinkedList<BlockNode> EndBlocks;
    private LinkedList<BlockNode> RestBlocks;
    private LinkedList<FunctionNode> functions;

    public ProgramNode() {
        BeginBlocks = new LinkedList<BlockNode>();
        EndBlocks = new LinkedList<BlockNode>();
        RestBlocks = new LinkedList<BlockNode>();
        functions = new LinkedList<FunctionNode>();
    }

    public LinkedList<BlockNode> getBeginBlocks() {
        return BeginBlocks;
    }

    public LinkedList<BlockNode> getEndBlocks() {
        return EndBlocks;
    }

    public LinkedList<BlockNode> getRestBlocks() {
        return RestBlocks;
    }

    public LinkedList<FunctionNode> getFunctions() {
        return functions;
    }

    public void addToEnd(BlockNode node) {
        EndBlocks.add(node);
    };

    public void addToBegin(BlockNode node) {
        BeginBlocks.add(node);
    }

    public void addToRest(BlockNode node) {
        RestBlocks.add(node);
    }

    public void addFunction(FunctionNode node) {
        functions.add(node);
    }

}
