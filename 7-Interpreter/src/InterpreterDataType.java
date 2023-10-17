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
    public String toString() {
        return contents;
    }
}
