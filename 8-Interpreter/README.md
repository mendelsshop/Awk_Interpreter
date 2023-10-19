# Interpreter 2
## Project organization

```
root
├── get_awk_files.py
├── tests
│   ├── hi.awk
│   ├── foo.awk
│   └── other.awk
├── README.md
├── src
│   ├── AssignmentNode.java
│   ├── AwkException.java
│   ├── Awk.java
│   ├── AwkRuntimeError.java
│   ├── BlockNode.java
│   ├── BreakNode.java
│   ├── ConstantNode.java
│   ├── ContinueNode.java
│   ├── DeleteNode.java
│   ├── DisplayError.java
│   ├── DoWhileNode.java
│   ├── ForEachNode.java
│   ├── ForNode.java
│   ├── Functional
│   │   ├── CheckedBiConsumer.java
│   │   ├── CheckedBiFunction.java
│   │   ├── CheckedConsumer.java
│   │   ├── CheckedFunction.java
│   │   ├── CheckedPredicate.java
│   │   ├── CheckedRunnable.java
│   │   └── CheckedSupplier.java
│   ├── FunctionalLexer.java
│   ├── FunctionCallNode.java
│   ├── FunctionNode.java
│   ├── IfNode.java
│   ├── InterpreterArrayDataType.java
│   ├── InterpreterDataType.java
│   ├── Interpreter.java
│   ├── Lexer.java
│   ├── Node.java
│   ├── OperationNode.java
│   ├── Optional.java
│   ├── Parser.java
│   ├── PatternNode.java
│   ├── ProgramNode.java
│   ├── ReturnNode.java
│   ├── StatementNode.java
│   ├── StringHandler.java
│   ├── TernaryOperationNode.java
│   ├── TokenHandler.java
│   ├── Token.java
│   ├── UnitTests.java
│   ├── VariableReferenceNode.java
│   └── WhileNode.java
```

The awk files are generated from using `get_awk_files.py` (and will probably be different then the ones shown here) are required for the first unit test to work (see `root/src/UnitTests.java` for more info)

The reason there is no `import java.util.Optional` is because I am using a custom version of `Optional` in `root/src/Optional.java`.

Some unit tests require java 21 preview for `instanceof` pattern matching.