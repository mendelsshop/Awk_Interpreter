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
import java.util.function.BiFunction;

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
        return (variables.computeIfAbsent(index, u -> new InterpreterDataType()));
    }

    private InterpreterDataType getOrInit(String index, Optional<HashMap<String, InterpreterDataType>> vars,
            Supplier<InterpreterDataType> defaultValue) {
        return vars
                .map(v -> Optional.ofNullable(v.get(index)).or(() -> Optional.ofNullable(variables.get(index)))
                        .orElseGet(() -> v.computeIfAbsent(index, (u) -> defaultValue.get())))
                .orElseGet(() -> variables.computeIfAbsent(index, (u) -> defaultValue.get()));

    }

    private InterpreterDataType getVariable(String index, Optional<HashMap<String, InterpreterDataType>> vars) {
        return getOrInit(index, vars, () -> new InterpreterDataType());
    }

    private InterpreterDataType getVariable(String index, HashMap<String, InterpreterDataType> vars) {
        return getVariable(index, Optional.ofNullable(vars));
    }

    private InterpreterArrayDataType getArray(String index, HashMap<String, InterpreterDataType> vars) {
        return getArray(index, Optional.ofNullable(vars));
    }

    private InterpreterArrayDataType getArray(String index, Optional<HashMap<String, InterpreterDataType>> vars) {
        if (getOrInit(index, vars, () -> new InterpreterArrayDataType()) instanceof InterpreterArrayDataType array) {
            return array;
        } else {
            var contents = getVariable(index, vars);
            throw new AwkRuntimeError.ExpectedArrayError(index, contents.getContents());
        }
    }

    private <T> T parse(Function<String, T> parser, InterpreterDataType value) {
        try {
            return parser.apply(value.getContents());
        } catch (NumberFormatException e) {
            throw new AwkRuntimeError.ExpectedNumberError(value, e);
        }
    }

    @FunctionalInterface
    interface TriFunction<T, U, V, K> {
        K apply(T t, U u, V v);
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
            put("print", new BuiltInFunctionDefinitionNode("print", (vars) -> {
                InterpreterArrayDataType strings = getArray("strings", vars);
                System.out.println(
                        strings.getItemsStream().map(InterpreterDataType::toString).collect(Collectors.joining(" ")));
                return "";
            }, new LinkedList<>() {
                {
                    add("strings");
                }
            }, true));
            put("printf", new BuiltInFunctionDefinitionNode("printf", (vars) -> {
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
            put("sprintf", new BuiltInFunctionDefinitionNode("sprintf", (vars) -> {
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
            put("getline", new BuiltInFunctionDefinitionNode("getline", (vars) -> input.SplitAndAssign() ? "" : "",
                    new LinkedList<>(), false));
            put("next", new BuiltInFunctionDefinitionNode("next", (vars) -> input.SplitAndAssign() ? "" : "",
                    new LinkedList<>(), false));

            BiFunction<String, TriFunction<String, String, String, String>, BuiltInFunctionDefinitionNode> sub = (name,
                    replacer) -> new BuiltInFunctionDefinitionNode(name, (vars) -> {
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
            put("gsub", sub.apply("gsub", String::replaceAll));
            put("match", new BuiltInFunctionDefinitionNode("match", (vars) -> {
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

            put("sub", sub.apply("sub", String::replaceFirst));
            put("index", new BuiltInFunctionDefinitionNode("index", (vars) -> {
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
            put("length", new BuiltInFunctionDefinitionNode("length", (vars) -> {
                String string = (getArray("string", vars)).getOptional("0")
                        .orElse(getGlobal("$0"))
                        .getContents();
                return String.valueOf(string.length());
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, true));
            put("split", new BuiltInFunctionDefinitionNode("split", (vars) -> {
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
            put("substr", new BuiltInFunctionDefinitionNode("substr", (vars) -> {
                String string = getVariable("string", vars).getContents();
                // TODO: handle parse number exceptions
                // we do start -1 b\c according to spec the start is 1-based index
                int start = parse(Integer::parseInt, getVariable("start", vars)) - 1;
                return (getArray("length", vars))
                        .getOptional("0")
                        .<String>map(n -> string.substring(start, start + parse(Integer::parseInt, n)))
                        .orElse(string.substring(start));

            }, new LinkedList<>() {
                {
                    add("string");
                    add("start");
                    add("length");
                }
            }, true));
            BiFunction<String, Function<String, String>, BuiltInFunctionDefinitionNode> strUpdate = (name,
                    mapper) -> new BuiltInFunctionDefinitionNode(name, (vars) -> {
                        String string = getVariable("string", vars).getContents();
                        return mapper.apply(string);
                    }, new LinkedList<>() {
                        {
                            add("string");
                        }
                    }, false);
            put("tolower", strUpdate.apply("tolower", String::toLowerCase));
            put("toupper", strUpdate.apply("toupper", String::toUpperCase));
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
                    return getArray(v.getName(), locals).get(index);
                }).orElseGet(() -> getVariable(v.getName(), locals));
            }
            case OperationNode op -> {
                return GetIDT(op, locals);
            }
            default -> {
                return null;
            }
        }
    }

    private InterpreterDataType GetIDT(OperationNode op, HashMap<String, InterpreterDataType> locals) {
        // yield is used to return from a block
        // https://stackoverflow.com/questions/56806905/return-outside-of-enclosing-switch-expression
        TriFunction<Node, Node, BiFunction<Float, Float, Float>, InterpreterDataType> mathOp = (a, b,
                math) -> new InterpreterDataType("" + math.apply(
                        parse(Float::parseFloat, GetIDT(a, locals)), parse(Float::parseFloat, GetIDT(b, locals))));
        return switch (op.getOperation()) {
            case DOLLAR -> {
                var index = GetIDT(op.getLeft(), locals);
                yield getGlobal("$" + index);
            }
            case ADD -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x + y);
            case AND -> new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                    ? truthyValue(GetIDT(op.getRight().get(), locals).getContents())
                    : "0");
            case CONCATENATION -> new InterpreterDataType(
                    GetIDT(op.getLeft(), locals).getContents() + GetIDT(op.getRight().get(), locals));
            case DIVIDE -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x / y);
            case EQ -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case EXPONENT -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> (float) Math.pow(x, y));
            case GE -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case GT -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case IN -> {
                var index = GetIDT(op.getLeft(), locals).getContents();

                if (op.getRight().get() instanceof VariableReferenceNode v) {
                    var array = getArray(v.getName(), locals);
                    yield new InterpreterDataType(array.contains(index) ? "1" : "0");
                } else {
                    throw new AwkRuntimeError.ExpectedArrayError(index, "");
                }
            }
            case LE -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case LT -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case MATCH -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case MODULO -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x % y);
            case MULTIPLY -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x * y);
            case NE -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case NOT ->
                new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                        ? "0"
                        : "1");
            case NOTMATCH -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case OR ->
                new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                        ? "1"
                        : truthyValue(GetIDT(op.getRight().get(), locals).getContents()));
            case POSTDEC -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case POSTINC -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case PREDEC -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case PREINC -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case SUBTRACT -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x - y);
            case UNARYNEG -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            case UNARYPOS -> throw new UnsupportedOperationException("Unimplemented case: " + op.getOperation());
            default -> throw new IllegalArgumentException("Unexpected value: " + op.getOperation());

        };
    }

    private String truthyValue(String value) {
        return (Float.parseFloat(value) == 0.0) || (value == "") || (value == "0") ? "0" : "1";
    }
}
