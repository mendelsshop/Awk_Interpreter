// Decided to make each error indivdual class makes it easier to test what type of errors are being thrown

public abstract class AwkRuntimeError extends RuntimeException implements DisplayError {
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
}
