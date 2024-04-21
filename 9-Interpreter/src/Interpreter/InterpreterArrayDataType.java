import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterpreterArrayDataType extends InterpreterDataType {
    private HashMap<String, InterpreterDataType> contents = new HashMap<String, InterpreterDataType>();

    public HashMap<String, InterpreterDataType> getHashMap() {
        return contents;
    }

    public InterpreterArrayDataType(HashMap<String, InterpreterDataType> contents) {
        this.contents = contents;
    }

    public Stream<InterpreterDataType> getItemsStream() {
        return contents.values().stream();
    }

    public List<InterpreterDataType> getItemsList() {
        return new LinkedList<>(contents.values());
    }

    public List<String> getKeysList() {
        return new LinkedList<>(contents.keySet());
    }

    public InterpreterDataType get(String index) {
        return (contents.computeIfAbsent(index, u -> new InterpreterDataType()));
    }

    // this method is used for varidaic parameter handling, in awk if a index is not
    // present we create it (purpose of get), but for varidiac parameters we want to
    // know if there is no map for the key to use the default value
    /// since its only for varidac
    // 1) assert its only 1 param
    // 2) dont take index assume its 0
    public Optional<InterpreterDataType> getOptional(String name) {
        if (contents.size() > 1) {
            throw new AwkRuntimeError.ToManyArgsForVardiacError(name, contents.size());
        }
        // javas hashmap returns null if cannot find value for a given key
        return Optional.ofNullable(contents.get("0"));
    }

    public boolean contains(String index) {
        return contents.containsKey(index);
    }

    // make sure that if we try to get it like its a scalar that we throw exception
    @Override
    public String getContents() {
        throw new AwkRuntimeError.ExpectedScalarError(this);
    }

    // make sure that if we try to set it like its a scalar that we throw exception
    @Override
    public void setContents(String contents) {
        throw new AwkRuntimeError.ExpectedScalarError(this);
    }

    public void insert(String name, InterpreterDataType value) {
        contents.put(name, value);
    }

    public InterpreterArrayDataType() {
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

    public void clear() {
        contents.clear();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((contents == null) ? 0 : contents.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        InterpreterArrayDataType other = (InterpreterArrayDataType) obj;
        if (contents == null) {
            if (other.contents != null)
                return false;
        } else if (!contents.equals(other.contents))
            return false;
        return true;
    }
}
