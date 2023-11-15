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
            return "Expected " + (varidiac ? "At least " + (expected - 1): expected ) + " arguement(s) but found " + found + " arguement(s) in call to " + functionName;
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

    public static class ExpectedIterableError extends AwkRuntimeError {
        private String variable;

        public ExpectedIterableError(String variable) {
            this.variable = variable;

        }

        @Override
        public String message() {
            return "Expeced " + variable + " to be array for iteration with for";
        }

    }

    public static class ExpectedDeleteArrayError extends AwkRuntimeError {
        private String variable;

        public ExpectedDeleteArrayError(String variable) {
            this.variable = variable;

        }

        @Override
        public String message() {
            return "Expeced " + variable + " to be array for delete";
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

    public static class ReturnInOuterBlockError extends AwkRuntimeError {
        private ReturnType returnValue;

        public ReturnInOuterBlockError(ReturnType returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public String message() {
            return "Cannot change control flow in outer block with " + returnValue;

        }
    }

    public static class FunctionNotFoundError extends AwkRuntimeError {
        private String functionName;

        public FunctionNotFoundError(String function) {
            functionName = function;
        }

        @Override
        public String message() {
            return "Functon " + functionName + " is not defined";

        }
    }

    public static class NextInBeginError extends AwkRuntimeError {
        @Override
        public String message() {
            return "Next statement ran in begin block";
        }
    }

    public static class NextInEndError extends AwkRuntimeError {
        @Override
        public String message() {
            return "Next statement ran in end block";
        }
    }

}
