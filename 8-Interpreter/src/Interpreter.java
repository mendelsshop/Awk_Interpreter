import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.Supplier;

public class Interpreter {
    private class LineManager {
        List<String> lines;
        // we could keep track of the number of lines by using NR, but this much easier
        // and more efficient b/c no string->number parsing
        int linesProcessed = 0;

        public LineManager(List<String> lines) {
            this.lines = lines;
        }

        public boolean SplitAndAssign() {
            // variables.clear();
            getGlobal("$)").setContents("");
            // TODO: should we clear N(FR|F|R)
            if (!lines.isEmpty()) {
                variables.put("NR", new InterpreterDataType("" + (++linesProcessed)));
                // TODO: since no multi-file support nr = nfr
                variables.put("NFR", new InterpreterDataType("" + linesProcessed));
                String text = lines.remove(0);
                var line = text.split(getGlobal("FS").getContents());
                variables.put("NR", new InterpreterDataType("" + lines.size()));
                // assign each variable to $n
                // assign lines and nf,fnr,nr
                // is $0 for whole line if so start from 1
                // also need to clear variables from previous line
                variables.put("$0", new InterpreterDataType(text));
                for (int i = 1; i <= line.length; i++) {
                    variables.put("$" + line, new InterpreterDataType(line[i]));
                }
                return true;
            }
            return false;
        }
    }

    // TODO: i assume that we need the program node for interpreter
    private ProgramNode program;
    private LineManager input;
    private HashMap<String, InterpreterDataType> variables = new HashMap<String, InterpreterDataType>() {
        {
            put("FS", new InterpreterDataType(" "));
            put("OFS", new InterpreterDataType(" "));
            put("OFT", new InterpreterDataType("%.6g"));
            put("ORS", new InterpreterDataType("\n"));
            // we dont set nr/nf/nfr as getglobal will auto assign them if accesed
        }
    };

    public InterpreterDataType getGlobal(String index) {
        return getVariable(index, variables);
    }

    private InterpreterDataType getVariable(String index, HashMap<String, InterpreterDataType> vars) {
        // TODO: maybe through nice exception if not variable
        return (vars.computeIfAbsent(index, u -> new InterpreterDataType()));
    }

    private InterpreterArrayDataType getArray(String index, HashMap<String, InterpreterDataType> vars) {
        // TODO: proper exception if not array
        if (vars.computeIfAbsent(index,
                u -> new InterpreterArrayDataType()) instanceof InterpreterArrayDataType array) {
            return array;
        } else {
            var contents = getVariable(index, vars);
            throw new AwkRuntimeError.ExpectedArray(index, contents.getContents());
        }
    }

    // TODO; should have functions for checking that pieces of data are of specific
    // data type
    // also need to make sure to not clone stuff (most of the time) so [g]sub
    // actualy replaces strings
    // TODO: tinterpreter needs custom exception b/c doesn't know line numbers
    // docs https://pubs.opengroup.org/onlinepubs/7908799/xcu/awk.html
    // slightly more formatted
    // https://manpages.ubuntu.com/manpages/focal/en/man1/awk.1posix.html
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {
            // TODO: builtin functions also need to figure out what each function does
            // TODO: printing arrays is not valid
            // for all the varidiac functions we can assume that the vardiac paramter is of
            // type InterpereterArrayDataType as the caller of each function knows to do
            // that
            put("print", new BuiltInFunctionDefinitionNode((vars) -> {
                InterpreterArrayDataType strings = getArray("strings", vars);
                System.out.println(
                        strings.getItemsStream().map(InterpreterDataType::toString).collect(Collectors.joining(" ")));
                return "";
            }, new LinkedList<>() {
                {
                    add("strings");
                }
            }, true));
            put("printf", new BuiltInFunctionDefinitionNode((vars) -> {
                String format = getVariable("format", vars).getContents();
                InterpreterArrayDataType strings = getArray("strings", vars);
                // how to use print to format elements of stream of strings by format
                System.out.printf(format, strings.getItemsStream().map(InterpreterDataType::toString).toArray());
                return "";
            }, new LinkedList<>() {
                {
                    add("format");
                    add("strings");
                }
            }, true));
            put("sprintf", new BuiltInFunctionDefinitionNode((vars) -> {
                String format = getVariable("format", vars).getContents();
                InterpreterArrayDataType strings = getArray("strings", vars);
                return format.formatted(strings.getItemsStream().map(InterpreterDataType::toString).toArray());
            }, new LinkedList<>() {
                {
                    add("format");
                    add("strings");
                }
            }, true));
            // are next and getline samething
            put("getline", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "",
                    new LinkedList<>(), false));
            put("next", new BuiltInFunctionDefinitionNode((vars) -> input.SplitAndAssign() ? "" : "",
                    new LinkedList<>(), false));
            @FunctionalInterface
            interface TriFunction<T, U, V, K> {
                K apply(T t, U u, V v);
            }
            Function<TriFunction<String, String, String, String>, BuiltInFunctionDefinitionNode> sub = (
                    replacer) -> new BuiltInFunctionDefinitionNode((vars) -> {
                        String pattern = getVariable("pattern", vars).getContents();
                        String replacement = (getVariable("replacement", vars)
                                .getContents());
                        InterpreterDataType target = (getArray("target", vars))
                                .getOptional("0")
                                .orElse(getGlobal("$0"));
                        target.setContents(replacer.apply(target.getContents(), pattern, replacement));
                        return "";
                    }, new LinkedList<>() {
                        {
                            add("pattern");
                            add("replacement");
                            add("target");
                        }
                    }, true);
            put("gsub", sub.apply(String::replaceAll));
            put("match", new BuiltInFunctionDefinitionNode((vars) -> {
                String haystack = getVariable("haystack", vars).getContents();
                String needle = getVariable("needle", vars).getContents();
                var pattern = Pattern.compile(needle);
                var matcher = pattern.matcher(haystack);
                boolean matches = matcher.matches();
                String index = String.valueOf(matches ? matcher.start() + 1 : 0);
                String length = String.valueOf((matches) ? matcher.end() - matcher.start() : -1);
                variables.put("RSTART", new InterpreterDataType(index));
                variables.put("RLENGTH", new InterpreterDataType(length));
                return index;
            }, new LinkedList<>() {
                {
                    add("haystack");
                    add("needle");
                }
            }, false));

