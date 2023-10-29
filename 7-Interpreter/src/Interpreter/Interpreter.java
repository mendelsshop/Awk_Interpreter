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
    private class Record {
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
            if (!lines.isEmpty()) {
                var.setContents(lines.remove(0));
            }
            return !lines.isEmpty();
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
    private Record record;
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

    private class Next extends RuntimeException {

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
                        strings.getItemsStream().map(InterpreterDataType::getContents)
                                .collect(Collectors.joining(" ")));
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
                System.out.printf(format, strings.getItemsStream().map(InterpreterDataType::getContents).toArray());
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
                return format.formatted(strings.getItemsStream().map(InterpreterDataType::getContents).toArray());
            }, new LinkedList<>() {
                {
                    add("format");
                    add("strings");
                }
            }, true));
            // are next and getline samething
            // should getline be variadiac on a variable
            put("getline",
                    new BuiltInFunctionDefinitionNode("getline",
                            (vars) -> getArray("var", vars).getOptional("0").map(v -> input.assign(v))
                                    .orElseGet(() -> input.SplitAndAssign()) ? "1" : "0",
                            new LinkedList<>() {
                                {
                                    add("var");
                                }
                            }, true));
            // next should be a statementnode b/c it changes control flow (if the entire awk
            // program is essentialy a loop next is like a continue)
            put("next", new BuiltInFunctionDefinitionNode("next", (vars) -> {
                throw new Next();
            }, new LinkedList<>(), false));
            BiFunction<String, TriFunction<String, String, String, String>, BuiltInFunctionDefinitionNode> sub = (name,
                    replacer) -> new BuiltInFunctionDefinitionNode(name, (vars) -> {
                        String pattern = getVariable("pattern", vars).getContents();
                        String replacement = (getVariable("replacement", vars)
                                .getContents());
                        InterpreterDataType target = (getArray("target", vars))
                                .getOptional("0")
                                .orElse(record.Get(0));
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
                        .orElse(record.Get(0))
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
                        .getOptional("0")
                        .<String>map(n -> string.substring(start, start + parse(n).intValue()))
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
}
