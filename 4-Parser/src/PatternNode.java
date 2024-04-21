public class PatternNode extends Node {
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

}
