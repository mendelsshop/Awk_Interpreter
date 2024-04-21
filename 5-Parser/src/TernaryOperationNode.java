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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cond == null) ? 0 : cond.hashCode());
        result = prime * result + ((then == null) ? 0 : then.hashCode());
        result = prime * result + ((alt == null) ? 0 : alt.hashCode());
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
        TernaryOperationNode other = (TernaryOperationNode) obj;
        if (cond == null) {
            if (other.cond != null)
                return false;
        } else if (!cond.equals(other.cond))
            return false;
        if (then == null) {
            if (other.then != null)
                return false;
        } else if (!then.equals(other.then))
            return false;
        if (alt == null) {
            if (other.alt != null)
                return false;
        } else if (!alt.equals(other.alt))
            return false;
        return true;
    }
}
