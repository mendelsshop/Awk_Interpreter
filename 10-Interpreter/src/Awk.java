import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Optional;

public class Awk {
    public static void main(String[] args) {
        // Handleing case when no awk file is specified
        if (args.length < 2) {
            System.err.println("No awk file specified");
            System.exit(1);
        }
        Path myPath = Paths.get(args[1]);
        String content;
        try {
            content = new String(Files.readAllBytes(myPath));

            try {
                Lexer lexer = new Lexer(content);
                // print result token stream
                LinkedList<Token> lex = lexer.lex();
                var parser = new Parser(lex);
                var interpreter = new Interpreter(parser.Parse(), Optional.ofNullable(args.length == 3 ? args[2] : null));
                interpreter.InterpretProgram();
            } catch (AwkException e) {
                e.DisplayError(content, myPath.toString());
            } catch (AwkRuntimeError e) {
               System.err.println("Awk runtime error:\n"+ e.message());
            }
        } catch (IOException e) {
            System.err.println("Error while reading awk file: " + myPath.toString() + ": " + e.getMessage());
        }
    }
}
