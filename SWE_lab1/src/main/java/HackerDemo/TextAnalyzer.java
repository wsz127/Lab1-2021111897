package HackerDemo;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.ui.view.Viewer;
import java.io.*;
import java.util.*;
import java.util.Random;

class GraphMat{
    private byte adjacencyMatrix[][]; // 设置邻接矩阵，因为邻边设置长度为1，为1有边
    public String[] node_name;
    public int num_node;
    public GraphMat(Graph graph)
    {
        this.num_node = 0;
        this.num_node = graph.getNodeCount();
        int n = this.num_node;
        this.adjacencyMatrix = new byte[n][n];
        this.node_name = new String[n + 1];
        for (int i = 0; i < n; i++) {
            this.node_name[i] = graph.getNode(i).getId();
            for (int j = 0; j < n; j++)
                this.adjacencyMatrix[i][j] = (byte) (graph.getNode(i).hasEdgeBetween(j) ? 1 : 0);
        }
    }

}


public class TextAnalyzer {
    private Graph graph;


    public TextAnalyzer(String filename) throws IOException {
        this.graph = new MultiGraph("TextGraph");
        this.graph.setStrict(false);
        this.graph.setAutoCreate(true);
        processFile(filename);
    }


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

    // 处理文本,转换成小写且去掉特殊字符，返回字符串
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

    private void processFile(String filename) throws IOException {

        String text = readTextFile(filename);
        String processedText = processText(text);

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String previousWord = null;


        StringTokenizer tokenizer = new StringTokenizer(processedText);
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            if (previousWord != null) {
                Edge edge = graph.getEdge(previousWord + "_" + word);

                if (edge == null) {
                    graph.addEdge(previousWord + "_" + word, previousWord, word, true);
                    Edge edge1 = graph.getEdge(previousWord + "_" + word);
                    edge1.setAttribute("weight", 1);
                } else {
                    edge.setAttribute("weight", (int)edge.getAttribute("weight") + 1);
                }
            }
            previousWord = word;
        }

