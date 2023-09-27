public class PatternNode extends StatementNode {
    private String pattern;

    public PatternNode(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "pattern(" + pattern + ")";
    }

}
