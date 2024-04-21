public class InterpreterDataType {
    private String contents = "";

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setContents(Float contents) {
        this.contents = contents % 1 == 0 ? String.valueOf(contents.intValue()) : String.valueOf(contents);
    }

    public InterpreterDataType() {
    }

    public InterpreterDataType(String contents) {
        this.contents = contents;
    }
    
    public InterpreterDataType(float contents) {
        setContents(contents);
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

    // cloning an IDT means you cannot modify the current IDT through the cloned one
    public InterpreterDataType Clone() {
        return new InterpreterDataType(contents);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contents == null) ? 0 : contents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InterpreterDataType other = (InterpreterDataType) obj;
        if (contents == null) {
            if (other.contents != null)
                return false;
        } else if (!contents.equals(other.contents))
            return false;
        return true;
    }
}
