import java.util.LinkedList;

public class ProgramNode extends Node {
    @Override
    public String toString() {
        return "functions=" + functions + "BEGIN" + BEGIN + other + "END" + END;
    }

    private LinkedList<BlockNode> BEGIN;
    private LinkedList<BlockNode> END;
    private LinkedList<BlockNode> other;
    private LinkedList<FunctionNode> functions;
    public LinkedList<BlockNode> getBEGIN() {
        return BEGIN;
    }
    public LinkedList<BlockNode> getEND() {
        return END;
    }
    public LinkedList<BlockNode> getOther() {
        return other;
    }
    public LinkedList<FunctionNode> getFunctions() {
        return functions;
    }

}
