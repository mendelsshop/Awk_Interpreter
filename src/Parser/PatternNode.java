public class PatternNode extends StatementNode {
    private String pattern;

    public String getPattern() {
        return pattern;
    }

    public PatternNode(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "pattern(" + pattern + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
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
        PatternNode other = (PatternNode) obj;
        if (pattern == null) {
            if (other.pattern != null)
                return false;
        } else if (!pattern.equals(other.pattern))
            return false;
        return true;
    }
}
