import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AwkException extends Exception {

    enum ExceptionType {
        LexicalError,
        ParseError,
    }

    Optional<String> source = Optional.empty();
    Integer lineNumber = 0;
    Integer position = 0;
    String message = "";
    private AwkException.ExceptionType exception;

    public AwkException(Integer lineNumber, Integer position, String message, ExceptionType exception) {
        this.lineNumber = lineNumber;
        this.position = position;
        this.message = message;
        this.exception = exception;
    }

    public void DisplayError(String sourceFile, String sourceFileName) {
        System.err.println(sourceFileName + ":" + lineNumber + ":" + position + ":" + exception + ": " + message);
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