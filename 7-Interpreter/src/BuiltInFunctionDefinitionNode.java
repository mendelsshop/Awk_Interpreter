import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionNode {

    Function<HashMap<String, InterpreterDataType>, String> Execute;

    private Boolean varidiac;

    public BuiltInFunctionDefinitionNode(Function<HashMap<String, InterpreterDataType>, String> execute,
            Boolean varidiac) {
        // TODO: how do we check arrity
        super("", new LinkedList<>(), new LinkedList<>());
        Execute = execute;
        this.varidiac = varidiac;
    }
}
