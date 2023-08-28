import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        // Handleing case when no awk file is specified
        if (args.length < 2) {
            System.err.println("No awk file specified");
            System.exit(1);
        }
        Path myPath = Paths.get(args[1]);
        String content = new String(Files.readAllBytes(myPath));
        // TODO: its a little confusing who gets the source code does the Lex class get
        // it and in its constructor instiates a StringHandles or do we pass is to
        // StringHandler whic get passed to Lex
        Lexer lexer = new Lexer(content);

        // print result token stream
        lexer.lex().forEach(System.out::println);

    }
}
