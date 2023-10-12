import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionNode {

    Function<HashMap<String, InterpreterDataType>, String> Execute;

    public BuiltInFunctionDefinitionNode(String name, LinkedList<String> parameters,
            LinkedList<StatementNode> statements, Function<HashMap<String, InterpreterDataType>, String> execute) {
        super(name, parameters, statements);
        Execute = execute;
    }


    
}
