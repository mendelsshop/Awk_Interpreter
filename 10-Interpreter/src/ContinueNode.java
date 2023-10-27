public class ContinueNode extends StatementNode {

    @Override
    public String toString() {
        return "continue";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ContinueNode;
    }
}
