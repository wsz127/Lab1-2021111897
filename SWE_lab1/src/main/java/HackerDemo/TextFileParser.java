package HackerDemo;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileParser {
    public static void main(String[] args) {
        // 输入文本文件路径
        String filePath = "C:/Users/tar/Desktop/Titanic/aaa.txt";

        // 读取文本文件
        String text = readTextFile(filePath);

        // 处理文本并输出结果
        String processedText = processText(text);
        System.out.println(processedText);

        try {
            FileWriter fileWriter = new FileWriter("C:/Users/tar/Desktop/Titanic/bbb.txt"); // 创建文件写入对象
            fileWriter.write(processedText); // 将字符串写入文件
            fileWriter.close(); // 关闭文件写入流
            System.out.println("成功写入文件。");
        } catch (IOException e) {
            System.out.println("写入文件时出现错误：" + e.getMessage());
        }


    }

    // 读取文本文件
    private static String readTextFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
                sb.append(" "); // 将换行符转换为空格
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // 处理文本并返回结果
    private static String processText(String text) {
        StringBuilder sb = new StringBuilder();
        boolean lastCharWasLetter = false;
        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                sb.append(Character.toLowerCase(ch)); // 转换为小写字母
                lastCharWasLetter = true;
            } else if (lastCharWasLetter) {
                sb.append(" "); // 将非字母字符替换为一个空格
                lastCharWasLetter = false;
            }
        }
        return sb.toString().trim(); // 去除首尾空格
    }
}
