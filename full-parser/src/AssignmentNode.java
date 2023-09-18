public class AssignmentNode extends Node {
    private Node target; 
    private Node expression;
    public AssignmentNode(Node target, Node expression) {
        this.target = target;
        this.expression = expression;
    }
    @Override
    public String toString() {
        return target + " = " + expression;
    }
}
