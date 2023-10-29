
import static org.junit.Assert.*;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

public class UnitTests {
        public static LinkedList<Token> testLexContent(String content, Token.TokenType[] expected) throws Exception {
                var lexer = new Lexer(content);
                var otherLexer = new FunctionalLexer(content);
                var lexed = lexer.lex();
                var otherLexed = otherLexer.lex();
                var lexedTokens = lexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
                var otherLexedTokens = otherLexed.stream().<Token.TokenType>map(c -> c.getType()).toArray();
                assertArrayEquals("lexer", expected, lexedTokens);
                assertArrayEquals("functional lexer", expected, otherLexedTokens);
                return lexed;

        }

        // use get_awk_files.py, make sure to have root/tests directory
        // amd that the junit tests run in the root directory
        // in vscode use "java.test.config": {
        // "workingDirectory": "${workspaceFolder}"
        // }, in settings.json
        // to pull random awk code from the internet
        //
        // NOTE: about this test:
        // a) it does not mean the lexer works b/c
        // 1) if a test fails, the source file could be invalid we have no way of
        // verfiying a file
        // 2) if it doesnt fail who says lexer is correct as we have no way of verfiying
        // a file
        // b) this simple file = file.replaceAll("/", "`"); messes with with division,
        // even though it fixes regex pattern
        public Stream<String> get_awk_files() throws Exception {
                return Files.list(Paths.get("tests")).filter(file -> {
                        try {
                                new String(Files.readAllBytes(file));
                                return true;
                        } catch (IOException e) {
                                return false;
                        }
                }).map(file -> {
                        try {
                                return new String(Files.readAllBytes(file));
                        } catch (IOException e) {
                                return "";
                        }
                });
        }

        public void assertWorks(String file) {
                file = file.replaceAll("/", "`");
                var lexer = new Lexer(file);
                var fpLexer = new FunctionalLexer(file);
                try {
                        lexer.lex();
                        fpLexer.lex();
                } catch (AwkException e) {
                        System.out.println("error lexing file\n" + file + "\n" + e.message);
                        throw new RuntimeException(e);
                }
        }

        // @Test
        public void TestRandomAwkFile() throws Exception {
                get_awk_files().forEach(this::assertWorks);
        }

        protected static Random rng = new Random();
        protected static boolean debug = false;

        public static ProgramNode parse(String input, Token.TokenType[] tokens, int beginBlocks, int endBlocks,
                        int functions,
                        int blocks) throws Exception {
                var parser = new Parser(UnitTests.testLexContent(input, tokens));
                ProgramNode parse = parser.Parse();
                assertEquals(parse.getEndBlocks().size(), endBlocks);
                assertEquals(parse.getBeginBlocks().size(), beginBlocks);
                assertEquals(parse.getFunctions().size(), functions);
                assertEquals(parse.getRestBlocks().size(), blocks);
                return parse;
        }

}
