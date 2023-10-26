import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

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
                System.out.println(parser.Parse());

            } catch (AwkException e) {
                e.DisplayError(content, myPath.toString());
            }
        } catch (IOException e) {
            System.err.println("Error while reading awk file: " + myPath.toString() + ": " + e.getMessage());
        }
    }
}
