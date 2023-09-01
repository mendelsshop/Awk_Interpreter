import java.util.LinkedList;

public class ProgramNode extends Node {
    @Override
    public String toString() {
        return "functions=" + functions + "BEGIN" + BeginBlocks + RestBlocks + "END" + EndBlocks;
    }

    private LinkedList<BlockNode> BeginBlocks;
    private LinkedList<BlockNode> EndBlocks;
    private LinkedList<BlockNode> RestBlocks;
    private LinkedList<FunctionNode> functions;
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
