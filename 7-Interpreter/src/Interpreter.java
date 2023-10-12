import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter {
    private class LineManager {
        List<String> lines;

        public LineManager(List<String> lines) {
            this.lines = lines;
        }

        public boolean SplitAndAssign() {
            if (!lines.isEmpty()) {
                var line = lines.remove(0).split(variables.get("FS").getContents());
                // assign each variable to $n
                // assign lines and nf,fnr,nr
                // is $0 for whole line if so start from 1
                // also need to clear variables from previous line
                for (int i = 0; i < line.length; i++) {
                    variables.put("$" + line, new InterpreterDataType(line[i]));
                }
                return true;
            }
            return false;
        }
    }

    private ProgramNode program;
    private LineManager input;
    private HashMap<String, InterpreterDataType> variables = new HashMap<String, InterpreterDataType>() {
        {
            put("FS", new InterpreterDataType(" "));
            put("OFS", new InterpreterDataType(" "));
            put("OFT", new InterpreterDataType("%.6g"));
            put("ORS", new InterpreterDataType("\n"));
            put("NF", new InterpreterDataType());
            put("NFR", new InterpreterDataType());
            put("NR", new InterpreterDataType());
        }
    };
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {
            // TODO: builtin functions
            put("print", new BuiltInFunctionDefinitionNode((vars) -> "", true));
            put("printf", new BuiltInFunctionDefinitionNode((vars) -> "", true));
            put("getline", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "", false));
            put("next", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "", false));
            put("gsub", null);
            put("match", null);
            put("sub", null);
            put("index", null);
            put("length", null);
            put("split", null);
            put("substr", null);
            put("tolower", null);
            put("toupper", null);
        }
    };

    public Interpreter(ProgramNode program, Optional<String> path) throws IOException {
        input = new LineManager(
                path.isPresent() ? Files.readAllLines(Paths.get(path.get())) : new LinkedList<String>());
        this.program = program;
        functions.putAll(
                program.getFunctions().stream().collect(Collectors.toMap(FunctionNode::getName, function -> function)));

    }
}
