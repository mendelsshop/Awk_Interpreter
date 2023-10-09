public class VariableReferenceNode extends StatementNode {
    private String name;
    private Optional<Node> index = Optional.empty();

    public VariableReferenceNode(String name) {
        this.name = name;

    }

    public VariableReferenceNode(String name, Node index) {
        this.name = name;
        this.index = Optional.of(index);
    }

    public Optional<Node> getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return index.isPresent() ? name + "[" + index.get() + "]" : name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((index == null) ? 0 : index.hashCode());
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
        VariableReferenceNode other = (VariableReferenceNode) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (index == null) {
            if (other.index != null)
                return false;
        } else if (!index.equals(other.index))
            return false;
        return true;
    }
}
