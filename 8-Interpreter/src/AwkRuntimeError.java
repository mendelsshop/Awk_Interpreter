// Decided to make each error indivdual class makes it easier to test what type of errors are being thrown
// TODO: make the inner classes accesable without/outer class
public abstract class AwkRuntimeError extends RuntimeException implements DisplayError {
    // we make them subclasses static which just means they don't have acccess to
    // the outer classes instance varaibles -> we can access the inner classes
    // without an instance to the outer class
    // in our class making the outer class act kinda of like a single file package
    // when not enough/to many arguments passed to a function
    public static class AwkArittyError extends AwkRuntimeError {
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

    public static class PatternError extends AwkRuntimeError {
        private PatternNode pattern;

        public PatternError(PatternNode pattern) {
            this.pattern = pattern;
        }

        @Override
        public String message() {
            return "Pattern " + pattern + "is invalid in this context";
        }

    }

    public static class ExpectedArray extends AwkRuntimeError {
        private String variable;
        private String contents;

        public ExpectedArray(String variable, String contents) {
            this.variable = variable;
            this.contents = contents;
        }

        @Override
        public String message() {
            return "Expeced " + variable + " to be array but was scalar with value" + contents;
        }

    }    // when not enough/to many arguments passed to a function
    public static class ExpectedScalarError extends AwkRuntimeError {
        private InterpreterArrayDataType value;

        public ExpectedScalarError(InterpreterArrayDataType value) {
            this.value = value;
        }

        @Override
        public String message() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'message'");
        }

    }
}
