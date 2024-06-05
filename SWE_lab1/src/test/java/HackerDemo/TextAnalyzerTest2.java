package HackerDemo;
import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.Assert.*;


public class TextAnalyzerTest2 {

    @Test
    public void testNoOutgoingEdges() throws IOException {


        // 设置文件输出捕获
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));


        TextAnalyzer analyzer = new TextAnalyzer("Approachable.txt");


        // 指定输出文件名，避免创建文件
        analyzer.startTraversal("output.txt");

        // 验证输出是否包含特定文本
        String output = outContent.toString();
        System.out.print(output);
        assertFalse(output.contains("No more edges to traverse from node:"));
        assertTrue(output.contains("Encountered a previously visited edge:"));
        assertFalse(output.contains("Graph is empty."));
        assertTrue(output.contains("Traversing to node: alwayswalkhere"));

        // 清理输出流
        System.setOut(System.out);
    }
}
