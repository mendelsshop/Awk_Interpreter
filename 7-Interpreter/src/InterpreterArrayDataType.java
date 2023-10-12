import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType {
    private HashMap<String,InterpreterDataType> contents = new HashMap<String,InterpreterDataType>();

    public InterpreterArrayDataType() {
    }
}
