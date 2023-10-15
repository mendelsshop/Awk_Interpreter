import java.util.LinkedList;
import java.util.stream.Collectors;

public class DeleteNode extends StatementNode {
    private String name; // could this be any lvalue

    public DeleteNode(String name) {
        this.name = name;
    }

    private Optional<LinkedList<String>> indexs = Optional.empty();

    public DeleteNode(String name, LinkedList<String> indexs) {
        this.name = name;
        this.indexs = Optional.of(indexs);
    }

    public String getName() {
        return name;
    }

    public Optional<LinkedList<String>> getIndex() {
        return indexs;
    }

    @Override
    public String toString() {
        return "delete " + name + indexs
                .map(i -> "[" + i.stream().map(c -> c.toString()).collect(Collectors.joining(",")) + "]").orElse("");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((indexs == null) ? 0 : indexs.hashCode());
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
        DeleteNode other = (DeleteNode) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (indexs == null) {
            if (other.indexs != null)
                return false;
        } else if (!indexs.equals(other.indexs))
            return false;
        return true;
    }

}
