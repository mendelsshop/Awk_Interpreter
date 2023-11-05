public abstract class AwkRuntimeError extends RuntimeException implements DisplayError {
    // we make them subclasses static which just means they don't have acccess to
    // the outer classes instance varaibles -> we can access the inner classes
    // without an instance to the outer class
    // in our class making the outer class act kinda of like a single file package

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

    public static class ExpectedScalarError extends AwkRuntimeError {
        private InterpreterArrayDataType value;

        public ExpectedScalarError(InterpreterArrayDataType value) {
            this.value = value;
        }

        @Override
        public String message() {
            return "Expected scalar value, but found array with {key:value}:" + value;
        }

    }

    public static class ExpectedArrayError extends AwkRuntimeError {
        private String variable;
        private String contents;

        public ExpectedArrayError(String variable, String contents) {
            this.variable = variable;
            this.contents = contents;
        }

        @Override
        public String message() {
            return "Expeced " + variable + " to be array but was scalar with value: " + contents;
        }

    }

    public static class ExpectedNumberError extends AwkRuntimeError {
        private InterpreterDataType value;
        private NumberFormatException parseError;

        public ExpectedNumberError(InterpreterDataType value, NumberFormatException parseError) {
            this.value = value;
            this.parseError = parseError;
        }

        @Override
        public String message() {
            return "Expected " + value + " to be a number, but it couldn't be coerreced to a number: "
                    + parseError.getMessage();
        }

    }

    public static class NotAVariableError extends AwkRuntimeError {
        private Node attempted;

        public NotAVariableError(Node attempted) {
            this.attempted = attempted;
        }

        @Override
        public String message() {
            return "Expected " + attempted + " to be a variable so it could be assigned to but was not";
        }
    }
    public static class NegativeFieldIndexError extends AwkRuntimeError {
        private Node attempted;
        private int index;

        public NegativeFieldIndexError(Node attempted, int index) {
            this.attempted = attempted;
            this.index = index;
        }

        @Override
        public String message() {
            return "Cannot get field with negative index " + index + " in expression " + attempted;
        }
    }

    public static class ToManyArgsForVardiacError extends AwkRuntimeError {

        private String name;
        private int size;

        public ToManyArgsForVardiacError(String name, int size) {
            this.name = name;
            this.size = size;
        }

        @Override
        public String message() {
            return "Function " + name + "expected an optional arguement, not an extra " + size + " arguements";
        }
    }

    public static class ExpectedPatternError extends AwkRuntimeError {
        private Node value;

        public ExpectedPatternError(Node value) {
            this.value = value;
        }

        @Override
        public String message() {
            return "Expected " + value + " to be a pattern, but it was not";
        }
    }
}
