import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private HashMap<String, FunctionNode> functions = new HashMap<String, FunctionNode>() {
        {

            // TODO: builtin functions also need to figure out what each function does
            // TODO: printing arrays is not valid
            // TODO: what do functiions that return nothing return? null? empty string? ..
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
            put("gsub", new BuiltInFunctionDefinitionNode((vars) -> {
                String pattern = vars.get("pattern").getContents();
                String replacement = (vars.get("replacement").getContents());
                InterpreterDataType target;
                // Substitute the string repl in place of the each instance of the extended
                // regular expression ERE in string in and return the number of substitutions.
                try {
                    target = ((InterpreterArrayDataType) vars.get("target")).getItemsList().get(0);
                } catch (IndexOutOfBoundsException e) {
                    target = variables.get("$0");
                }
                target.setContents(target.getContents().replaceAll(pattern, replacement));
                return "";
            }, new LinkedList<>() {
                {
                    add("pattern");
                    add("replacement");
                    add("target");
                }
            }, true));
            put("match", new BuiltInFunctionDefinitionNode((vars) -> {
                // TODO: maybe set rstart/rlength
                String haystack = vars.get("haystack").getContents();
                String needle = vars.get("needle").getContents();
                var pattern = Pattern.compile(needle);
                var matcher = pattern.matcher(haystack);
                return String.valueOf(matcher.matches() ? matcher.start() : 0);
            }, new LinkedList<>() {
                {
                    add("haystack");
                    add("needle");
                }
            }, false));
            put("sub", new BuiltInFunctionDefinitionNode((vars) -> {
                String pattern = vars.get("pattern").getContents();
                String replacement = (vars.get("replacement").getContents());
                InterpreterDataType target;
                // Substitute the string repl in place of the first instance of the extended
                // regular expression ERE in string in and return the number of substitutions.
                try {
                    target = ((InterpreterArrayDataType) vars.get("target")).getItemsList().get(0);
                } catch (IndexOutOfBoundsException e) {
                    target = variables.get("$0");
                }
                target.setContents(target.getContents().replaceFirst(pattern, replacement));
                return "";
            }, new LinkedList<>() {
                {
                    add("pattern");
                    add("replacement");
                    add("target");
                }
            }, true));
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
                String string = vars.get("string").getContents();
                return String.valueOf(string.length());
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, false));
            put("split", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = vars.get("string").getContents();
                InterpreterArrayDataType array = (InterpreterArrayDataType) vars.get("array");
                String sep;
                try {
                    sep = ((InterpreterArrayDataType) vars.get("sep")).getItemsList().get(0).getContents();
                } catch (IndexOutOfBoundsException e) {
                    sep = variables.get("FS").getContents();
                }
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
                int start = Integer.parseInt(vars.get("start").getContents());

                try {
                    int end = start + Integer.parseInt(
                            ((InterpreterArrayDataType) vars.get("length")).getItemsList().get(0).getContents());
                    // we do not need top check if endgreather than string length as that is index
                    // out of bounds and gets caught by catch
                    return string.substring(start, end);
                } catch (IndexOutOfBoundsException e) {
                    return string.substring(start);
                }

            }, new LinkedList<>() {
                {
                    add("string");
                    add("start");
                    add("length");
                }
            }, true));
            put("tolower", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = vars.get("string").getContents();
                return string.toLowerCase();
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, false));
            put("toupper", new BuiltInFunctionDefinitionNode((vars) -> {
                String string = vars.get("string").getContents();
                return string.toUpperCase();
            }, new LinkedList<>() {
                {
                    add("string");
                }
            }, false));
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
