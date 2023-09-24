import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ProgramNode extends Node {
    @Override
    public String toString() {
        BiFunction<LinkedList<? extends Node>, String, String> listTosString = (s, p) -> s.stream()
                .map(c -> p + c.toString()).collect(Collectors.joining("\n"));
        return listTosString.apply(functions, "") + "\n" + listTosString.apply(BeginBlocks, "BEGIN ") + "\n"
                + listTosString.apply(RestBlocks, "") + "\n" + listTosString.apply(EndBlocks, "END ");
    }

    private LinkedList<BlockNode> BeginBlocks = new LinkedList<>();
    private LinkedList<BlockNode> EndBlocks = new LinkedList<>();
    private LinkedList<BlockNode> RestBlocks = new LinkedList<>();
    private LinkedList<FunctionNode> functions = new LinkedList<>();

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
