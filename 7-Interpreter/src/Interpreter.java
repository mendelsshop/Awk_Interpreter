import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Function;

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
            if (!lines.isEmpty()) {
                variables.put("NR", new InterpreterDataType("" + (++linesProcessed)));
                // TODO: since no multi-file support nr = nfr
                variables.put("NFR", new InterpreterDataType("" + linesProcessed));
                String text = lines.remove(0);
                var line = text.split(variables.get("FS").getContents());
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
            put("NF", new InterpreterDataType());
            put("NFR", new InterpreterDataType());
            put("NR", new InterpreterDataType());
        }
    };
    // TODO; should have functions for checking that pieces of data are of specific
    // data type
    // and nned better way to interact with array dt
    // also need to make sure to not clone stuff (most of the time) so [g]sub
    // actualy replaces strings
    // TODO: tinterpreter needs custom exception b/c doesn't know line numbers
    // docs https://pubs.opengroup.org/onlinepubs/7908799/xcu/awk.html
    // slightly more formatted
    // https://manpages.ubuntu.com/manpages/focal/en/man1/awk.1posix.html
    // TODO: make function that get varidiac arguements contents (for varidaic
    // buitins with single thing that is varidac)
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {
            // TODO: builtin functions also need to figure out what each function does
            // TODO: printing arrays is not valid
            put("print", new BuiltInFunctionDefinitionNode((vars) -> {
                InterpreterArrayDataType strings = (InterpreterArrayDataType) vars.get("strings");
                System.out.println(
                        strings.getItemsStream().map(InterpreterDataType::toString).collect(Collectors.joining(" ")));
                return "";
            }, new LinkedList<>() {
                {
                    add("strings");
                }
            }, true));
            put("printf", new BuiltInFunctionDefinitionNode((vars) -> {
                String format = vars.get("format").getContents();
                InterpreterArrayDataType strings = (InterpreterArrayDataType) vars.get("strings");
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
                String format = vars.get("format").getContents();
                InterpreterArrayDataType strings = (InterpreterArrayDataType) vars.get("strings");
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
                        String pattern = vars.get("pattern").getContents();
                        String replacement = (vars.get("replacement").getContents());
                        InterpreterDataType target = ((InterpreterArrayDataType) vars.get("target")).get("0")
                                .orElse(variables.get("$0"));
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
                String haystack = vars.get("haystack").getContents();
                String needle = vars.get("needle").getContents();
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
                String haystack = vars.get("haystack").getContents();
                String needle = vars.get("needle").getContents();
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
                String string = ((InterpreterArrayDataType) vars.get("string")).get("0").orElse(variables.get("$0"))
                        .getContents();
                return String.valueOf(string.length());
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, true));
            put("split", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = vars.get("string").getContents();
                InterpreterArrayDataType array = (InterpreterArrayDataType) vars.get("array");
                String sep = ((InterpreterArrayDataType) vars.get("sep")).get("0").orElse(variables.get("FS"))
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
                String string = vars.get("string").getContents();
                // TODO: handle parse number exceptions
                // we do start -1 b\c according to spec the start is 1-based index
                int start = Integer.parseInt(vars.get("start").getContents()) - 1;
                return ((InterpreterArrayDataType) vars.get("length")).get("0")
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
                        String string = vars.get("string").getContents();
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
}
