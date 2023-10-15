public class BreakNode extends StatementNode {

    @Override
    public String toString() {
        return "break";
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BreakNode;
    }
}
