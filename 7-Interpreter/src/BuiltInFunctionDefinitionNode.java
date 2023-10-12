import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionNode {

    Function<HashMap<String, InterpreterDataType>, String> Execute;

    private Boolean varidiac;

    public BuiltInFunctionDefinitionNode(String name, LinkedList<String> parameters,
            LinkedList<StatementNode> statements, Function<HashMap<String, InterpreterDataType>, String> execute,
            Boolean varidiac) {
        super(name, parameters, statements);
        Execute = execute;
        this.varidiac = varidiac;
    }    
}
