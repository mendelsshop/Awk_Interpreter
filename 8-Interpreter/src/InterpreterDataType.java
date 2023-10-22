public class InterpreterDataType {
    private String contents = "";

    public void setContents(String contents) {
        this.contents = contents;
    }

    public InterpreterDataType() {
    }

    public InterpreterDataType(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    // this is only used for error handling purposes within the interpreter we
    // generally use getContents to make sure that in the case its an array it
    // throws an exception
    public String toString() {
        return contents;
    }
}
