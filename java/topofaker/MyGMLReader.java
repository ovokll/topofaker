package Chou;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyGMLReader {

    static final int CHOOSE_TOPO_INDEX = 3;//1:dfn, 2:cogentco, 3:deltacom
    static final boolean USE_RAND_SEED = false;
    static final int RAND_SEED = 56;

    public static void main(String[] args) {
        String[] str = new String[]{"Dfn", "Cogentco", "Deltacom"};

        String fileStr = "", filePath = "gmlFolder/" + str[CHOOSE_TOPO_INDEX-1] + ".gml";
        int nodeCount = 0, edgeCount = 0, edgeDocumpNum = 0;
        Set<String> set = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                if(line.equals("  node ["))
                    nodeCount++;
                else if(line.equals("  edge ["))
                    edgeCount++;
            }
            reader.close();
            fileStr = stringBuilder.toString();
        } catch (IOException e) {
            System.err.println("can not read file: " + e.getMessage());
        }
        ArrayList<Integer> linkFrom = new ArrayList<>(), linkTo = new ArrayList<>();
        String regex = "edge \\[.*?\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileStr);
        int check[] = new int[edgeCount], nocheck = 0, degrees[] = new int[nodeCount];
        while (matcher.find()) {
            String edge = matcher.group(), strs[] = edge.split(" ");
            int from = Integer.parseInt(strs[6]), to = Integer.parseInt(strs[11]);
            String temp = from + "-" + to;
            if(set.contains(temp)) {
                edgeDocumpNum++;
                continue;
            }else
                set.add(temp);
            linkFrom.add(from);
            linkTo.add(to);
            degrees[from]++;
            degrees[to]++;
            if(strs[16].startsWith("\"e"))
                check[Integer.parseInt(strs[16].substring(2, strs[16].length()-1))]++;
            else
                nocheck++;
        }
        System.out.println();
        System.out.println("total node count: " + nodeCount);
        System.out.println("total edge count: " + linkFrom.size());

        System.out.println();
        System.out.print("int linkFrom[] = {");
        soutArrInLine(linkFrom);
        System.out.println("};");
        System.out.print("int linkTo[] = {");
        soutArrInLine(linkTo);
        System.out.println("};");

        int[] sortDegree = new int[nodeCount];
        for(int i = 0; i < nodeCount; i++)
            sortDegree[i] = degrees[i];

        Arrays.sort(sortDegree);
        int threshold = 0, edgeNodeCount = 0;
        for(int i = 1; i < nodeCount; i++) {
            if (sortDegree[i - 1] != sortDegree[i] && i * 10 >= nodeCount * 3) {
                threshold = sortDegree[i - 1];
                break;
            }else if(sortDegree[i - 1] != sortDegree[i]){
            }
        }

        ArrayList<Integer>[] degree_nodes = new ArrayList[threshold];
        for(int i = 0; i < threshold; i++)
            degree_nodes[i] = new ArrayList<>();
        System.out.println("edge node array（ degree <= degree at 30%）: ");
        System.out.println();
        System.out.print("int[] edgeNode = {");
        boolean flag = true;
        for(int i = 0; i < nodeCount; i++) {
            if (degrees[i] <= threshold) {
                degree_nodes[degrees[i]-1].add(i);
                if (flag) {
                    System.out.print(i);
                    flag = false;
                } else
                    System.out.print(", " + i);
                edgeNodeCount++;
            }
        }
        System.out.println("};");
        System.out.println();
        System.out.println( "edge node num is " + edgeNodeCount + " ：");
        System.out.print("int[][] edge_nodes = new int[][]{");
        for(int i = 0; i < threshold; i++){
            for(int j = 0; j < degree_nodes[i].size(); j++){
                if(j == 0)
                    System.out.print("{" + degree_nodes[i].get(j));
                else
                    System.out.print(", " + degree_nodes[i].get(j));
            }
            if(i < threshold - 1)
                System.out.println("}, ");
            else
                System.out.println("}");
        }
        System.out.println("};");


        int choosed_num = (int)Math.ceil(nodeCount * 3.0f / 10);
        Integer[] choosed_hosts1 = degree_nodes[0].toArray(new Integer[degree_nodes[0].size()]);
        int[] choosed_hosts2 = getRandomArr(degree_nodes[1], choosed_num - degree_nodes[0].size());

        Set<Integer> set1 = new HashSet<>();
        System.out.println("\nthe num of edge node choosed for experiment is " + choosed_num + "：");
        System.out.print("edge_nodes = new int[][]{");
        for(int j = 0; j < degree_nodes[0].size(); j++){
            if(set1.contains(degree_nodes[0].get(j)))
                System.out.println("err");
            else
                set1.add(degree_nodes[0].get(j));
            if(j == 0)
                System.out.print("{" + degree_nodes[0].get(j));
            else
                System.out.print(", " + degree_nodes[0].get(j));
        }
        System.out.println("}, ");
        for(int j = 0; j < choosed_hosts2.length; j++){
            if(set1.contains(choosed_hosts2[j]))
                System.out.println("err2");
            else
                set1.add(choosed_hosts2[j]);
            if(j == 0)
                System.out.print("{" + choosed_hosts2[j]);
            else
                System.out.print(", " + choosed_hosts2[j]);
        }
        System.out.println("}\n};");

    }


    public static int[] getRandomArr(ArrayList<Integer> arr, int num){
        int[] res = new int[num];
        Random rand = null;
        if(USE_RAND_SEED)
            rand = new Random(RAND_SEED);
        else
            rand = new Random();
        int left_node = num;
        boolean[] choosed = new boolean[num];
        for(int i = 0; i < num; i++){
            int rand_num = rand.nextInt(left_node);
            for(int j = 0, k = 0; j < arr.size() && k < left_node; j++){
                if(!choosed[j] && k == rand_num) {
                    res[i] = arr.get(j);
                    choosed[j] = true;
                    left_node--;
                    break;
                }else if(!choosed[j])
                    k++;
            }
        }
        return res;
    }

    public static int[] mergeArray(int[] arr1, int[] arr2){
        int la1 = arr1.length, la2 = arr2.length, res[] = new int[la1+la2], index = 0;
        for(int i = 0; i < la1; i++)
            res[index++] = arr1[i];;
        for(int i = 0; i < la2; i++)
            res[index++] = arr2[i];
        return res;
    }

    public static void soutArrInLine(ArrayList<Integer> arr){
        for(int i = 0; i < arr.size(); i++)
            if(i == arr.size() -1)
                System.out.print(arr.get(i));
            else
                System.out.print(arr.get(i) + ", ");
    }
}
