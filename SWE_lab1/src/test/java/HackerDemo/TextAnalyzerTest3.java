package HackerDemo;
import org.junit.jupiter.api.Test;
import java.io.*;
import static org.junit.Assert.*;


public class TextAnalyzerTest3 {

    @Test
    public void testNoOutgoingEdges() throws IOException {


        // 设置文件输出捕获
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));


        TextAnalyzer analyzer = new TextAnalyzer("EmptyGraph.txt");


        // 指定输出文件名，避免创建文件
        analyzer.startTraversal("output.txt");

        // 验证输出是否包含特定文本
        String output = outContent.toString();
        System.out.print(output);
        assertFalse(output.contains("No more edges to traverse from node:"));
        assertFalse(output.contains("Encountered a previously visited edge:"));
        assertTrue(output.contains("Graph is empty."));

        // 清理输出流
        System.setOut(System.out);
    }
}
