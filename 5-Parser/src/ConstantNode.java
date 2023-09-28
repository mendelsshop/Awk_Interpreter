public class ConstantNode extends Node {
    private String value;

    // do we need a to preserve the type
    public ConstantNode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
