
public class DeleteNode extends StatementNode {
    private Node array;

    public Node getArray() {
        return array;
    }

    public DeleteNode(Node array) {
        this.array = array;
    }

    @Override
    public String toString() {
        return "delete " + array;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((array == null) ? 0 : array.hashCode());
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
        if (array == null) {
            if (other.array != null)
                return false;
        } else if (!array.equals(other.array))
            return false;
        return true;
    }
}
