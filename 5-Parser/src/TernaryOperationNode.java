public class TernaryOperationNode extends Node {
    private Node cond; 
    private Node then; 
    private Node alt;
    public TernaryOperationNode(Node cond, Node then, Node alt) {
        this.cond = cond;
        this.then = then;
        this.alt = alt;
    }
    @Override
    public String toString() {
        return "(" + cond + " ? " + then + " : " + alt + ")";
    }
}
