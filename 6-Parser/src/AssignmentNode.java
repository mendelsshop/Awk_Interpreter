public class AssignmentNode extends StatementNode {
    private Node target; 
    private Node expression;
    public Node getTarget() {
        return target;
    }
    public Node getExpression() {
        return expression;
    }
    public AssignmentNode(Node target, Node expression) {
        this.target = target;
        this.expression = expression;
    }
    @Override
    public String toString() {
        return "("+ target + " = " + expression + ")";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
        AssignmentNode other = (AssignmentNode) obj;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }
}