            put("sub", sub.apply(String::replaceFirst));
            put("index", new BuiltInFunctionDefinitionNode((vars) -> {
                String haystack = getVariable("haystack", vars).getContents();
                String needle = getVariable("needle", vars).getContents();
                int index = haystack.indexOf(needle);
                return String.valueOf(index == -1 ? 0 : index + 1);
            }, new LinkedList<>() {
                {
                    add("haystack");
                    add("needle");
                }
            }, false));
            // defaults to $0 if nothing maybee
            put("length", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = (getArray("string", vars)).getOptional("0")
                        .orElse(getGlobal("$0"))
                        .getContents();
                return String.valueOf(string.length());
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, true));
            put("split", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = getVariable("string", vars).getContents();
                InterpreterArrayDataType array = getArray("array", vars);
                String sep = (getArray("sep", vars))
                        .getOptional("0").orElse(getGlobal("FS"))
                        .getContents();
                var strings = string.split(sep);
                int index = 0;
                for (String s : strings) {
                    array.insert(String.valueOf(index++), new InterpreterDataType((s)));
                }
                return "";
            }, new LinkedList<>() {
                {
                    add("string");
                    add("array");
                    add("sep");
                }
            }, true));
            put("substr", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = getVariable("string", vars).getContents();
                // TODO: handle parse number exceptions
                // we do start -1 b\c according to spec the start is 1-based index
                int start = Integer
                        .parseInt(getVariable("start", vars).getContents()) - 1;
                return (getArray("length", vars))
                        .getOptional("0")
                        .<String>map(n -> string.substring(start, start + Integer.parseInt(n.getContents())))
                        .orElse(string.substring(start));

            }, new LinkedList<>() {
                {
                    add("string");
                    add("start");
                    add("length");
                }
            }, true));
            Function<Function<String, String>, BuiltInFunctionDefinitionNode> strUpdate = (
                    mapper) -> new BuiltInFunctionDefinitionNode((vars) -> {
                        String string = getVariable("string", vars).getContents();
                        return mapper.apply(string);
                    }, new LinkedList<>() {
                        {
                            add("string");
                        }
                    }, false);
            put("tolower", strUpdate.apply(String::toLowerCase));
            put("toupper", strUpdate.apply(String::toUpperCase));
        }
    };

    public Interpreter(ProgramNode program, Optional<String> path) throws IOException {
        input = new LineManager(
                path.isPresent() ? Files.readAllLines(Paths.get(path.get())) : new LinkedList<String>());
        this.program = program;
        functions.putAll(
                program.getFunctions().stream().collect(Collectors.toMap(FunctionNode::getName, function -> function)));

    }

    private String RunFunctionCall(FunctionCallNode function, HashMap<String, InterpreterDataType> locals) {
        return "";
    }

    // TODO: prefer locals to be optional
    private InterpreterDataType GetIDT(Node value, HashMap<String, InterpreterDataType> locals) {

        switch (value) {
            case AssignmentNode a -> {
                var newValue = GetIDT(a.getExpression(), locals);
                return newValue;
            }
            case ConstantNode c -> {
                return new InterpreterDataType(c.getValue());
            }
            case FunctionCallNode f -> {
                return new InterpreterDataType(RunFunctionCall(f, locals));
            }
            case PatternNode p -> {
                throw new AwkRuntimeError.PatternError(p);
            }
            case TernaryOperationNode t -> {
                var cond = GetIDT(t.getCond(), locals);
                if (cond.getContents().trim() == "0" || cond.getContents().trim() == "") {
                    return GetIDT(t.getThen(), locals);
                } else {
                    return GetIDT(t.getAlt(), locals);
                }

            }
            // TODO: logic here got very messy with indexing + globals+locals
            case VariableReferenceNode v -> {
                return v.getIndex().<InterpreterDataType>map(i -> {
                    var index = GetIDT(i, locals).getContents();
                    if (locals == null) {
                        return getArray(v.getName(), variables).get(index);
                    } else {
                        InterpreterDataType orElseGet = Optional.ofNullable(locals.get(v.getName()))
                                .or(() -> Optional.ofNullable(variables.get(v.getName())))
                                .orElseGet(() -> locals.computeIfAbsent(v.getName(),
                                        u -> new InterpreterArrayDataType()));
                        if (orElseGet instanceof InterpreterArrayDataType va) {
                            return va.get(index);
                        } else {
                           throw new AwkRuntimeError.ExpectedArray(index, orElseGet.getContents());
 
                        }
                    }
                }).orElseGet(() -> {
                    if (locals == null) {
                        return getGlobal(v.getName());
                    } else {
                        var va = Optional.ofNullable(locals.get(v.getName()))
                                .or(() -> Optional.ofNullable(variables.get(v.getName())))
                                .orElseGet(() -> locals.computeIfAbsent(v.getName(),
                                        u -> new InterpreterDataType()));
                        return va;
                    }
                });
            }
            case OperationNode op -> {
                return GetIDT(op);
            }
            default -> {
                return null;
            }
        }
    }

    private InterpreterDataType GetIDT(OperationNode op ) {

    }
}
