import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionNode {

    private Function<HashMap<String, InterpreterDataType>, String> Execute;

    private Boolean varidiac;

    public Function<HashMap<String, InterpreterDataType>, String> getExecute() {
        return Execute;
    }

    public Boolean getVaridiac() {
        return varidiac;
    }

    public BuiltInFunctionDefinitionNode(String name, Function<HashMap<String, InterpreterDataType>, String> execute,
        LinkedList<String> parameters,
            Boolean varidiac) {
        // TODO: how do we check arrity
        super(name, parameters);
        Execute = execute;
        this.varidiac = varidiac;
    }
}
