import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LexerException extends Exception {
    Optional<String> source = Optional.empty();
    Integer lineNumber = 0;
    Integer position = 0;
    String message = "";

    public LexerException(Integer lineNumber, Integer position, String message) {
        this.lineNumber = lineNumber;
        this.position = position;
        this.message = message;
    }

    public void DisplayError(String sourceFile, String sourceFileName) {
        System.err.println(sourceFileName + ":" + lineNumber + ":" + position + ": Error: " + message);
        sourceFile.lines().skip(lineNumber - 1).findFirst().ifPresent(line -> {
            System.err.println("\t" + lineNumber + " | " + line);
            System.err
                    .println("\t" + lineNumber.toString().chars().mapToObj(c -> " ").collect(Collectors.joining())
                            + " | " + IntStream.range(0, position - 1).mapToObj(c -> " ").collect(Collectors.joining())
                            + "^");
        });
        System.exit(1);
    }
}