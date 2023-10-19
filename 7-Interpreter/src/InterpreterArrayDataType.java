import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class InterpreterArrayDataType extends InterpreterDataType {
    private HashMap<String, InterpreterDataType> contents = new HashMap<String, InterpreterDataType>();

    public Stream<InterpreterDataType> getItemsStream() {
        return contents.values().stream();
    }

    public List<InterpreterDataType> getItemsList() {
        return new LinkedList<>(contents.values());
    }

    // TODO through exception if contents is null
    public InterpreterDataType get(String index) {
        return contents.get(index);
    } 

    public void insert(String name, InterpreterDataType value) {
        contents.put(name, value);
    } 



    public InterpreterArrayDataType() {
    }

    // TODO: printing arrays is not valid
}
