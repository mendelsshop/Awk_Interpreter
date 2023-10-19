import java.io.PrintStream;

public interface DisplayError {
        public String message();

        default public void printMessage() {
            printMessage(System.out);
        }

        default public void printMessage(PrintStream printer) {
            printer.println("Awk Runtime Error: " + message());
        }
    }