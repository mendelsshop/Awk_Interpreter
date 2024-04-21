# AWK interpreter

## Project organization

```
.
├── get_awk_files.py
├── lib
│   ├── hamcrest-core-1.3.jar
│   └── junit-4.13.2.jar
├── LICENSE
├── README.md
├── src
│   ├── AwkException.java
│   ├── Awk.java
│   ├── Functional
│   │   ├── CheckedBiConsumer.java
│   │   ├── CheckedBiFunction.java
│   │   ├── CheckedConsumer.java
│   │   ├── CheckedFunction.java
│   │   ├── CheckedPredicate.java
│   │   ├── CheckedRunnable.java
│   │   └── CheckedSupplier.java
│   ├── Interpreter
│   │   ├── AwkRuntimeError.java
│   │   ├── DisplayError.java
│   │   ├── InterpreterArrayDataType.java
│   │   ├── InterpreterDataType.java
│   │   ├── Interpreter.java
│   │   └── ReturnType.java
│   ├── Lexer
│   │   ├── FunctionalLexer.java
│   │   ├── Lexer.java
│   │   ├── StringHandler.java
│   │   └── Token.java
│   ├── Optional.java
│   ├── Parser
│   │   ├── AssignmentNode.java
│   │   ├── BlockNode.java
│   │   ├── BreakNode.java
│   │   ├── BuiltInFunctionDefinitionNode.java
│   │   ├── ConstantNode.java
│   │   ├── ContinueNode.java
│   │   ├── DeleteNode.java
│   │   ├── DoWhileNode.java
│   │   ├── ForEachNode.java
│   │   ├── ForNode.java
│   │   ├── FunctionCallNode.java
│   │   ├── FunctionNode.java
│   │   ├── IfNode.java
│   │   ├── Node.java
│   │   ├── OperationNode.java
│   │   ├── Parser.java
│   │   ├── PatternNode.java
│   │   ├── ProgramNode.java
│   │   ├── ReturnNode.java
│   │   ├── StatementNode.java
│   │   ├── TernaryOperationNode.java
│   │   ├── TokenHandler.java
│   │   ├── VariableReferenceNode.java
│   │   └── WhileNode.java
│   └── UnitTests
│       ├── InterpreterTests.java
│       ├── LexerTests.java
│       ├── LineManagerTests.java
│       ├── ParserTests.java
│       ├── StringHandlerTests.java
│       ├── TokenHandlerTests.java
│       └── UnitTests.java
├── tests
│   ├── 10-Interpreter.jar
│   ├── backtick
│   │   ├── csv.awk
│   │   ├── fen.awk
│   │   ├── find_special_class.awk
│   │   ├── imager.awk
│   │   ├── mangler.awk
│   │   ├── math.awk
│   │   ├── misc1.awk
│   │   └── scheme.awk
│   ├── extra
│   │   ├── data.awk
│   │   ├── ex.awk
│   │   └── work.awk
│   ├── files.txt
│   ├── normal
│   │   ├── csv.awk
│   │   ├── fen.awk
│   │   ├── find_special_class.awk
│   │   ├── imager.awk
│   │   ├── mangler.awk
│   │   ├── math.awk
│   │   ├── misc1.awk
│   │   └── scheme.awk
│   └── text
│       ├── art
│       ├── classes
│       ├── example.txt
│       ├── fen.txt
│       ├── foo
│       ├── hello.rkt
│       ├── lorum_ipsum
│       └── sample.csv
└── test.sh
```

The awk files are generated from using `get_awk_files.py` (and will probably be different then the ones shown here) are required for the first unit test to work (see `root/src/UnitTests.java` for more info)

The reason there is no `import java.util.Optional` is because I am using a custom version of `Optional` in `root/src/Optional.java`.

Some unit tests require java 21 preview for `instanceof` pattern matching.

Too run the integration tests, run `test.sh` from the root directory with the path java 21 excuatable.
