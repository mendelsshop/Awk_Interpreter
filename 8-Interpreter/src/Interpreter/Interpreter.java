import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Interpreter {
    @FunctionalInterface
    interface TriFunction<T, U, V, K> {
        K apply(T t, U u, V v);
    }

    /// mutability
    // scalars passed into builtins can effect outside call
    // scalars passed into user defined function cannot cannot change outside call
    /// (done by calling Clone on IDT at function call time)
    // arrays will always will always modify outside outside call
    // something assigned to an index of an array cannot modify the what that value

    // for managing $0 ,$n
    // public for testing
    class Record {
        private LinkedList<Field> fields;
        private HeadField record;

        public Record(String record) {
            ProcessRecord(record);
        }

        private void ProcessRecord(String record) {
            this.record = new HeadField(record);
            String[] fields = record.split(getGlobal("FS").getContents());
            variables.put("NF", new InterpreterDataType(fields.length));
            this.fields = Stream.of(fields).map(f -> new Field(f))
                    .collect(Collectors.toCollection(() -> new LinkedList<>()));
        }

        // we assume non negative b/c this will only be used in GetIDT and we can verify
        // that there
        public InterpreterDataType Get(int index) {
            return switch (index) {
                case 0 -> record;
                default ->
                    Optional.ofExceptionable(() -> fields.get(index - 1)).orElseGet(() -> {
                        // the second you update the record any witespace from record input ges removed
                        record.updateRecord(fields.stream().map(Field::getContents).collect(Collectors.joining(" ")));
                        Stream.iterate(fields.size() - 2 - index, n -> n < index - 1, n -> n + 1)
                                .map(f -> new Field("")).forEach(f -> fields.add(f));
                        return fields.get(index - 1);
                    });

            };
        }

        // reprsents $1 overides IDT, so setting it can do special things
        private class HeadField extends InterpreterDataType {
            public HeadField(String contents) {
                super(contents);
            }

            private void updateRecord(String input) {
                super.setContents(input);
            }

            @Override
            public void setContents(String contents) {
                // we only resplit on fs if we update whole record
                super.setContents(contents);
                ProcessRecord(contents);
            }

            // cloning a $0 means you get the contents but not tha ability to modify the
            // current record
            @Override
            public InterpreterDataType Clone() {
                return super.Clone();
            }
        }

        // reprsents $n overides IDT, so setting it can do special things
        private class Field extends InterpreterDataType {
            public Field(String contents) {
                super(contents);

            }

            @Override
            public void setContents(String contents) {
                super.setContents(contents);
                // the second you update the record any witespace from record input ges removed
                record.updateRecord(fields.stream().map(Field::getContents).collect(Collectors.joining(" ")));

            }

            // cloning a $n means you get the contents but not tha ability to modify the
            // current record
            @Override
            public InterpreterDataType Clone() {
                return super.Clone();
            }

        }
    }

    private class LineManager {
        private List<String> lines;
        // we could keep track of the number of lines by using NR, but this much easier
        // and more efficient b/c no string->number parsing
        // int linesProcessed = 0;

        public LineManager(List<String> lines) {
            this.lines = lines;
        }

        // used for getline with variable
        public boolean assign(InterpreterDataType var) {
            var left = !lines.isEmpty();
            if (left) {
                var.setContents(lines.remove(0));
            }
            return left;
        }

        public boolean SplitAndAssign() {
            if (!lines.isEmpty()) {
                Consumer<String> update = (n) -> {
                    var nref = getGlobal(n);
                    try {
                        // if this is first record "" gets parsed to 0 (0 + 1) = 1
                        nref.setContents(parse(nref) + 1);
                    } catch (AwkRuntimeError.ExpectedNumberError e) {
                        nref.setContents("1");
                    }
                };
                update.accept("NR");
                update.accept("NFR");
                String text = lines.remove(0);
                variables.put("NF", new InterpreterDataType("" + lines.size()));
                // assign each variable to $n
                // assign lines and nf,fnr,nr
                // is $0 for whole line if so start from 1
                // also need to clear variables from previous line
                record = new Record(text);
                return true;
            }
            return false;
        }
    }

    private ProgramNode program;
    private LineManager input;

    // public for testing purposes
    public void setInput(String input) {
        this.input = new LineManager(new LinkedList<>(List.of(input.split("\n"))));
    }

    // public for testing purposes
    public Record getRecord() {
        return record;
    }

    private HashMap<String, InterpreterDataType> variables = new HashMap<String, InterpreterDataType>() {
        {
            put("FS", new InterpreterDataType(" "));
            put("OFS", new InterpreterDataType(" "));
            put("OFT", new InterpreterDataType("%.6g"));
            put("ORS", new InterpreterDataType("\n"));
            // we dont set nr/nf/nfr as getglobal will auto assign them if accesed
        }
    };

    // has to be after variables or else using new record freaks out about variables
    // being null1
    // your allowed to play with $0 $n in begin and end blocks via getline
    private Record record = new Record("");

    // public for testing purposes
    public InterpreterDataType getGlobal(String index) {
        return (variables.computeIfAbsent(index, u -> new InterpreterDataType()));
    }

    // awk allows for inventing varaibles so get or init
    // will attemptt to find the varaible or create otherwise in the samllest scope
    // possible
    // basis for get(Varaible|Array)
    private InterpreterDataType getOrInit(String index, Optional<HashMap<String, InterpreterDataType>> vars,
            Supplier<InterpreterDataType> defaultValue) {
        return vars
                .map(v -> Optional.ofNullable(v.get(index)).or(() -> Optional.ofNullable(variables.get(index)))
                        .orElseGet(() -> v.computeIfAbsent(index, (u) -> defaultValue.get())))
                .orElseGet(() -> variables.computeIfAbsent(index, (u) -> defaultValue.get()));

    }

    // public for testing purposes
    public InterpreterDataType getVariable(String index, Optional<HashMap<String, InterpreterDataType>> vars) {
        return getOrInit(index, vars, () -> new InterpreterDataType());
    }

    private InterpreterDataType getVariable(String index, HashMap<String, InterpreterDataType> vars) {
        return getVariable(index, Optional.ofNullable(vars));
    }

    // public for testing purposes
    public InterpreterArrayDataType getArray(String index, HashMap<String, InterpreterDataType> vars) {
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

    // real awk uses c's atof which allow for "" -> 0 instead of an error
    private Float parse(InterpreterDataType value) {
        var string = value.getContents().trim();
        try {
            if (string.isBlank()) {
                return 0f;
            }
            return Float.parseFloat(string);
        } catch (NumberFormatException e) {
            throw new AwkRuntimeError.ExpectedNumberError(value, e);
        }
    }

    // public for testing next
    // used for singalling a next staements has appeared - wont get handled till
    // interpeter 4
    public class Next extends RuntimeException {

    }

    // docs https://pubs.opengroup.org/onlinepubs/7908799/xcu/awk.html
    // slightly more formatted
    // https://manpages.ubuntu.com/manpages/focal/en/man1/awk.1posix.html
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {
            // for all the varidiac functions we can assume that the vardiac paramter is of
            // type InterpereterArrayDataType as the caller of each function knows to do
            // that
            // we can also assume that all the variables are present as the caller of each
            // function knows to do that

            // and that the correct varidac paramter is passed in
            // as the for print, printf, sprintf we accept any number of strings
            // for the others the getOptional on IADT checks that for us

            // prints list of strings to stdout + newline
            put("print", new BuiltInFunctionDefinitionNode("print", (vars) -> {
                InterpreterArrayDataType strings = getArray("strings", vars);
                System.out.println(
                        strings.getItemsStream().map(InterpreterDataType::getContents)
                                .collect(Collectors.joining(" ")));
                return "";
            }, new LinkedList<>() {
                {
                    add("strings");
                }
            }, true));

            // prints list of strings formatted by format to stdout
            put("printf", new BuiltInFunctionDefinitionNode("printf", (vars) -> {
                String format = getVariable("format", vars).getContents();
                InterpreterArrayDataType strings = getArray("strings", vars);
                // how to use print to format elements of stream of strings by format
                System.out.printf(format, strings.getItemsStream().map(InterpreterDataType::getContents).toArray());
                return "";
            }, new LinkedList<>() {
                {
                    add("format");
                    add("strings");
                }
            }, true));

            // returns list of strings formatted by format
            put("sprintf", new BuiltInFunctionDefinitionNode("sprintf", (vars) -> {
                String format = getVariable("format", vars).getContents();
                InterpreterArrayDataType strings = getArray("strings", vars);
                return format.formatted(strings.getItemsStream().map(InterpreterDataType::getContents).toArray());
            }, new LinkedList<>() {
                {
                    add("format");
                    add("strings");
                }
            }, true));

            // if nothing passed reset record ($0, $n)
            // otherwise sets variable to new line
            // returns 0 if no lines left otherwise 1
            put("getline",
                    new BuiltInFunctionDefinitionNode("getline",
                            (vars) -> getArray("var", vars).getOptional("getline").map(v -> input.assign(v))
                                    // if no var passed -> $0
                                    .orElseGet(() -> input.SplitAndAssign()) ? "1" : "0",
                            new LinkedList<>() {
                                {
                                    add("var");
                                }
                            }, true));

            // we through next aand will handl in later assignments
            put("next", new BuiltInFunctionDefinitionNode("next", (vars) -> {
                throw new Next();
            }, new LinkedList<>(), false));

            // function for [g?]sub
            // mutates passed in string or $0 (b/c when buitins are called they do not
            // clone)
            BiFunction<String, TriFunction<String, String, String, String>, BuiltInFunctionDefinitionNode> sub = (name,
                    replacer) -> new BuiltInFunctionDefinitionNode(name, (vars) -> {
                        String pattern = getVariable("pattern", vars).getContents();
                        String replacement = (getVariable("replacement", vars)
                                .getContents());
                        InterpreterDataType target = (getArray("target", vars))
                                .getOptional(name)
                                // if no target passed -> $0
                                .orElseGet(() -> record.Get(0));
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
                boolean matches = matcher.find();
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
            // returns 1 based index of needle in haystack if present otherwise 0
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
            // defaults to $0
            put("length", new BuiltInFunctionDefinitionNode("length", (vars) -> {
                String string = (getArray("string", vars)).getOptional("length")
                        .orElseGet(() -> record.Get(0))
                        .getContents();
                return String.valueOf(string.length());
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, true));
            // varidac over seperator (defaults to FS)
            put("split", new BuiltInFunctionDefinitionNode("split", (vars) -> {
                String string = getVariable("string", vars).getContents();
                InterpreterArrayDataType array = getArray("array", vars);
                array.clear();
                String sep = (getArray("sep", vars))
                        // if no sep passed -> FS
                        .getOptional("split").orElse(getGlobal("FS"))
                        .getContents();
                var strings = string.split(sep);
                int index = 0;
                for (String s : strings) {
                    // indicies start mostly at one in awk
                    array.insert(String.valueOf(++index), new InterpreterDataType((s)));
                }
                return "" + strings.length;
            }, new LinkedList<>() {
                {
                    add("string");
                    add("array");
                    add("sep");
                }
            }, true));
            put("substr", new BuiltInFunctionDefinitionNode("substr", (vars) -> {
                String string = getVariable("string", vars).getContents();
                // we do start -1 b\c according to spec the start is 1-based index
                int start = parse(getVariable("start", vars)).intValue() - 1;
                return (getArray("length", vars))
                        .getOptional("substr")
                        .<String>map(n -> string.substring(start, start + parse(n).intValue()))
                        // if no length -> just go to end of string
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

    // public for testing purposes
    public FunctionNode getFunction(String function) {
        return functions.get(function);
    }

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
    // package visibile for unit tests
    InterpreterDataType GetIDT(Node value, HashMap<String, InterpreterDataType> locals) {

        switch (value) {
            case AssignmentNode a -> {
                var newValue = GetIDT(a.getExpression(), locals);
                checkAssignAble(a.getTarget());
                // we can really only assign to scalar
                // we inteninall setcontents and getcontents so assignment doesnt modify
                // original variable
                GetIDT(a.getTarget(), locals).setContents(newValue.getContents());
                return newValue;
            }
            case ConstantNode c -> {
                // TODO: if its number truncate n.0 to n
                return new InterpreterDataType(c.getValue());
            }
            case FunctionCallNode f -> {
                return new InterpreterDataType(RunFunctionCall(f, locals));
            }
            case PatternNode p -> {
                throw new AwkRuntimeError.PatternError(p);
            }
            case TernaryOperationNode t -> {
                return truthyValue(GetIDT(t.getCond(), locals).getContents()) == "1" ? GetIDT(t.getThen(), locals)
                        : GetIDT(t.getAlt(), locals);

            }
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

    private void checkAssignAble(Node value) {
        if (value instanceof VariableReferenceNode var || value instanceof OperationNode op
                && op.getOperation() == OperationNode.Operation.DOLLAR) {

        } else {
            throw new AwkRuntimeError.NotAVariableError(value);
        }

    }

    private InterpreterDataType GetIDT(OperationNode op, HashMap<String, InterpreterDataType> locals) {
        // yield is used to return from a block
        // https://stackoverflow.com/questions/56806905/return-outside-of-enclosing-switch-expression
        TriFunction<Node, Node, BiFunction<Float, Float, Float>, InterpreterDataType> mathOp = (a, b,
                math) -> new InterpreterDataType(math.apply(
                        parse(GetIDT(a, locals)), parse(GetIDT(b, locals))));
        TriFunction<Node, Boolean, Function<Float, Float>, InterpreterDataType> opAssign = (v, pre, math) -> {
            checkAssignAble(v);
            var variable = GetIDT(v, locals);
            var oldValue = parse(variable);
            var newValue = math.apply(oldValue);
            variable.setContents(newValue);
            return new InterpreterDataType((pre ? oldValue : newValue));
        };
        BiFunction<String, Node, String> match = (string, pattern) -> {
            if (pattern instanceof PatternNode p) {
                return Pattern.matches(p.getPattern(), string) ? "1" : "0";
            }
            throw new AwkRuntimeError.ExpectedPatternError(pattern);
        };
        TriFunction<Node, Node, Function<Integer, Boolean>, InterpreterDataType> compare = (
                x, y, comparator) -> {
            var new_x = GetIDT(x, locals);
            var new_y = GetIDT(y, locals);
            try {
                return new InterpreterDataType(comparator.apply(parse(new_x).compareTo(parse(new_y))) ? "1" : "0");
            } catch (AwkRuntimeError.ExpectedNumberError e) {
                return new InterpreterDataType(
                        comparator.apply(new_x.getContents().compareTo(new_y.getContents())) ? "1" : "0");
            }

        };
        return switch (op.getOperation()) {
            case DOLLAR -> {
                var index = parse(GetIDT(op.getLeft(), locals));
                if (index < 0) {
                    throw new AwkRuntimeError.NegativeFieldIndexError(op, index.intValue());
                }
                yield record.Get(index.intValue());
            }
            case ADD -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x + y);
            case AND -> new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                    ? truthyValue(GetIDT(op.getRight().get(), locals).getContents())
                    : "0");
            case CONCATENATION -> new InterpreterDataType(
                    GetIDT(op.getLeft(), locals).getContents() + GetIDT(op.getRight().get(), locals));
            case DIVIDE -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x / y);
            case EQ -> compare.apply(op.getLeft(), op.getRight().get(), c -> c == 0);
            case EXPONENT -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> (float) Math.pow(x, y));
            case GE -> compare.apply(op.getLeft(), op.getRight().get(), c -> c >= 0);
            case GT -> compare.apply(op.getLeft(), op.getRight().get(), c -> c > 0);

            case IN -> {
                var index = GetIDT(op.getLeft(), locals).getContents();
                if (op.getRight().get() instanceof VariableReferenceNode v) {
                    // through error if the index is present since we do not support mutlidimensional arrays
                    if (v.getIndex().isPresent()) {
                        throw new AwkRuntimeError.ExpectedArrayError(index, "");
                    }
                    var array = getArray(v.getName(), locals);
                    yield new InterpreterDataType(array.contains(index) ? "1" : "0");
                } else {
                    throw new AwkRuntimeError.ExpectedArrayError(index, "");
                }
            }
            case LE -> compare.apply(op.getLeft(), op.getRight().get(), c -> c <= 0);
            case LT -> compare.apply(op.getLeft(), op.getRight().get(), c -> c < 0);
            case MATCH -> new InterpreterDataType(match.apply(GetIDT(op.getLeft(), locals).getContents(),
                   op.getRight().get()));
            case MODULO -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x % y);
            case MULTIPLY -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x * y);
            case NE -> compare.apply(op.getLeft(), op.getRight().get(), c -> c != 0);
            case NOT ->
                new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                        ? "0"
                        : "1");
            case NOTMATCH -> new InterpreterDataType(match.apply(GetIDT(op.getLeft(), locals).getContents(),
                    op.getRight().get()) == "1" ? "0" : "1");
            case OR ->
                new InterpreterDataType(truthyValue(GetIDT(op.getLeft(), locals).getContents()) == "1"
                        ? "1"
                        : truthyValue(GetIDT(op.getRight().get(), locals).getContents()));
            case POSTDEC -> opAssign.apply(op.getLeft(), false, (x) -> x - 1);
            case POSTINC -> opAssign.apply(op.getLeft(), false, (x) -> x + 1);
            case PREDEC -> opAssign.apply(op.getLeft(), true, (x) -> x - 1);
            case PREINC -> opAssign.apply(op.getLeft(), true, (x) -> x + 1);
            case SUBTRACT -> mathOp.apply(op.getLeft(), op.getRight().get(), (x, y) -> x - y);
            case UNARYNEG -> new InterpreterDataType((-parse(GetIDT(op.getLeft(), locals))));
            // unary pos is just used to check that a IDT is a numberish
            case UNARYPOS -> new InterpreterDataType(parse(GetIDT(op.getLeft(), locals)));
            default -> throw new IllegalArgumentException("Unexpected value: " + op.getOperation());

        };
    }

    private String truthyValue(String value) {
        try {
            return (Float.parseFloat(value) == 0.0) ? "0" : "1";
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}