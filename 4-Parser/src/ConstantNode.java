public class ConstantNode extends Node {
    private String value;


    public ConstantNode(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }
}
