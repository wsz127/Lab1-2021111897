package HackerDemo;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.ui.view.Viewer;
import scala.util.parsing.combinator.testing.Str;

import java.io.*;
import java.util.*;
import java.util.Random;

// 没什么用的类
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
        System.out.println("Processing file...");
        String processedText = processText(text);
        System.out.println(processedText);

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

        graph.setAttribute("ui.stylesheet", "node{ size: 10px, 10px; shape: circle; fill-color: #B1DFF7; stroke-mode: plain; stroke-color: #B1DFF7; stroke-width: 5; text-mode: normal; text-size: 20px; /*normal or hidden*/ text-background-mode: plain; /*plain or none*/ text-background-color: rgba(255, 255, 255, 200); text-alignment: above;}"+
                "edge { " +
                "   text-size: 20px; " +  // 设置边标签字体大小
                "}");

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


    public Map<String, Integer> calculateDistancesFrom(String startNodeId) {
        Map<String, Integer> distances = new HashMap<>();
        Node startNode = graph.getNode(startNodeId);

        if (startNode == null) {
            throw new IllegalArgumentException("Node " + startNodeId + " does not exist in the graph.");
        }

        for (Node node : graph) {
            String nodeId = node.getId();
            if (nodeId.equals(startNodeId)) {
                distances.put(nodeId, 0); // Distance to itself is defined as infinity
            } else {
                Edge edge = graph.getEdge(startNodeId + "_" + nodeId);
                if (edge != null) {
                    int weight = edge.getAttribute("weight");
                    distances.put(nodeId, weight);
                } else {
                    distances.put(nodeId, Integer.MAX_VALUE); // No direct edge, distance is infinity
                }
            }
        }

        return distances;
    }

    // 可以输出红色最短路径，并打印所有节点最短路径长度
    // 这是掉了包的实现
    //TODO：重写这个Dij算法
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

    private static class NodeDistance {
        String nodeId;
        int distance;

        NodeDistance(String nodeId, int distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    private List<String> getPath(Map<String, String> previous, String start, String end) {
        LinkedList<String> path = new LinkedList<>();
        for (String at = end; at != null; at = previous.get(at)) {
            path.addFirst(at);
        }
        path.addFirst(start);
        if (!path.isEmpty() && path.getFirst().equals(start)) {
            return path;
        } else {
            return Collections.emptyList();
        }
    }

    private void highlightPath(List<String> path) {
        int j = path.size() - 1; // last index
        for (String nodeId : path) {
            Node node = graph.getNode(nodeId);
            System.out.print(nodeId);
            if(!node.getId().equals(path.get(j)))
            {
                System.out.print(" -> ");
            }
            else {
                System.out.println();
            }
            node.setAttribute("ui.style", "fill-color: yellow;");
        }
        for (int i = 0; i < path.size() - 1; i++) {
            String edgeId = path.get(i) + "_" + path.get(i + 1);
            Edge edge = graph.getEdge(edgeId);
            if (edge != null) {
                edge.setAttribute("ui.style", "fill-color: red;");
            }
        }
    }


    public List<String> My_Dij(String start, String end) {
        if (graph.getNode(start) == null || graph.getNode(end) == null) {
            System.out.println("One or both nodes not in the graph!");
            return null;
        }

        //记录dist数组
        Map<String, Integer> dist = calculateDistancesFrom(start);
        //优先队列维护最短边
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));
        //是否访问过
        Set<String> visited = new HashSet<>();
        Map<String, String> previous = new HashMap<>();

        //从自己dist = 0开始
        pq.add(new NodeDistance(start, 0));
        for(String name: dist.keySet())
        {
            pq.add(new NodeDistance(name, dist.get(name)));
        }

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            String currentNodeId = current.nodeId;

            //如果访问过则continue
            if (!visited.add(currentNodeId)) {
                continue;
            }

            //用当前边更新所有边权
            for (Edge edge : graph.getNode(currentNodeId).getLeavingEdgeSet()) {
                String neighbor = edge.getTargetNode().getId();
                int weight = edge.getAttribute("weight");
                int newDist = dist.get(currentNodeId) + weight;

                if (newDist < dist.get(neighbor)) {
                    dist.put(neighbor, newDist);
                    previous.put(neighbor, currentNodeId);
                    pq.add(new NodeDistance(neighbor, newDist));
                }
            }
        }

        //打印start-->end的路径节点
        // Print the shortest path distance to the end node
        List<String> path = new ArrayList<>();
        if (dist.get(end) == Integer.MAX_VALUE) {
            System.out.println("There is no path from " + start + " -> " + end);
        } else {
            System.out.println("Shortest path from " + start + " -> " + end + " is " + dist.get(end));
            path = getPath(previous, start, end);

            highlightPath(path);
        }

        //打印到所有节点的最短路径长
        for (Map.Entry<String, Integer> entry : dist.entrySet()) {
            System.out.println("Distance from " + start + " -> " + entry.getKey() + " is " + entry.getValue());
        }
        return path;
    }

    //function 6: 图上随机游走，直到判环
    public String MyRandomWalk(int steps) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press ENTER to stop the random walk...");

        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        for (Node node : graph) {
            nodes.add(node);
        }

        if (nodes.isEmpty()) {
            return "The graph is empty!";
        }

        // 随机Start from a node
        Node currentNode = nodes.get(random.nextInt(nodes.size()));
        System.out.println("The starting point is " + "\"" + currentNode.getId() + "\"");
        StringBuilder result = new StringBuilder();
        Set<Node> visited = new HashSet<>();
        visited.add(currentNode);

        for (int i = 0; i < steps; i++) {
            result.append(currentNode.getId()).append("-> ");
            try {
                if (System.in.available() > 0) {
                    int input = System.in.read();
                    if (input == '\n') {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //收集所有的邻居，一遍进行randomwalk
            List<Node> neighbors = new ArrayList<>();
            for (Edge edge : currentNode.getEachEdge()) {
                Node neighbor = edge.getOpposite(currentNode);
                System.out.println(neighbor.getId());
                neighbors.add(neighbor);
            }

            //无处边情况
            if (neighbors.isEmpty()) {
                break;
            }
            //移动到下一个随机节点
            currentNode = neighbors.get(random.nextInt(neighbors.size()));
            //如果遇到了之前访问过的额节点，退出
            if (visited.contains(currentNode)) {
                // result.append(currentNode.getId()).append(" ");
                System.out.println("");
                break;
            }
            visited.add(currentNode);
        }
        return result.toString().trim();
    }

    public void startTraversal(String filename) throws IOException {
        Set<String> visitedEdges = new HashSet<>();
        BufferedWriter writer;
        Iterator<String> iterator = visitedEdges.iterator();
        while(iterator.hasNext()){

            iterator.remove();

        }
        writer = new BufferedWriter(new FileWriter(filename));
        Node currentNode = getRandomNode();
        if (currentNode == null) {
            System.out.println("Graph is empty.");
            return;
        }

        try {
            System.out.println("Starting traversal from node: " + currentNode.getId());
            writer.write("Starting traversal from node: " + currentNode.getId() + "\n");

            while (true) {
                Iterator<Edge> edgeIterator = currentNode.getLeavingEdgeIterator();
                if (!edgeIterator.hasNext()) {
                    System.out.println("No more edges to traverse from node: " + currentNode.getId());
                    break;
                }

                Edge edge = getRandomEdge(edgeIterator);
                if (!visitedEdges.add(edge.getId())) {
                    System.out.println("Encountered a previously visited edge: " + edge.getId());
                    break;
                }

                currentNode = edge.getTargetNode();
                System.out.println("Traversing to node: " + currentNode.getId());
                writer.write("Traversing to node: " + currentNode.getId() + "\n");
                writer.flush();
            }
        } finally {
            writer.close();
        }
    }

    //wsz 图上随机游走
    private Node getRandomNode() {
        int size = graph.getNodeCount();
        if (size == 0) return null;
        int index = new Random().nextInt(size);
        return graph.getNode(index);
    }

    private Edge getRandomEdge(Iterator<Edge> edgeIterator) {
        List<Edge> edges = new ArrayList<>();
        while (edgeIterator.hasNext()) {
            edges.add(edgeIterator.next());
        }
        return edges.get(new Random().nextInt(edges.size()));
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in); // 创建Scanner对象
        System.out.println("请输入文本文件的地址：");
        String input = scanner.nextLine(); // 读取用户输入的一行文本
        TextAnalyzer tg = new TextAnalyzer(input);
        tg.showDirectedGraph();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("选择你要实现的功能`v´:\n" +
                " 3)查询桥接词\n" +
                " 4)根据bridge word生成新文本\n" +
                " 5)计算两个单词之间的最短路径\n" +
                " 6)随机游走\n");
        while (true) {
            while (scanner.hasNextLine()) {
                String fun = scanner.nextLine();

                if (fun.equals("3")) {
                    tg.init_graph();
                    System.out.println("Enter two words separated by space to find bridge words:");
                    String[] words = scanner.nextLine().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        System.out.println("选择你要实现的功能`v´:\n" +
                                " 3)查询桥接词\n" +
                                " 4)根据bridge word生成新文本\n" +
                                " 5)计算两个单词之间的最短路径\n" +
                                " 6)随机游走\n");

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
                    System.out.println("选择你要实现的功能`v´:\n" +
                            " 3)查询桥接词\n" +
                            " 4)根据bridge word生成新文本\n" +
                            " 5)计算两个单词之间的最短路径\n" +
                            " 6)随机游走\n");
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
                    System.out.println("选择你要实现的功能`v´:\n" +
                            " 3)查询桥接词\n" +
                            " 4)根据bridge word生成新文本\n" +
                            " 5)计算两个单词之间的最短路径\n" +
                            " 6)随机游走\n");
                }
                if (fun.equals("5")) {
                    tg.init_graph();
                    System.out.println("输入两个词");
                    String[] words = scanner.nextLine().split("\\s+");
                    if (words.length != 2) {
                        System.out.println("Please enter exactly two words.");
                        System.out.println("选择你要实现的功能`v´:\n" +
                                " 3)查询桥接词\n" +
                                " 4)根据bridge word生成新文本\n" +
                                " 5)计算两个单词之间的最短路径\n" +
                                " 6)随机游走\n");
                        continue;
                    }

                    List<String> path = tg.My_Dij(words[0].toLowerCase(), words[1].toLowerCase());

                    if ( path == null || path.isEmpty()){
                        System.out.println("不可达");
                        System.out.println("选择你要实现的功能`v´:\n" +
                                " 3)查询桥接词\n" +
                                " 4)根据bridge word生成新文本\n" +
                                " 5)计算两个单词之间的最短路径\n" +
                                " 6)随机游走\n");
                        continue;
                    }
                    System.out.println("选择你要实现的功能`v´:\n" +
                            " 3)查询桥接词\n" +
                            " 4)根据bridge word生成新文本\n" +
                            " 5)计算两个单词之间的最短路径\n" +
                            " 6)随机游走\n");
                }
                if (fun.equals("6"))
                {
                    tg.init_graph();
//                    String ret = tg.MyRandomWalk(1000);
//                    System.out.println(ret);
//                    // todo: 写入文件
//                    FileWriter writer;
//                    try {
//                        writer = new FileWriter("./randomwalk.txt", true);
//                        writer.write(ret);
//                        writer.flush();
//                        writer.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    tg.startTraversal("./output.txt");
                    System.out.println("选择你要实现的功能`v´:\n" +
                            " 3)查询桥接词\n" +
                            " 4)根据bridge word生成新文本\n" +
                            " 5)计算两个单词之间的最短路径\n" +
                            " 6)随机游走\n");
                }
                if (fun.equals("7"))
                {
                    System.out.println("GoodBye!");
                    return;
                }
            }
        }
    }
}
