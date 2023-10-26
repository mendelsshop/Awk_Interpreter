import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterpreterArrayDataType extends InterpreterDataType {
    private HashMap<String, InterpreterDataType> contents = new HashMap<String, InterpreterDataType>();

    public Stream<InterpreterDataType> getItemsStream() {
        return contents.values().stream();
    }

    public List<InterpreterDataType> getItemsList() {
        return new LinkedList<>(contents.values());
    }

    public InterpreterDataType get(String index) {
        return (contents.computeIfAbsent(index, u -> new InterpreterDataType()));
    }

    // this method is used for varidaic parameter handling, in awk if a index is not
    // present we create it (purpose of get), but for varidiac parameters we want to
    // know if there is no map for the key to use the default value
    // TODO: maybe since its only for varidac 1) assert its only 1 param
    // 2) dont take index assume its 0
    public Optional<InterpreterDataType> getOptional(String index) {
        // javas hashmap returns null if cannot find value for a given key
        return Optional.ofNullable(contents.get(index));
    }

    public void insert(String name, InterpreterDataType value) {
        contents.put(name, value);
    }

    public InterpreterArrayDataType() {
    }

    // make sure that if we try to get it like its a scalar that we throw exception
    @Override
    public String getContents() {
        throw new AwkRuntimeError.ExpectedScalarError(this);
    }

    @Override
    // just for errors
    public String toString() {
        return "{" + contents.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(",")) + "}";
    }

    // Cloning array does not do anything
    @Override
    public InterpreterDataType Clone() {
        return this;
    }
}
