// Decided to make each error indivdual class makes it easier to test what type of errors are being thrown

public abstract class AwkRuntimeError extends Exception implements DisplayError {

    // when not enough/to many arguments passed to a function
    public class AwkArittyError extends AwkRuntimeError {
        String functionName;
        int expected;
        int found;
        boolean varidiac;

        public AwkArittyError(String functionName, int expected, int found, boolean varidiac) {
            this.functionName = functionName;
            this.expected = expected;
            this.found = found;
            this.varidiac = varidiac;
        }

        @Override
        public String message() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'message'");
        }

    }
}