        reader.close();
    }



    public void showDirectedGraph() {

        graph.setAttribute("ui.stylesheet", "node{ size: 10px, 10px; shape: circle; fill-color: #B1DFF7; stroke-mode: plain; stroke-color: #B1DFF7; stroke-width: 3; text-mode: normal; /*normal or hidden*/ text-background-mode: plain; /*plain or none*/ text-background-color: rgba(255, 255, 255, 200); text-alignment: above;}");

        Viewer viewer = graph.display();

        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
        }
        for (Edge edge : graph.getEachEdge()) {
            if (edge.getAttribute("weight") != null) {
                edge.addAttribute("ui.label", edge.getAttribute("weight").toString());
            }
        }
    }

    // init origin graph
    public void init_graph()
    {
        for (Node node : graph) {
            node.setAttribute("ui.style", "fill-color: blue;");
        }
        for (Edge edge : graph.getEachEdge())
        {
            edge.setAttribute("ui.style", "fill-color: black;");
        }
    }

    // 判断桥接词
    public List<String> queryBridgeWords(String word1, String word2) {
        if (graph.getNode(word1) == null||graph.getNode(word2) == null) {
            List<String> list = new ArrayList<>();
            list.add(" ");//TODO： 这里输入输出有问题
            return list;//若没有这两个词，返回的不是null，而是一个有" "的列表
        }

        List<String> bridges = new ArrayList<>();
        for (Edge edge1 : graph.getNode(word1).getLeavingEdgeSet()) {
            String midWord = edge1.getTargetNode().getId();
            for (Edge edge2 : graph.getNode(midWord).getLeavingEdgeSet()) {
                if (edge2.getTargetNode().getId().equals(word2)) {
                    bridges.add(midWord);
                }
            }
        }


        return bridges;
    }

    // 生成含有桥接词的序列
    public List<String> generateNewText(String sentence) {
        String[] words = sentence.split("\\s+");

        List<String> result = new ArrayList<>();

        if(words.length<=1)
        {
            if(words.length==1)
            {
                result.add(words[0]);
            }
            return result;
        }

        List<String> list = new ArrayList<>();
        list.add(" ");

        result.add(words[0]);

        for(int i = 0; i< words.length-1; i++){
            List<String> bridges =queryBridgeWords(words[i].toLowerCase(), words[i+1].toLowerCase());

            if ( bridges!=null && !bridges.equals(list)&& bridges.size()!=0){
            Random random = new Random();

            int randomIndex = random.nextInt(bridges.size());



            result.add(bridges.get(randomIndex));
            }
            result.add(words[i+1]);
        }
        return result;
    }


    //todo: 重写最短路径算法
    public List<String> shortestPath(String start, String end) {
    if (graph.getNode(start) == null|| graph.getNode(end) == null) {
        System.out.println("One or both nodes not in the graph!");
        return Collections.emptyList();
    }

    Map<String, String> pred = new HashMap<>();
    Queue<String> queue = new LinkedList<>();
    Set<String> visited = new HashSet<>();

    queue.add(start);
    visited.add(start);
    pred.put(start, null);

    // BFS to find shortest path
    while (!queue.isEmpty()) {
        String current = queue.poll();
        if (current.equals(end)) {
            break;
        }
        for (Edge edge : graph.getNode(current).getLeavingEdgeSet()) {
            String neighbor = edge.getTargetNode().getId();
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                pred.put(neighbor, current);
                queue.add(neighbor);
            }
        }
    }


    // Reconstruct path
    LinkedList<String> path = new LinkedList<>();
    for (String at = end; at != null; at = pred.get(at)) {
        path.addFirst(at);
    }
    if (path.getFirst().equals(start)) {
        return path;
    } else {
        return Collections.emptyList();
    }
}

    // 可以输出红色最短路径，并打印所有节点最短路径长度
    public void Dijstra(String start, String end)
    {
        if (graph.getNode(start) == null|| graph.getNode(end) == null) {
            System.out.println("One or both nodes not in the graph!");
            return;
        }

        // Edge lengths are stored in an attribute called "length"
        // The length of a path is the sum of the lengths of its edges
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");

        // Compute the shortest paths in g from A to all nodes
        dijkstra.init(this.graph);
        dijkstra.setSource(this.graph.getNode(start));
        dijkstra.compute();

        // Print the lengths of all the shortest paths
        // 可选功能：
        for (Node node : this.graph)
        {
            System.out.printf("%s->%s:%10.2f%n", dijkstra.getSource(), node,
                    dijkstra.getPathLength(node));
        }

        System.out.printf("the destination node is:%s\n", this.graph.getNode(end).getId());
        // Color in blue all the nodes on the shortest path form A to B
        for (Node node : dijkstra.getPathNodes(this.graph.getNode(end)))
        {
            node.setAttribute("ui.style", "fill-color: yellow;");
            System.out.printf(node.getId());
            if(node != this.graph.getNode(end))
                System.out.printf("->");
        }
        for (Edge edge: dijkstra.getPathEdges(graph.getNode(end)))
        {
            edge.setAttribute("ui.style", "fill-color: red;");
        }


    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in); // 创建Scanner对象
        System.out.println("请输入文本文件的地址：");
        String input = scanner.nextLine(); // 读取用户输入的一行文本
        TextAnalyzer tg = new TextAnalyzer(input);
        tg.showDirectedGraph();

        System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
        while (true) {
            while (scanner.hasNextLine()) {
                String fun = scanner.nextLine();

                if (fun.equals("3")) {
                    tg.init_graph();
                    System.out.println("Enter two words separated by space to find bridge words:");
                    String[] words = scanner.nextLine().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");

                        continue;
                    }

                    List<String> bridges = tg.queryBridgeWords(words[0], words[1]);

                    List<String> list = new ArrayList<>();
                    list.add(" ");
                    if (bridges!=null&&bridges.equals(list)) {
                        System.out.println("No \"" + words[0] + "\" or \"" + words[1] + "\" in the graph!");
                    }
                    else {
                        if (bridges.size() == 0) {
                            System.out.println("No bridge words from \"" + words[0] + "\" to \"" + words[1] + "\"!");
                        } else {
                            System.out.println("The bridge words from \"" + words[0] + "\" to \"" + words[1] + "\" are: " + String.join(", ", bridges) + ".");

                        }
                    }
                    System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
                }
                if (fun.equals("4")) {
                    tg.init_graph();
                    System.out.println("输入一段话");
                    String sentence = scanner.nextLine();
                    List<String> result = tg.generateNewText(sentence);
                    StringBuilder output = new StringBuilder();
                    for (int i = 0; i < result.size(); i++) {
                        // 将当前字符串追加到输出中
                        output.append(result.get(i));

                        // 检查是否是列表中的最后一个字符串
                        if (i < result.size() - 1) {
                        // 如果不是最后一个字符串，添加一个空格
                        output.append(" ");

                    }
                    }
                    // 输出最终结果
                    System.out.println(output.toString());
                    System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
                }
                if (fun.equals("5")) {
                    tg.init_graph();
                    System.out.println("输入两个词");
                    String[] words = scanner.nextLine().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
                        continue;
                    }
                    List<String> path = tg.shortestPath(words[0].toLowerCase(), words[1].toLowerCase());
                    tg.Dijstra(words[0].toLowerCase(), words[1].toLowerCase());

                    if ( path.isEmpty()){
                        System.out.println("不可达");
                        System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
                        continue;

                    }
//                    StringBuilder output = new StringBuilder();
//                    for (int i = 0; i < path.size(); i++) {
//                        // 将当前字符串追加到输出中
//                        output.append(path.get(i));
//
//                        // 检查是否是列表中的最后一个字符串
//                        if (i < path.size() - 1) {
//                        // 如果不是最后一个字符串，添加一个空格
//                        output.append("->");
//
//                    }
//                    }
                    // 输出最终结果
//                    System.out.println(output.toString());
                    System.out.println("选择你要实现的功能 3查询桥接词 4根据bridge word生成新文本 5计算两个单词之间的最短路径 6随机游走");
                }
            }
        }
    }
}
