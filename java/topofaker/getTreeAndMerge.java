package Chou;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class getTreeAndMerge {
    static final byte CHOOSE_TOPO_INDEX = 1;//1：Dfn     2：Cogentco      other：Deltacom
    static int CHOOSE_EDGE_NODE_PRECENT = 10;//10, 20, ..., 100
    static final int EXPERIMENT_INDEX = 1;
    // 1:real_shortest_path  2:attack_topo  4:fake_shortest_paths
    //5:(no use)     6:(no use)
    //7：parent_info   8：degree      9：degree for excel
    static final int OUTPUT_MATRIX = 9;
    static final int DEGREE_THRESHOLD = 3;
    static final int INF = Integer.MAX_VALUE;
    static int node_count = 0;

    public static void main(String[] args) {
        if(OUTPUT_MATRIX == 9)
            System.out.println("rhmax\trhvar\tremax\trevar\tfhmax\tfhvar\tfemax\tfevar");
        for(int i = 10; i <= 100; i+= 10){
            CHOOSE_EDGE_NODE_PRECENT = i;
            main2(args);
            System.out.println();
        }
    }

    public static void main2(String[] args) {
        int[][] no_detection_topo = initTopo();
        int[] detection_node = getDetectionNode();
        int[][] all_topo = topo_add_detection_node(no_detection_topo, detection_node);
        String[] real_tree_str = new String[detection_node.length];
        ArrayList<ArrayList<Integer>>[] real_shortest_path = fixedRealPath(detection_node.length, no_detection_topo.length, real_tree_str);
        int[][] attack_topo = GetAttackTopo(all_topo, real_shortest_path);
        int sender_start_index = attack_topo.length - detection_node.length;
        int[][] fake_topo = getWeightFakeTopo(attack_topo, DEGREE_THRESHOLD);
        String[] fake_tree_str = new String[detection_node.length];
        WeightedDijkstra(fake_topo, sender_start_index, detection_node.length, fake_tree_str);

        int[][] real_parent_info = convertStringToArray(real_tree_str);
        node_count = detection_node.length;
        MyTree[] real_tree = convertArrayToMyTree(real_parent_info);
        if(OUTPUT_MATRIX == 8)
            System.out.println("real type：");
        mergeTree(real_tree);

        node_count = detection_node.length;
        int[][] fake_parent_info = convertStringToArray(fake_tree_str);
        MyTree[] fake_tree = convertArrayToMyTree(fake_parent_info);
        if(OUTPUT_MATRIX == 8)
            System.out.println("topoFaker type：");
        mergeTree(fake_tree);

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

    public static void mergeTree(MyTree[] trees) {
        int n = trees.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                //get two path
                List<MyTree> i2j = getPath(trees[i], j), j2i = getPath(trees[j], i);
                markPath(i2j, j2i, i, j);
            }
        }

        if (OUTPUT_MATRIX == 8 || OUTPUT_MATRIX == 9) {
            DegreeCount[] total_degrees = cal_total_degree(trees);
            if(OUTPUT_MATRIX == 8)
                soutTotalDegree(total_degrees);
            else if(OUTPUT_MATRIX == 9)
                soutTotalDegree2(total_degrees);
        }
    }

    public static void soutTotalDegree2(DegreeCount[] degrees){
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
        float avg = 1f / degrees.length, var = 0;
        float avg2 = 1f / edge_info[1][0], var2 = 0;
        for(int j = 0; j < degrees.length; j++){
            var += Math.pow(((float)degrees[j].count/sum - avg) , 2);
        }
        for(int j = 0; j < edge_info[1][0]; j++){
            var2 += Math.pow(((float)edge_info[0][j]/edge_info[1][1] - avg2) , 2);
        }

        //host_max  host_var    edge_max    edge_var
        System.out.print(((float)max/sum) + "\t" + var);
        System.out.print("\t" + ((float)edge_info[1][2]/edge_info[1][1]) + "\t" + var2 + "\t");

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
        float avg = 1f / degrees.length, var = 0;
        float avg2 = 1f / edge_info[1][0], var2 = 0;
        for(int j = 0; j < degrees.length; j++){
            var += Math.pow(((float)degrees[j].count/sum - avg) , 2);
        }
        for(int j = 0; j < edge_info[1][0]; j++){
            var2 += Math.pow(((float)edge_info[0][j]/edge_info[1][1] - avg2) , 2);
        }

        System.out.println("after merge：\nhost degree：\tmax：\t" + ((float)max/sum) + "\tavg：\t" + avg + "\tvar：\t" + var);
        System.out.println("host_degrees = {");
        for(int j = 0; j < degrees.length; j++)
            System.out.print(((float)degrees[j].count/sum) + ", ");
        System.out.println("\n}");

        System.out.println("edge degree：\tmax：\t" +((float)edge_info[1][2]/edge_info[1][1]) + "\tavg：\t" + avg2 + "\tvar：\t" + var2);
        System.out.println("edge_degrees = {");
        for(int j = 0; j < edge_info[1][0]; j++)
            System.out.print(((float)edge_info[0][j]/edge_info[1][1]) + ", ");
        System.out.println("\n}\n");
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

    public static void markPath(List<MyTree> s2d, List<MyTree> d2s, int s, int d){
        int l1 = s2d.size(), l2 = d2s.size(), count = 0, min = Math.min(l1, l2);
        if(l1 != l2)
            System.out.println(s + " to " + d + " distence not same");
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


    public static List<MyTree> getPath(MyTree tree, int target){
        List<MyTree> list = new ArrayList<>();
        getPath(tree, target, list);
        if(list.size() <= 0){
            return list;
        }
        list.remove(0);
        return list;
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

    public static DegreeCount[] cal_total_degree(MyTree[] trees) {
        DegreeCount[] degrees = new DegreeCount[5000];
        for (int i = 0; i < trees.length; i++)
            cal_degree(trees[i], degrees);
        DegreeCount[] res = new DegreeCount[node_count];
        for(int i = 0; i < res.length; i++)
            res[i] = degrees[i];
        return res;
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

        if(OUTPUT_MATRIX == 7){
            System.out.print("children_parent = new int[][]{");
            for(int i = 0; i <links.length; i++) {
                System.out.print("{");
                for (int j = 0; j < parent_info[i].length; j++)
                    System.out.print(j + ":" + parent_info[i][j] + ", ");
                System.out.println("},");
            }
            System.out.println("};");
        }
        return parent_info;
    }

    public static ArrayList<ArrayList<Integer>> myFindPath(int[][] topo, int detection_host_start_index, int detection_host_num, int path_start_index){
        int[] destlen = new int[detection_host_num];
        int[] step = new int[topo.length];
        step[path_start_index] = 1;
        PathSave[] pss = new PathSave[detection_host_num];

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(path_start_index);
        Queue<PathCount> pathqueue = new LinkedList<>();
        pathqueue.offer(new PathCount(1, "path: "));
        Arrays.fill(destlen, INF);

        while(!queue.isEmpty()){
            PathCount path = pathqueue.poll();
            int count = 0;
            while (count < path.count){
                int node = queue.poll();
                int addcount = 0;
                for(int opnode = 0; opnode < topo.length; opnode++){
                    if(topo[node][opnode] != 1)
                        continue;
                    for(int i = 0; i < detection_host_num; i++){
                        int end = detection_host_start_index + i;
                        if(end == path_start_index)
                            continue;
                        if(opnode == end && path.str.length() <= destlen[i]){
                            destlen[i] = path.str.length();
                            String fp = path.str + "->" + String.format("%03d", node) + "->" + String.format("%03d", opnode);
                            if(pss[i] == null)
                                pss[i] = new PathSave(fp);
                            else {
                                PathSave temp = pss[i];
                                while (temp.next != null)
                                    temp = temp.next;
                                temp.next = new PathSave(fp);
                            }
                        }
                    }
                    if(step[opnode] == 0)
                        step[opnode] = step[node] + 1;
                    else if(step[opnode] != step[node]+1)
                        continue;
                    addcount++;
                    queue.add(opnode);
                }
                pathqueue.offer(new PathCount(addcount, path.str + "->" + String.format("%03d", node)));
                count++;
            }
        }

        ArrayList<ArrayList<Integer>> res = new ArrayList<>();

        for(PathSave ps : pss){
            ArrayList<Integer> inres = new ArrayList<>();
            while(ps != null){
                String[] pssp = ps.path.split("->");
                for(int i = 1; i < pssp.length; i++){
                    inres.add(Integer.parseInt(pssp[i]));
                }
                ps = null;
            }
            if(inres.size() > 0)
                res.add(inres);
        }
        return res;
    }


    public static int[][] topo_add_detection_node(int[][] topo, int[] add_link_to_node){
        int tl = topo.length, al = add_link_to_node.length, rl = tl + al;
        int[][] res = new int[rl][rl];
        for(int i = 0; i < rl; i++){
            Arrays.fill(res[i], INF);
            res[i][i] = 0;
        }
        for(int i = 0; i < tl; i++)
            for(int j = 0; j < tl; j++)
                res[i][j] = topo[i][j];

        for(int i = 0; i < al; i++){
            res[tl+i][add_link_to_node[i]] = 1;
            res[add_link_to_node[i]][tl+i] = 1;
        }

        return res;
    }
    public static int[] getDetectionNode(){

        String path = "fixed_arr_file/detection_node/link" + EXPERIMENT_INDEX + ".txt";
        int[] res = null;
        try {
            int count = 0, need_skip_line = (CHOOSE_TOPO_INDEX - 1) * 10 + CHOOSE_EDGE_NODE_PRECENT / 10 - 1;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            for(int i = 0; i < need_skip_line; i++){//跳过前面的行
                line = reader.readLine();
                if(line == null || line.equals(""))
                    return null;
            }
            line = reader.readLine();
            String[] strs = line.split(", ");
            res = new int[strs.length];
            for(int i = 0; i < strs.length; i++)
                res[i] = Integer.parseInt(strs[i]);
            reader.close();
        } catch (IOException e) {
            System.err.println("can not read file: " + path + e.getMessage());
        }
        return res;
    }

    public static int[][] initTopo(){
        int[] linkFrom, linkTo;
        int nodeNum;
        if(CHOOSE_TOPO_INDEX == 1){
            linkFrom = new int[]{0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 10, 10, 11, 12, 13, 14, 14, 14, 16, 16, 17, 17, 17, 18, 18, 19, 19, 20, 21, 21, 22, 23, 25, 26, 27, 28, 28, 29, 30, 30, 32, 32, 33, 33, 34, 34, 35, 36, 37, 38, 39, 40, 40, 41, 42, 42, 43, 44, 44, 45, 46, 47, 47, 48, 48, 48, 48, 50, 50, 50, 50, 51, 51, 52, 52, 52, 52, 53, 55, 56};
            linkTo = new int[]{1, 3, 53, 6, 15, 56, 49, 52, 53, 51, 5, 10, 7, 53, 51, 51, 51, 11, 36, 43, 52, 52, 24, 50, 27, 50, 23, 25, 50, 31, 19, 38, 51, 20, 46, 22, 23, 51, 24, 27, 50, 44, 51, 36, 51, 48, 31, 33, 50, 50, 37, 35, 52, 50, 51, 52, 39, 44, 41, 43, 53, 43, 53, 51, 50, 45, 46, 51, 51, 53, 56, 49, 52, 57, 51, 52, 53, 55, 52, 53, 53, 54, 55, 56, 54, 56, 57};
            nodeNum = 58;
        }else if(CHOOSE_TOPO_INDEX == 2){
            linkFrom = new int[]{0, 0, 1, 1, 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 7, 8, 8, 8, 10, 10, 11, 12, 12, 12, 13, 13, 14, 14, 14, 16, 18, 18, 19, 19, 19, 20, 20, 21, 22, 22, 24, 25, 25, 26, 26, 26, 28, 28, 29, 30, 31, 32, 32, 34, 35, 36, 36, 37, 37, 38, 39, 40, 40, 41, 41, 42, 42, 42, 44, 44, 45, 45, 46, 46, 48, 48, 49, 49, 49, 50, 50, 51, 52, 52, 53, 54, 56, 56, 58, 60, 60, 61, 61, 62, 62, 62, 63, 63, 64, 64, 64, 65, 66, 67, 69, 70, 70, 71, 71, 72, 73, 74, 75, 75, 76, 77, 77, 77, 78, 78, 80, 80, 80, 80, 82, 82, 82, 83, 84, 84, 86, 87, 89, 90, 91, 91, 92, 92, 92, 94, 95, 95, 96, 97, 98, 98, 99, 100, 101, 101, 101, 102, 103, 103, 105, 105, 105, 107, 107, 108, 109, 110, 111, 111, 112, 113, 113, 115, 115, 117, 117, 119, 119, 120, 121, 121, 123, 123, 123, 127, 127, 129, 131, 132, 133, 134, 134, 134, 136, 137, 138, 138, 139, 140, 142, 143, 144, 145, 146, 146, 147, 147, 148, 149, 151, 152, 153, 153, 154, 154, 155, 155, 155, 156, 157, 158, 158, 158, 161, 162, 162, 163, 163, 165, 165, 165, 165, 166, 166, 167, 168, 169, 178, 181, 182, 183, 183, 186, 190, 192, 193, 195};
            linkTo = new int[]{176, 9, 8, 176, 114, 116, 175, 76, 77, 4, 77, 6, 135, 131, 6, 7, 8, 174, 194, 9, 191, 11, 13, 16, 32, 13, 30, 16, 15, 64, 129, 15, 17, 19, 30, 89, 68, 82, 21, 23, 26, 188, 23, 27, 171, 55, 27, 28, 29, 51, 54, 78, 35, 37, 33, 37, 37, 37, 38, 39, 160, 38, 196, 181, 41, 42, 43, 189, 43, 143, 143, 45, 47, 48, 164, 49, 47, 155, 181, 177, 147, 165, 57, 51, 188, 53, 55, 58, 55, 57, 59, 59, 61, 69, 128, 122, 144, 86, 63, 68, 149, 65, 67, 68, 66, 67, 69, 144, 79, 183, 72, 79, 73, 74, 183, 173, 183, 173, 152, 162, 133, 94, 79, 81, 81, 86, 87, 88, 83, 150, 148, 148, 85, 87, 88, 150, 172, 99, 92, 96, 93, 183, 171, 96, 171, 97, 172, 194, 131, 100, 132, 104, 180, 102, 109, 104, 106, 106, 107, 179, 108, 129, 179, 110, 128, 112, 140, 137, 114, 191, 116, 118, 120, 118, 176, 175, 175, 122, 124, 124, 125, 126, 128, 130, 130, 142, 135, 173, 137, 138, 135, 139, 172, 174, 141, 174, 141, 143, 185, 149, 157, 152, 154, 166, 177, 154, 150, 152, 153, 160, 154, 159, 183, 195, 156, 165, 157, 158, 183, 196, 165, 162, 163, 167, 187, 164, 177, 181, 184, 186, 170, 183, 168, 169, 185, 179, 196, 189, 184, 186, 187, 191, 193, 194, 196};
            nodeNum = 197;
        }else{
            linkFrom = new int[]{0, 0, 0, 0, 0, 1, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6, 6, 6, 7, 8, 8, 9, 10, 10, 10, 10, 11, 11, 11, 12, 12, 14, 14, 15, 15, 16, 17, 18, 18, 19, 19, 19, 20, 20, 20, 21, 22, 23, 24, 24, 24, 25, 25, 25, 25, 27, 28, 29, 30, 30, 30, 30, 31, 31, 31, 32, 32, 33, 34, 36, 38, 40, 40, 41, 42, 43, 44, 46, 47, 47, 47, 47, 47, 48, 48, 49, 49, 50, 50, 50, 50, 52, 53, 53, 54, 54, 54, 54, 55, 55, 56, 58, 58, 58, 59, 60, 60, 61, 62, 62, 62, 63, 63, 64, 64, 64, 66, 66, 66, 68, 68, 70, 72, 72, 72, 73, 74, 74, 74, 75, 75, 76, 77, 77, 77, 78, 79, 81, 82, 87, 88, 89, 89, 91, 93, 94, 95, 97, 98, 99, 99, 100, 101, 105, 106, 107, 108, 109};
            linkTo = new int[]{64, 1, 9, 8, 63, 65, 55, 87, 88, 4, 45, 86, 47, 86, 5, 6, 7, 46, 80, 81, 62, 8, 86, 63, 63, 16, 104, 11, 13, 16, 104, 13, 19, 13, 17, 84, 97, 47, 104, 43, 19, 35, 104, 36, 37, 21, 22, 23, 26, 83, 105, 25, 27, 95, 89, 26, 28, 92, 83, 29, 92, 103, 36, 77, 31, 112, 36, 37, 33, 34, 38, 35, 37, 39, 41, 49, 42, 43, 45, 47, 47, 73, 51, 84, 111, 60, 57, 49, 84, 54, 57, 91, 52, 51, 53, 59, 60, 56, 90, 111, 55, 94, 111, 57, 105, 61, 110, 60, 104, 97, 82, 80, 70, 85, 85, 70, 65, 66, 67, 67, 69, 85, 69, 71, 71, 73, 75, 81, 98, 112, 75, 103, 104, 76, 100, 81, 100, 103, 101, 102, 101, 83, 88, 93, 96, 90, 92, 94, 95, 96, 98, 99, 104, 112, 102, 102, 106, 107, 108, 109, 110};
            nodeNum = 113;
        }
        int[][] topo = new int[nodeNum][nodeNum];
        for(int i = 0; i < nodeNum; i++){
            Arrays.fill(topo[i], INF);
            topo[i][i] = 0;
        }
        for(int i = 0; i < linkFrom.length; i++){
            topo[linkFrom[i]][linkTo[i]] = 1;
            topo[linkTo[i]][linkFrom[i]] = 1;
        }

        return topo;
    }

    public static ArrayList<ArrayList<Integer>>[] WeightedDijkstra(int[][] topo, int source_start_index, int source_num, String[] tree){
        int path_len[][] = new int[topo.length][];
        ArrayList<ArrayList<Integer>>[] paths = new ArrayList[source_num];

        for(int i = 0; i < source_num; i++){
            paths[i] = new ArrayList<>();
            path_len[i] = getPathWithWeightDijkstra(topo, source_start_index+i, paths[i]);
        }

        if(OUTPUT_MATRIX == 4) {
            System.out.println("src\tdest\tweight\tpath");
            for (int i = 0; i < source_num; i++)
                for (int j = 0; j < source_num; j++) {
                    if (i == j)
                        continue;
                    System.out.println(i + "\t" + j + "\t" + path_len[i][source_start_index + j] + "\t" + paths[i].get(source_start_index + j));
                }
        }

        for(int i = 0; i < source_num; i++){
            Set<String> set = new HashSet<>();
            StringBuffer sb = new StringBuffer();
            Map<Integer, Integer> map = new HashMap<>();
            int temp_index = source_num;
            for (int j = 0; j < source_num; j++) {
                if(i == j)
                    continue;
                for(int k = 1; k < paths[i].get(source_start_index+j).size(); k++){
                    int left = paths[i].get(source_start_index+j).get(k), right = paths[i].get(source_start_index+j).get(k-1);
                    if(left >= source_start_index && left < source_start_index + source_num)
                        left -= source_start_index;
                    else{
                        if(!map.containsKey(left)){
                            map.put(left, temp_index);
                            left = temp_index++;
                        }else
                            left = map.get(left);
                    }
                    if(right >= source_start_index && right < source_start_index + source_num)
                        right -= source_start_index;
                    else{
                        if(!map.containsKey(right)){
                            map.put(right, temp_index);
                            right = temp_index++;
                        }else
                            right = map.get(right);
                    }
                    String str = "link: " + left + " " + right + " ";
                    if(!set.contains(str)){
                        set.add(str);
                        sb.insert(0, str);
                    }
                }
            }
            if(OUTPUT_MATRIX == 6 && i == 0){
                System.out.print("\"");
                System.out.print(sb.toString());
                System.out.println("\", ");
            }
            tree[i] = sb.toString();
        }
        return paths;
    }

    public static int[] getPathWithWeightDijkstra(int[][] graph, int source, ArrayList<ArrayList<Integer>> paths) {
        int numVertices = graph.length;

        int[] dist = new int[numVertices];
        Arrays.fill(dist, INF);
        dist[source] = 0;

        int[] prev = new int[numVertices];
        Arrays.fill(prev, -1);

        boolean[] visited = new boolean[numVertices];

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
        pq.add(new int[]{source, 0});

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];

            if (visited[u]) {
                continue;
            }

            visited[u] = true;

            for (int v = 0; v < numVertices; v++) {
                if (!visited[v] && graph[u][v]!= INF && dist[u] + graph[u][v] < dist[v]) {
                    dist[v] = dist[u] + graph[u][v];
                    prev[v] = u;
                    pq.add(new int[]{v, dist[v]});
                } else if (!visited[v] && graph[u][v] == 0 && dist[u] < dist[v]) {
                    dist[v] = dist[u];
                    prev[v] = u;
                    pq.add(new int[]{v, dist[v]});
                }
            }
        }

        for (int i = 0; i < numVertices; i++) {
            if (i!= source) {
                ArrayList<Integer> path = new ArrayList<>();
                int at = i;
                while (at!= -1) {
                    path.add(at);
                    at = prev[at];
                }
                ArrayList<Integer> reversedPath = new ArrayList<>();
                for (int j = path.size() - 1; j >= 0; j--) {
                    reversedPath.add(path.get(j));
                }
                paths.add(reversedPath);
            } else {
                paths.add(new ArrayList<>());
                paths.get(i).add(source);
            }
        }
        return dist;
    }

    public static int[][] getWeightFakeTopo(int[][] attack_topo, int threshold) {
        int atk_len = attack_topo.length, res_len = atk_len;

        int[][] res = new int[atk_len*3][atk_len*3];
        for(int i = 0; i < res.length; i++)
            Arrays.fill(res[i], INF);
        for(int i = 0; i < atk_len; i++)
            for(int j = 0; j < atk_len; j++)
                res[i][j] = attack_topo[i][j];
        for(int i = 0; i < atk_len; i++){
            int degree = 0;
            ArrayList<Integer> list = new ArrayList<>();
            for(int j = 0; j < res_len; j++)
                if(res[j][i] == 1){
                    degree++;
                    list.add(j);
                }

            if(degree > threshold){
                host_division(res, res_len, i, degree, threshold, list);
                res_len += Math.ceil((float)degree/(threshold-2))-1;
            }else{
                for(int index : list){
                    res[index][i] = 1;
                    res[i][index] = 1;//error
                }
            }
        }
        int[][] fake_topo = new int[res_len][res_len];
        for(int i = 0; i < res_len; i++){
            for(int j = 0; j < res_len; j++)
                fake_topo[i][j] = res[i][j];
            fake_topo[i][i] = 0;
        }

        int[][] fake_topo2 = new int[fake_topo.length][fake_topo.length];

        for(int i = 0; i < fake_topo.length; i++){
            for(int j = 0; j < fake_topo.length; j++){
                fake_topo2[i][j] = fake_topo[i][j];
                if(i == j)
                    continue;
                if(fake_topo2[i][j] == 0)
                    fake_topo2[i][j] = 1;
                else if(fake_topo2[i][j] == 1)
                    fake_topo2[i][j] = 100;
            }
        }

        return fake_topo2;
    }

    public static void host_division(int[][] fake_topo, int fake_max,int index, int degree, int threshold, ArrayList<Integer> list){
        int add_len = (int) (Math.ceil((float)degree/(threshold-2))-1);
        for(int i : list){
            fake_topo[i][index] = INF;
            fake_topo[index][i] = INF;
        }

        fake_topo[index][fake_max] = 0;
        fake_topo[fake_max][index] = 0;
        for(int i = 0; i < add_len-1; i++){
            fake_topo[fake_max+i][fake_max+i+1] = 0;
            fake_topo[fake_max+i+1][fake_max+i] = 0;
        }
        fake_topo[index][fake_max+add_len-1] = 0;
        fake_topo[fake_max+add_len-1][index] = 0;

        int list_index = 0;
        for(int i = 0; i < threshold-2 && list_index < list.size(); i++){
            int ophost = list.get(list_index++);
            fake_topo[ophost][index] = 1;
            fake_topo[index][ophost] = 1;
        }
        for(int i = 0; i < add_len && list_index < list.size(); i++)
            for(int j = 0; j < threshold-2 && list_index < list.size(); j++) {
                int ophost = list.get(list_index++);
                fake_topo[ophost][fake_max+i] = 1;
                fake_topo[fake_max+i][ophost] = 1;
            }
    }

    public static int[][] GetAttackTopo(int[][] topo, ArrayList<ArrayList<Integer>>[] paths) {
        int total_num = topo.length, source_num = paths.length;
        boolean[] used_host = new boolean[total_num];
        boolean[][] used_edge = new boolean[total_num][total_num];
        for(ArrayList<ArrayList<Integer>> node_to_all_path : paths){
            for(int i = 0; i < source_num-1; i++){
                ArrayList<Integer> path = node_to_all_path.get(i);
                for(int j = 0; j < path.size()-1; j++){
                    used_edge[path.get(j)][path.get(j+1)] = true;
                    used_edge[path.get(j+1)][path.get(j)] = true;
                    used_host[path.get(j)] = true;
                }
                used_host[path.get(path.size()-1)] = true;
            }
        }
        int used_host_num = 0;
        for(int i = 0; i < total_num; i++)
            if(used_host[i])
                used_host_num++;

        int[][] res = new int[used_host_num][used_host_num];
        int res_row = 0, res_line;
        for(int i = 0; i < total_num; i++){
            if(!used_host[i])
                continue;
            res_line = 0;
            for(int j = 0; j < total_num; j++){
                if(!used_host[j])
                    continue;
                res[res_row][res_line++] = used_edge[i][j]? topo[i][j] : INF;
            }
            res_row++;
        }
        for(int i = 0; i < used_host_num; i++)
            res[i][i] = 0;

        if(OUTPUT_MATRIX == 2){
            for(int i = 0; i < used_host_num; i++){
                for(int j = 0; j < used_host_num; j++)
                    System.out.print((res[i][j]==INF?"INF":res[i][j]) + "\t");
                System.out.println();
            }
            System.out.println("\ntotal node num\t" + res.length);
        }
        return res;
    }

    public static ArrayList<ArrayList<Integer>>[] fixedRealPath(int source_num, int detection_start_index, String[] tree){
        String path = "ns3_res/ns3_res_exp" + EXPERIMENT_INDEX + "/real/";
        if(CHOOSE_TOPO_INDEX == 1)
            path += "dfn/dfn_res/dfn_";
        else if(CHOOSE_TOPO_INDEX == 2)
            path += "cogentco/cogentco_res/cogentco_";
        else
            path += "deltacom/deltacom_res/deltacom_";
        path += CHOOSE_EDGE_NODE_PRECENT + "_pathes_res.txt";

        ArrayList<ArrayList<Integer>>[] res = new ArrayList[source_num];
        for(int i = 0; i < source_num; i++)
            res[i] = new ArrayList<>();
        try {
            int count = 0;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                if(line.equals(""))
                    continue;
                String[] strs = line.trim().split("\t");
                ArrayList<Integer> in_res = new ArrayList<>();
                for(int j = 3; j < strs.length; j++) {
                    if(strs[j].equals(""))
                        continue;
                    in_res.add(Integer.parseInt(strs[j]));
                }
                String[] strs2 = strs[2].trim().split("\\.");
                in_res.add(detection_start_index + (Integer.parseInt(strs2[3])/2));
                res[count++/(source_num-1)].add(in_res);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("can not read file: " + path + e.getMessage());
        }
        if(OUTPUT_MATRIX == 1) {
            System.out.println("src\tdest\tweight\tpath");
            for (int i = 0; i < source_num; i++)
                for (int j = 0; j < source_num; j++) {
                    if(i == j)
                        continue;
                    if(j >= i)
                        System.out.println(i + "\t" + j + "\t" + (res[i].get(j-1).size()-1) + "\t" + res[i].get(j-1));
                    else
                        System.out.println(i + "\t" + j + "\t" + (res[i].get(j).size()-1) + "\t" + res[i].get(j));
                }
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < source_num; i++)
                for (int j = i+1; j < source_num; j++) {
                    if(res[i].get(j-1).size() != res[j].get(i).size()){
                        list.add(i + "->" + j + "len err");
                        continue;
                    }
                    for(int k = 0; k < res[i].get(j-1).size(); k++)
                        if(res[i].get(j-1).get(k) != res[j].get(i).get(res[i].get(j-1).size()-k-1))
                            list.add(i + "->" + j + " at step " + (k+1));
                }
            if(list.size() > 0)
                System.out.println("Different round-trip paths：" + list);
        }
        for(int i = 0; i < source_num; i++) {
            Set<String> set = new HashSet<>();
            StringBuffer sb = new StringBuffer();
            Map<Integer, Integer> map = new HashMap<>();
            int temp_index = source_num;
            for (int j = 1; j < source_num; j++) {
                for (int k = 1; k < res[i].get(j - 1).size(); k++) {
                    int left = res[i].get(j - 1).get(k), right = res[i].get(j - 1).get(k - 1);
                    if (left >= detection_start_index)
                        left -= detection_start_index;
                    else{
                        if(!map.containsKey(left)){
                            map.put(left, temp_index);
                            left = temp_index++;
                        }else
                            left = map.get(left);
                    }
                    if (right >= detection_start_index)
                        right -= detection_start_index;
                    else{
                        if(!map.containsKey(right)){
                            map.put(right, temp_index);
                            right = temp_index++;
                        }else
                            right = map.get(right);
                    }
                    String str = "link: " + left + " " + right + " ";
                    if (!set.contains(str)) {
                        set.add(str);
                        sb.insert(0, str);
                    }
                }
            }
            if(OUTPUT_MATRIX == 5){
                System.out.println(sb.toString());

            }
            tree[i] = sb.toString();
        }

        return res;
    }

}