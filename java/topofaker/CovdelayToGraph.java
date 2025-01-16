package Chou;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CovdelayToGraph {

    static final byte CHOOSE_TOPO_INDEX = 2;//1：Dfn     2：Cogentco      other：Deltacom
    static int CHOOSE_EDGE_NODE_PRECENT = 20;//10, 20, ..., 100
    static int TOPO_FAKE = 0;//0: real     1: topofaker
    /*
    * 1：topo tree for zhangshasha(output to file, please start code in order of 10 to 100)
    * 2：children_info   3：degree
    * 4: degree no introduction(max(DC), var(DC), max(EC), var(EC))  5: real_covdelay_list to python file
     */
    static final int OUTPUT_MATRIX = 4;
    static int EXPERIMENT_INDEX = 2;
    
    static int node_count = 0;
    static final int GET_TREE_DELTA = 8000;
    static final int DELAY_DEVIDED = 1000;//when OUTPUT_TYPE = 1,delay time will devide this num

    public static void main(String[] args) {
        main1(args);
        //main2(args);
    }

    public static void main1(String[] args) {
        for(int i = 10; i <= 100; i+=10){
            node_count = 0;
            CHOOSE_EDGE_NODE_PRECENT = i;
            main2(args);
        }
    }

    public static void main2(String[] args) {
        int source_host_num = getSourceHostNum();
        int[][] cov_delay = getCovDelay2(source_host_num);
        String[] tree_str = new String[source_host_num];
        for(int i = 0; i < source_host_num; i++)
            tree_str[i] = getTree(cov_delay[i], source_host_num-1, i);

        int[][] parent_info = convertStringToArray(tree_str);
        node_count = source_host_num;
        MyTree[] fake_tree = convertArrayToMyTree(parent_info);
        getDegree(fake_tree, parent_info);
    }

    public static MyTree[] convertArrayToMyTree(int[][] parent_info){
        MyTree[] res = new MyTree[parent_info.length];
        MyTree root = null;
        for(int i = 0; i < res.length; i++){
            MyTree[] temp = new MyTree[parent_info[i].length];
            for(int j = 0; j < temp.length; j++){
                temp[j] = new MyTree(j);
                temp[j].children = new ArrayList<>();
                if(j < node_count)
                    temp[j].flag = "node:" + j;
            }
            for(int j = 0; j < temp.length; j++){
                if(parent_info[i][j] == -1){//root
                    root = temp[j];
                    continue;
                }
                temp[parent_info[i][j]].children_count++;
                temp[parent_info[i][j]].children.add(temp[j]);
            }
            res[i] = root;
        }
        return res;
    }

    public static int[][] convertStringToArray(String[] links){
        int[][] parent_info = new int[links.length][];
        for(int i = 0; i <links.length; i++){
            String[] stsp = links[i].split("link: ");
            parent_info[i] = new int[stsp.length];

            for(int j = stsp.length-1; j > 0; j--) {
                String[] temp = stsp[j].trim().split(" ");
                int left = Integer.parseInt(temp[0]), right = Integer.parseInt(temp[1]);
                if(j == stsp.length-1)
                    parent_info[i][right] = -1;
                parent_info[i][left] = right;
            }
        }

        if(OUTPUT_MATRIX == 2){
            System.out.print("children_parent = new int[][]{");
            for(int i = 0; i <links.length; i++) {
                System.out.print("{");
                for (int j = 0; j < parent_info[i].length; j++)
                    System.out.print(parent_info[i][j] + ", ");
                System.out.println("},");
            }
            System.out.println("};");
        }
        return parent_info;
    }

    public static String getTree(int[] data, int dnum, int source_index){
        int vnum = dnum, maxnum = 4 * dnum;
        Node snode = new Node(source_index, true);//source
        Node[] nodes = new Node[maxnum];//V
        Edge last_edge;
        List<Edge> edges = new LinkedList<>();//E
        int[][] psd2 = new int[maxnum][maxnum];//rou(s,D2)      <- dt

        for(int i = 0; i < dnum; i++)
            nodes[i] = new Node(i);
        for(int i = 0, k = 0; i < dnum; i++)
            for(int j = i+1; j <dnum; j++,k++)
                psd2[i][j] = data[k];

        while (true) {
            //step 2.1
            //Find i,j ∈ D with the largest rou(i,j)
            int maxi = 0, maxj = 0, maxval = 0;//i <= j
            for (int i = 0; i < dnum; i++){
                if(nodes[i].del)
                    continue;
                for (int j = i; j < dnum; j++){
                    if(nodes[j].del)
                        continue;
                    if (psd2[i][j] > maxval) {
                        maxi = i;
                        maxj = j;
                        maxval = psd2[i][j];
                    }
                }
            }
            //D = D \ {i, j}
            nodes[maxi].del = true;
            nodes[maxj].del = true;

            //Create a node f as the parent of i and j
            Node newnode = new Node(vnum);
            //V = V U {f}
            nodes[vnum] = newnode;
            vnum++;
            //E = E U {(f,i), (f,j)}
            Edge edge1 = new Edge(maxi, newnode.index, source_index), edge2 = new Edge(maxj, newnode.index, source_index);
            edges.add(edge1);
            edges.add(edge2);

            //step2.2
            for (int i = 0; i < dnum; i++) {
                if (nodes[i].del)
                    continue;
                int row = Math.min(i, maxi), line = Math.max(i, maxi);
                //for every k ∈ D rou(i,j)-r(i,k) < △/2
                if (maxval - psd2[row][line] < GET_TREE_DELTA / 2) {
                    //D = D \ k;
                    nodes[i].del = true;
                    //E = E U (f,k)
                    Edge newedge = new Edge(i, newnode.index, source_index);
                    edges.add(newedge);
                }
            }

            //step23
            for (int i = 0; i < dnum; i++) {
                if (nodes[i].del)
                    continue;
                //for each k ∈ D, compute rou(k,f) = (rou(k,i)+rou(k,j))/2
                if (i < maxi)
                    psd2[i][newnode.index] = (psd2[i][maxi] + psd2[i][maxj]) / 2;
                else if (maxi < i && i < maxj)
                    psd2[i][newnode.index] = (psd2[maxi][i] + psd2[i][maxj]) / 2;
                else
                    psd2[i][newnode.index] = (psd2[maxi][i] + psd2[maxj][i]) / 2;
            }
            //D = D U f
            dnum++;

            //step3
            int count = 0, left_index = -1;
            for (int i = 0; i < dnum; i++) {
                if (!nodes[i].del){
                    count++;
                    left_index = i;
                }
                if (count > 1)
                    break;
            }
            //if |D| = 1, E = E U (s,k)
            if (count == 1){
                last_edge = new Edge(left_index, snode.index-1, source_index);
                edges.add(last_edge);
                break;
            }
        }
        String str = getTreeInOrder(edges, (maxnum >> 2) + 1);
        return str;
    }

    public static String getTreeInOrder(List<Edge> edges, int host_num){
        int n = edges.size(), source_index = edges.get(n-1).index2;
        int[] parent_index = new int[n+1];
        List<String>[] lists = new ArrayList[host_num];
        StringBuffer sb = new StringBuffer();
        Set<String> set = new HashSet<>();;
        for(Edge edge : edges)
            parent_index[edge.index] = edge.index2;
        for(int i = 0; i < host_num; i++){
            if(i == source_index)
                continue;
            int temp_root = i;
            lists[i] = new ArrayList<>();
            while(temp_root != source_index){
                lists[i].add("link: " + temp_root + " " + parent_index[temp_root] + " ");
                temp_root = parent_index[temp_root];
            }
            for(int j = lists[i].size()-1; j >= 0; j--)
                if(!set.contains(lists[i].get(j))){
                    set.add(lists[i].get(j));
                    sb.insert(0, lists[i].get(j));
                }

        }
        if(OUTPUT_MATRIX == 1 && source_index == 0){
            //System.out.print(sb.toString());
            String write_path = "fixed_arr_file/restore_tree_str/";

            if(TOPO_FAKE == 0)
                write_path += "restore_real/";
            else if(TOPO_FAKE == 1)
                write_path += "restore_topofaker/";

            if(CHOOSE_TOPO_INDEX == 1)
                write_path += "dfn/";
            else if(CHOOSE_TOPO_INDEX == 2)
                write_path += "cogentco/";
            else if(CHOOSE_TOPO_INDEX == 3)
                write_path += "deltacom/";
            write_path += "str" + EXPERIMENT_INDEX + ".txt";
            File write_file = new File(write_path);
            try {
                FileWriter fw = new FileWriter(write_file, StandardCharsets.UTF_8, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(sb.toString());
                bw.write("\n");
                bw.flush();
                bw.close();
                fw.close();
            }catch (Exception e){
                System.out.println(e);
                System.out.println("err when write restroe_real tree string");
            }
        }
        return sb.toString();
    }

    public static int getSourceHostNum(){
        int[] source_host_nums;
        if(CHOOSE_TOPO_INDEX == 1){
            source_host_nums = new int[]{5, 6, 8, 9, 11, 12, 14, 15, 17, 18};
        }else if(CHOOSE_TOPO_INDEX == 2){
            source_host_nums = new int[]{9, 15, 21, 26, 32, 38, 43, 49, 55, 60};
        }else{
            source_host_nums = new int[]{7, 10, 13, 16, 19, 22, 25, 28, 31, 34};
        }
        return source_host_nums[CHOOSE_EDGE_NODE_PRECENT/10-1];
    }

    public static int fillChild(Queue<MyTree> queue, int[] children, int[] children_count , int index){
        MyTree tree = queue.poll();
        if(tree.index < node_count)
            tree.flag = "node:" + tree.index;
        if(tree.children_count == 0)
            return 0;
        int len = children.length;
        if(index + tree.children_count > len)
            return -1;
        tree.children = new ArrayList<>();
        for(int i = 0; i < tree.children_count; i++){
            MyTree temp = new MyTree(children[index+i], children_count[index+i]);
            tree.children.add(temp);
            queue.offer(temp);
        }
        return tree.children_count;
    }

    public static boolean getPath(MyTree tree, int target, List<MyTree> list){
        list.add(tree);
        if(tree.index == target){
            list.remove(list.size()-1);
            return true;
        }
        if(tree.children_count == 0){
            list.remove(list.size()-1);
            return false;
        }
        for(MyTree child : tree.children)
            if(getPath(child, target, list))
                return true;
        list.remove(list.size()-1);
        return false;
    }

    public static List<MyTree> getPath(MyTree tree, int target){
        List<MyTree> list = new ArrayList<>();
        getPath(tree, target, list);
        if(list.size() <= 0){
            return list;
        }
        list.remove(0);
        return list;
    }

    public static void markPath(List<MyTree> s2d, List<MyTree> d2s, int s, int d){
        int l1 = s2d.size(), l2 = d2s.size(), count = 0, min = Math.min(l1, l2);
        while (count < min){
            MyTree s2dt = s2d.get(count), d2st = d2s.get(l2-count-1);
            if(l1 > l2) {
                if(count == min - 1)
                    s2dt = s2d.get(l1 - 1);
                else if(count == 0)
                    s2dt = s2d.get(0);
                else
                    s2dt = s2d.get(l1 * (count + 1) / min - 1);
            }else if(l1 < l2) {
                if(count == min - 1)
                    d2st = d2s.get(0);
                else if(count == 0)
                    d2st = d2s.get(l2-1);
                else
                    d2st = d2s.get(l1 * (count + 1) / min - 1);
            }
            if(s2dt.flag == null && d2st.flag == null){
                s2dt.flag = "node:" + node_count;
                d2st.flag = "node:" + node_count;
                node_count++;
            }else if(s2dt.flag == null && d2st.flag != null){
                s2dt.flag = d2st.flag;
            }else if(s2dt.flag != null && d2st.flag == null){
                d2st.flag = s2dt.flag;
            }else if(!s2dt.flag.equals(d2st.flag)){
                s2dt.conflict++;
                d2st.conflict++;
            }
            count++;
        }
    }

    public static void soutpath(List<MyTree> tree){
        for(MyTree tre : tree)
            System.out.print(tre.index + "->");
        System.out.println();
    }

    public static void getDegree(MyTree[] trees, int[][] parent_info){

        if(OUTPUT_MATRIX == 3 || OUTPUT_MATRIX == 4){
            int[][][] host_degrees = cal_host_degree(parent_info);
            int[][][] edge_degrees = cal_edge_degree(host_degrees, trees);
            if(OUTPUT_MATRIX == 3)
                soutToEachDegree(host_degrees, edge_degrees);
            else if(OUTPUT_MATRIX == 4)
                soutToEachDegreeForExcelPic(host_degrees, edge_degrees);
        }

    }
    public static int[][][] cal_edge_degree(int[][][] host_degrees, MyTree[] trees) {
        int[][][] res = new int[2][host_degrees[0].length][];
        for(int i = 0; i < trees.length; i++){
            res[0][i] = new int[host_degrees[0][i].length-1];
            res[1][i] = new int[3];
            cal_edge_degree(res, i, trees[i], host_degrees[0][i]);
        }
        return res;
    }

    public static void cal_edge_degree(int[][][] res, int row_index, MyTree tree, int[] host_degrees){
        if(tree.children == null || tree.children.size() <= 0)
            return;
        int host1 = tree.index;
        for(MyTree child : tree.children){
            int host2 = child.index, degree = host_degrees[host1]*host_degrees[host2];
            res[1][row_index][1] += degree;
            res[1][row_index][2] = Math.max(res[1][row_index][2], degree);
            res[0][row_index][res[1][row_index][0]] = degree;
            res[1][row_index][0]++;
            cal_edge_degree(res, row_index, child, host_degrees);
        }
    }

    public static int[][][] cal_host_degree(int[][] parent_info){
        int[][][] res = new int[2][parent_info.length][];
        for(int i = 0; i < parent_info.length; i++){
            res[0][i] = new int[parent_info[i].length];
            res[1][i] = new int[2];
            res[1][i][0] = 2 * (parent_info[i].length-1);
            for(int j = 0; j < parent_info[i].length; j++){
                if(parent_info[i][j] == -1)
                    continue;
                res[0][i][j]++;
                res[0][i][parent_info[i][j]]++;
            }
            for(int j = 0; j < parent_info[i].length; j++)
                res[1][i][1] = Math.max(res[0][i][j], res[1][i][1]);
        }
        return res;
    }


    public static void soutToEachDegree2(int[][][] host_degrees, int[][][] edge_degrees){
        for(int i = 0; i < host_degrees[0].length; i++){
            //host degrees
            int sum = host_degrees[1][i][0], max = host_degrees[1][i][1];
            float avg = (float)sum / host_degrees[0][i].length;
            float var = 0;
            for(int j = 0; j < host_degrees[0][i].length; j++){
                var += Math.pow((host_degrees[0][i][j] - avg) , 2);
            }
            System.out.println("source：\t" + i + "\nnode degree\tmax：\t" + ((float)max) + "\tvar：\t" + var);
            System.out.println("host_degrees={");
            for(int j = 0; j < host_degrees[0][i].length; j++){
                System.out.print(host_degrees[0][i][j] + ", ");
                //System.out.print(String.format("%.2f", (float)host_degrees[0][i][j] / sum) + ", ");
            }
            System.out.println("\n}");

            //edge degrees
            int sum2 = edge_degrees[1][i][1], max2 = edge_degrees[1][i][2];
            float avg2 = (float)sum2 / edge_degrees[0][i].length, var2 = 0;
            for(int j = 0; j < edge_degrees[0][i].length; j++){
                var2 += Math.pow(((float)edge_degrees[0][i][j] - avg2) , 2);
            }
            System.out.println("edge degree\tmax：\t" + max2 + "\tavg：\t" + avg2 + "\tvar：\t" + var2);
            System.out.println("edge_degrees={");
            for(int j = 0; j < edge_degrees[0][i].length; j++){
                System.out.print(edge_degrees[0][i][j] + ", ");
            }
            System.out.println("\n}\n");
        }
    }

    public static void soutToEachDegreeForExcelPic(int[][][] host_degrees, int[][][] edge_degrees){
        int i = 0;
        //host degrees
        int sum = host_degrees[1][i][0], max = host_degrees[1][i][1];
        float avg = 1f / host_degrees[0][i].length, var = 0;
        for(int j = 0; j < host_degrees[0][i].length; j++){
            var += Math.pow(((float)host_degrees[0][i][j]/sum - avg) , 2);
        }
        System.out.print(((float)max/sum) + "\t" + var + "\t");

        //edge degrees
        int sum2 = edge_degrees[1][i][1], max2 = edge_degrees[1][i][2];
        float avg2 = 1f / edge_degrees[0][i].length, var2 = 0;
        for(int j = 0; j < edge_degrees[0][i].length; j++){
            var2 += Math.pow(((float)edge_degrees[0][i][j]/sum2 - avg2) , 2);
        }
        System.out.println(((float)max2/sum2) + "\t" + var2);

    }

    public static void soutToEachDegree(int[][][] host_degrees, int[][][] edge_degrees){
        for(int i = 0; i < host_degrees[0].length; i++){
            //host degrees
            int sum = host_degrees[1][i][0], max = host_degrees[1][i][1];
            float avg = 1f / host_degrees[0][i].length;
            float var = 0;
            for(int j = 0; j < host_degrees[0][i].length; j++){
                var += Math.pow(((float)host_degrees[0][i][j]/sum - avg) , 2);
            }
            System.out.println("src：\t" + i + "\nnode importance\tmax：\t" + ((float)max/sum) + "\tavg：\t" + avg  + "\tvar：\t" + var);
            System.out.println("host_degrees={");
            for(int j = 0; j < host_degrees[0][i].length; j++){
                System.out.print(String.format("%.2f", (float)host_degrees[0][i][j] / sum) + ", ");
            }
            System.out.println("\n}");

            //edge degrees
            int sum2 = edge_degrees[1][i][1], max2 = edge_degrees[1][i][2];
            float avg2 = 1f / edge_degrees[0][i].length, var2 = 0;
            for(int j = 0; j < edge_degrees[0][i].length; j++){
                var2 += Math.pow(((float)edge_degrees[0][i][j]/sum2 - avg2) , 2);
            }
            System.out.println("edge importance\tmax：\t" + ((float)max2/sum2) + "\tavg：\t" + avg2 + "\tvar：\t" + var2);
            System.out.println("edge_degrees={");
            for(int j = 0; j < edge_degrees[0][i].length; j++){
                System.out.print((float)edge_degrees[0][i][j]/sum2 + ", ");
            }
            System.out.println("\n}\n");
        }
    }

    public static void mout(MyTree trees){
        System.out.print(trees.index + "-" + trees.flag + ": ");
        if(trees.children != null)
            for(MyTree t : trees.children)
                System.out.print(t.index + "-" + t.flag + "\t");
        System.out.println();
        if(trees.children != null)
            for(MyTree t : trees.children){
                mout(t);
            }
    }

    public static DegreeCount[] cal_total_degree(MyTree[] trees) {
        DegreeCount[] degrees = new DegreeCount[200];
        for (int i = 0; i < trees.length; i++)
            cal_degree(trees[i], degrees);
        DegreeCount[] res = new DegreeCount[node_count];
        for(int i = 0; i < res.length; i++)
            res[i] = degrees[i];
        return res;
    }
    public static void soutTotalDegree(DegreeCount[] degrees){
        int sum = 0, max = 0;
        int[][] edge_info = new int[2][];
        edge_info[0] = new int[degrees.length*degrees.length];
        edge_info[1] = new int[3];//edge_count, sum, max
        for(int j = 0; j < degrees.length; j++){
            sum += degrees[j].count;
            max = Math.max(max, degrees[j].count);
            for(int i : degrees[j].set){
                if(i < j)
                    continue;
                else if(i == j)
                    System.out.println("errrr");
                int d = degrees[j].count * degrees[i].count;
                edge_info[0][edge_info[1][0]++] = d;
                edge_info[1][1] += d;
                edge_info[1][2] = Math.max(edge_info[1][2], d);
            }
        }
        float avg = (float)sum / degrees.length, var = 0;
        float avg2 = (float)edge_info[1][1] / edge_info[1][0], var2 = 0;
        for(int j = 0; j < degrees.length; j++){
            var += Math.pow(((float)degrees[j].count - avg) , 2);
        }
        for(int j = 0; j < edge_info[1][0]; j++){
            var2 += Math.pow(((float)edge_info[0][j] - avg2) , 2);
        }

        System.out.println("after merge：\nnode degree：\tmax：\t" + max + "\tavg：\t" + avg + "\tvar：\t" + var);
        System.out.println("host_degrees = {");
        for(int j = 0; j < degrees.length; j++)
                System.out.print(degrees[j].count + ", ");
        System.out.println("\n}");

        System.out.println("edge degree：\tmax：\t" + edge_info[1][2] + "\tavg：\t" + avg2 + "\tvar：\t" + var2);
        System.out.println("edge_degrees = {");
        for(int j = 0; j < edge_info[1][0]; j++)
            System.out.print(edge_info[0][j] + ", ");
        System.out.println("\n}\n");
    }

    public static void cal_degree(MyTree tree, DegreeCount[] degrees){
        if(tree.children != null && tree.children.size() > 0){
            int i = getFlagIndex(tree);
            if(degrees[i] == null)
                degrees[i] = new DegreeCount(i);
            for(MyTree t : tree.children){
                int j = 0;
                if(t.flag == null){
                    t.flag = "node:" + node_count;
                    j = node_count;
                    node_count++;
                }else
                    j = getFlagIndex(t);
                if(degrees[j] == null)
                    degrees[j] = new DegreeCount(j);

                else if(i != j && !degrees[i].set.contains(j)){
                    degrees[i].set.add(j);
                    degrees[i].count++;
                    degrees[j].set.add(i);
                    degrees[j].count++;
                }
                cal_degree(t, degrees);
            }
        }
    }

    public static int getFlagIndex(MyTree tree){
        return Integer.parseInt(tree.flag.split(":")[1]);
    }


    public static int[][] getCovDelay2(int host_num){
        String path = "ns3_res/ns3_res_exp" + EXPERIMENT_INDEX + "/";

        if(TOPO_FAKE == 0)
            path += "real/";
        else if(TOPO_FAKE == 1)
            path += "topofaker/";
        else
            return null;

        if(CHOOSE_TOPO_INDEX == 1)
            path += "dfn/dfn_res/dfn_";
        else if(CHOOSE_TOPO_INDEX == 2)
            path += "cogentco/cogentco_res/cogentco_";
        else
            path += "deltacom/deltacom_res/deltacom_";

        path += CHOOSE_EDGE_NODE_PRECENT + "_delay/";
        int[][][] delay = new int[host_num][(host_num-1)*(host_num-2)*3/2][2];//[host_num][(host_num-1)*(host_num-2)*3/2][2]: pkt_id & delay
        int[][] res = new int[host_num][(host_num-1)*(host_num-2)/2];
        for(int i = 0; i < host_num; i++){
            int index = 0;
            try {
                File file = new File(path + "10.2.0." + (i*2+1) + ".txt");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.equals(""))
                        continue;
                    String[] strs = line.trim().split("\t");
                    delay[i][index][0] = Integer.parseInt(strs[1].trim());
                    String str_delay = strs[strs.length-1].trim();
                    delay[i][index][1] = Integer.parseInt(str_delay.substring(0, str_delay.length()-4));
                    index++;
                }
                reader.close();
            } catch (IOException e) {
                System.err.println("can not read file: " + path + e.getMessage());
            }
        }

        for (int i = 0; i < delay.length; i++) {
            Arrays.sort(delay[i], (a, b) -> Integer.compare(a[0], b[0]));
        }

        for(int i = 0; i < host_num; i++)
            for(int j = 0; j < res[i].length; j++){
                if(delay[i][j*3][1] == 0 || delay[i][j*3+1][1] == 0 || delay[i][j*3+2][1] == 0){
                    System.err.println("cov delay is zero\n");
                    return null;
                }

                res[i][j] = delay[i][j*3+2][1] - delay[i][j*3][1];
                if(res[i][j] <= 0){
                    System.out.println("10.2.0." + (i*2+1) + ".txt at row " + (j*2+1) + " with : " + delay[i][j*3+2][1] + " - " + delay[i][j*3][1]);
                    System.err.println("cov delay is negative\n");
                    return null;
                }
            }

        if(OUTPUT_MATRIX == 5 && TOPO_FAKE  == 0){
            String cov_delay_output_path = "fake_delay/fake_delay_exp" + EXPERIMENT_INDEX + "/topofaker/";
            if(CHOOSE_TOPO_INDEX == 1)
                cov_delay_output_path += "dfn/dfn";
            else if(CHOOSE_TOPO_INDEX == 2)
                cov_delay_output_path += "cogentco/cogentco";
            else
                cov_delay_output_path += "deltacom/deltacom";
            cov_delay_output_path += "_" + CHOOSE_EDGE_NODE_PRECENT + ".py";
            File file = new File(cov_delay_output_path);
            try {
                if(file.exists())
                    file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("# coding: utf-8\n" +
                        "from mip import Model, xsum, maximize, BINARY, minimize, INTEGER\n" +
                        "import time\n");
                bw.write("\nreal_covdelay_list = [\\\n");
                for(int i = 0; i < host_num; i++){
                    for(int j = 0; j < res[i].length; j++){
                        bw.write(res[i][j]/DELAY_DEVIDED + ", ");
                    }
                    bw.write("\\\n");
                }
                bw.write("]\n");
                bw.flush();
                bw.close();
                fw.close();
            }catch (Exception e){
                System.out.println(e);
                System.out.println("err when write fake_cov_delay to file");
            }
        }

        return res;
    }
}

class Node{
    int index;
    boolean del;
    Node(int index){
        this.index = index;
        this.del = false;
    }
    Node(int index, boolean del){
        this.index = index;
        this.del = del;
    }
}
class Edge{
    int index, index2;
    Edge(int index, int index2, int source){
        this.index = index >= source ? index+1 : index;
        this.index2 = index2+1;
    }
}

class MyTree{
    int index;
    int children_count;
    int conflict;
    String flag;
    List<MyTree> children;
    MyTree(int index){
        this.index = index;
        this.children_count = 0;
    }
    MyTree(int index, int children_count){
        this.index = index;
        this.children_count = children_count;
    }
}

class DegreeCount{
    int index;
    int count;
    Set<Integer> set;
    DegreeCount(int index){
        this.index = index;
        this.set = new HashSet<>();
    }
}