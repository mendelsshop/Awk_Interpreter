public class ConstantNode extends Node {
    private String value;
    private ValueType type;

    public ConstantNode(String value, ConstantNode.ValueType type) {
        this.value = value;
        this.type = type;
    }

    public enum ValueType {
        Number,
        String
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}
