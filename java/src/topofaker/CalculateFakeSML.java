package Chou;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CalculateFakeSML {

    static final byte CHOOSE_TOPO_INDEX = 1;//1：Dfn     2：Cogentco      other：Deltacom
    static int CHOOSE_EDGE_NODE_PRECENT = 10;//10, 20, ..., 100
    static final int EXPERIMENT_INDEX = 1;
    /*
    *1:no_detection_topo  2:all_topo 3:real_shortest_path  4:attack_topo
    *5:fake_topo 6:fake_shortest_paths 7:fake_SLM  8：fake_SLM to python file   9: time
    *10:real topo tree for zhangshasha(output to file, please start code in order of 10 to 100)
    *11:fake slm tree for zhangshasha(output to file, please start code in order of 10 to 100)
    */
    static final int OUTPUT_MATRIX = 1;
    static final int DEGREE_THRESHOLD = 3;
    static final int INF = 99999;

    public static void main(String[] args) {
        main1(args);
        //main2(args);
    }

    public static void main1(String[] args) {
        for(int i = 10; i <= 100; i+= 10){
            CHOOSE_EDGE_NODE_PRECENT = i;
            main2(args);
        }
    }

    public static void main2(String[] args) {
        int[][] no_detection_topo = initTopo();
        int[] detection_node = getDetectionNode();
        int[][] all_topo = topo_add_detection_node(no_detection_topo, detection_node);
        ArrayList<ArrayList<Integer>>[] real_shortest_path = fixedRealPath(detection_node.length, no_detection_topo.length);
        long time_start = System.nanoTime();
        int[][] attack_topo = GetAttackTopo(all_topo, real_shortest_path);
        int sender_start_index = attack_topo.length - detection_node.length;
        int[][] fake_topo = getWeightFakeTopo(attack_topo, DEGREE_THRESHOLD);
        Map<String, Integer> map = new HashMap<>();
        int edge_len = getEdgeNum(fake_topo, map);
        ArrayList<ArrayList<Integer>>[] fake_shortest_paths = WeightedDijkstra(fake_topo, sender_start_index, detection_node.length);
        int[][] fake_SLM = getFakeSLM(fake_shortest_paths, edge_len, map, sender_start_index, detection_node.length);
        long time_end = System.nanoTime();
        if(OUTPUT_MATRIX == 9)
            System.out.println("run time：\t" + (time_end - time_start) + "\tns");
    }

    public static ArrayList<ArrayList<Integer>>[] myFindPaths(int[][] topo, int source_start_index, int source_num) {
        ArrayList<ArrayList<Integer>>[] res = new ArrayList[source_num];
        for(int i = 0; i < source_num; i++){
            res[i] = myFindPath(topo, source_start_index, source_num, source_start_index+i);
        }

        if(OUTPUT_MATRIX == 3) {
            System.out.println("src\tdest\tweight\tpath");
            for (int i = 0; i < source_num; i++)
                for (int j = 0; j < source_num-1; j++) {
                    if(j >= i)
                        System.out.println(i + "\t" + (j+1) + "\t" + (res[i].get(j).size()-1) + "\t" + res[i].get(j));
                    else
                        System.out.println(i + "\t" + j + "\t" + (res[i].get(j).size()-1) + "\t" + res[i].get(j));
                }
        }

        return res;
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

        if(OUTPUT_MATRIX == 2) {
            for(int i = 0; i < rl; i++){
                for(int j = 0; j < rl; j++)
                    System.out.print((res[i][j]==INF?"INF":res[i][j]) + "\t");
                System.out.println();
            }
            System.out.println("\ntotal node num\t" + res.length);
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
            for(int i = 0; i < need_skip_line; i++){
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

        if(OUTPUT_MATRIX == 1) {
            for(int i = 0; i < nodeNum; i++){
                for(int j = 0; j < nodeNum; j++)
                    System.out.print((topo[i][j]==INF?"INF":topo[i][j]) + "\t");
                System.out.println();
            }
            System.out.println("\ntotal node num\t" + nodeNum);
        }

        return topo;
    }

    public static int[][] getFakeSLM(ArrayList<ArrayList<Integer>>[] paths, int edge_len, Map<String, Integer> map, int detection_host_index, int detection_host_num){
        int[][] res = new int[detection_host_num*(detection_host_num-1)*(detection_host_num-2)/2][edge_len];
        int index = 0;
        for(int source = 0; source < detection_host_num; source++){
            for(int small = 0; small < detection_host_num-1; small++){
                if(small == source)
                    continue;
                for(int big = small+1; big < detection_host_num; big++){
                    if(big == source)
                        continue;
                    makeShardPath(res, index++, paths[source].get(detection_host_index+small), paths[source].get(detection_host_index+big), map);
                }
            }
        }

        if(OUTPUT_MATRIX == 7) {
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[i].length; j++) {
                    System.out.print(res[i][j] + "\t");
                }
                System.out.println();
            }
            System.out.println("\ntotal edge num：\t" + edge_len);
            System.out.println("detection host num：\t" + detection_host_num);
            System.out.println("combination num：\t" + res.length);
        }

        if(OUTPUT_MATRIX == 8){
            String path = "fake_delay/fake_delay_exp" + EXPERIMENT_INDEX +"/topofaker/";
            if(CHOOSE_TOPO_INDEX == 1)
                path += "dfn/dfn";
            else if(CHOOSE_TOPO_INDEX == 2)
                path += "cogentco/cogentco";
            else
                path += "deltacom/deltacom";
            path += "_" + CHOOSE_EDGE_NODE_PRECENT + ".py";
            File file = new File(path);
            try {
                if(!file.exists())
                    file.createNewFile();
                FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("\nfake_route_matrix = [\\\n");
                for(int i = 0; i < res.length; i++){
                    bw.write("[");
                    for(int j = 0; j < res[i].length; j++)
                            bw.write(res[i][j] + ", ");
                        bw.write("],\\\n");
                }
                bw.write("]\n\n");
                bw.write("faketopo_edges_len = " + edge_len + "\n");
                bw.write("probe_nodes_len = " + detection_host_num + "\n\n");

                bw.write("start= time.time()\n\n");
                bw.write("WholeObfuscatorModel = Model(\"WholeObfuscator\")\n" +
                        "Delay=[WholeObfuscatorModel.add_var(name=\"d'({})\".format(i), var_type=INTEGER) for i in range(faketopo_edges_len)]\n" +
                        "fake_covdelay_list=[WholeObfuscatorModel.add_var(name=\"fcl'({})\".format(i), var_type=INTEGER) for i in range(int(probe_nodes_len*(probe_nodes_len-2)*(probe_nodes_len-1)/2))]\n" +
                        "WholeObfuscatorModel.objective = minimize(xsum(fake_covdelay_list[i] for i in range(int(probe_nodes_len*(probe_nodes_len-2)*(probe_nodes_len-1)/2))))\n" +
                        "for i in range(len(real_covdelay_list)):\n" +
                        "    WholeObfuscatorModel += real_covdelay_list[i] <= fake_covdelay_list[i]\t\n" +
                        "for i in range(len(real_covdelay_list)):\n" +
                        "    WholeObfuscatorModel += xsum(fake_route_matrix[i][j]*Delay[j] for j in range(faketopo_edges_len)) == fake_covdelay_list[i]\n" +
                        "for i in range(faketopo_edges_len):\n" +
                        "    WholeObfuscatorModel += 5 <= Delay[i]\n" +
                        "WholeObfuscatorModel.optimize(max_seconds=1000)\n" +
                        "\n" +
                        "end = time.time()\n" +
                        "\n" +
                        "print(\"\\n\")\n" +
                        "print(\"fake_covdelay_list\")\n" +
                        "for var in fake_covdelay_list:\n" +
                        "    print(var.x, end=', ')\n" +
                        "print()\n" +
                        "print()\n" +
                        "print(int((end-start)*1000000000))\n" +
                        "print()");
                bw.flush();
                bw.close();
                fw.close();
            }catch (Exception e){
                System.out.println("err when write SLM to file");
            }
        }

        return res;
    }

    public static void makeShardPath(int[][] slm, int row, ArrayList<Integer> path1, ArrayList<Integer> path2, Map<String, Integer> map){
        int pre_host = path1.get(0);
        int min_size = Math.min(path1.size(), path2.size());
        for(int i = 1; i < min_size; i++){
            int host1 = path1.get(i), host2 = path2.get(i);
            if(host1 != host2)
                return;
            int line = 0;
            if(host1 < pre_host)
                line = map.get(host1+"-"+pre_host);
            else
                line = map.get(pre_host+"-"+host1);
            slm[row][line] = 1;
            pre_host = host1;
        }
    }

    public static int getEdgeNum(int[][] topo, Map<String, Integer> map){
        int res = 0;
        for(int i = 0; i < topo.length; i++)
            for(int j = i+1; j < topo.length; j++){
                if(topo[i][j] == INF)
                    continue;
                map.put(i+"-"+j, res++);
            }
        return res;
    }

    public static ArrayList<ArrayList<Integer>>[] WeightedDijkstra(int[][] topo, int source_start_index, int source_num){
        int path_len[][] = new int[topo.length][];
        ArrayList<ArrayList<Integer>>[] paths = new ArrayList[source_num];

        for(int i = 0; i < source_num; i++){
            paths[i] = new ArrayList<>();
            path_len[i] = getPathWithWeightDijkstra(topo, source_start_index+i, paths[i]);
        }

        if(OUTPUT_MATRIX == 6) {
            System.out.println("src\tdest\tweight\tpaht");
            for (int i = 0; i < source_num; i++)
                for (int j = 0; j < source_num; j++) {
                    if (i == j)
                        continue;
                    System.out.println(i + "\t" + j + "\t" + path_len[i][source_start_index + j] + "\t" + paths[i].get(source_start_index + j));
                }
        }

        if(OUTPUT_MATRIX == 11){
            Set<String> set = new HashSet<>();
            StringBuffer sb = new StringBuffer();
            for (int j = 1; j < source_num; j++) {
                for(int k = 1; k < paths[0].get(source_start_index+j).size(); k++){
                    int left = paths[0].get(source_start_index+j).get(k), right = paths[0].get(source_start_index+j).get(k-1);
                    if(left >= source_start_index && left < source_start_index + source_num)
                        left -= source_start_index;
                    else if( left < source_start_index)
                        left += source_num;
                    if(right >= source_start_index && right < source_start_index + source_num)
                        right -= source_start_index;
                    else if( right < source_start_index)
                        right += source_num;
                    String str = "link: " + left + " " + right + " ";
                    if(!set.contains(str)){
                        set.add(str);
                        sb.insert(0, str);
                    }
                }
            }
            //System.out.print(sb.toString());
            String write_path = "fixed_arr_file/restore_tree_str/fake_topo/";
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

        if(OUTPUT_MATRIX == 5){
            for(int i = 0; i < res_len; i++){
                for(int j = 0; j < res_len; j++){
                    if(fake_topo[i][j] == INF)
                        System.out.print("INF\t");
                    else
                        System.out.print(fake_topo[i][j] + "\t");
                }
                System.out.println();
            }
            System.out.println("\ntotal node num\t" + fake_topo.length);
        }
        return fake_topo;
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
        for(int i = 0; i < threshold-2 && list_index < list.size(); i++){//分配原本节点
            int ophost = list.get(list_index++);
            fake_topo[ophost][index] = 1;
            fake_topo[index][ophost] = 1;
        }
        for(int i = 0; i < add_len && list_index < list.size(); i++)
            for(int j = 0; j < threshold-2 && list_index < list.size(); j++) {//分配虚拟节点
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

        if(OUTPUT_MATRIX == 4){
            for(int i = 0; i < used_host_num; i++){
                for(int j = 0; j < used_host_num; j++)
                    System.out.print((res[i][j]==INF?"INF":res[i][j]) + "\t");
                System.out.println();
            }
            System.out.println("\ntotal node num\t" + res.length);
        }
        return res;
    }

    public static ArrayList<ArrayList<Integer>>[] fixedRealPath(int source_num, int detection_start_index){
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
        if(OUTPUT_MATRIX == 3) {
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
        if(OUTPUT_MATRIX == 10){
            Set<String> set = new HashSet<>();
            StringBuffer sb = new StringBuffer();
            for (int j = 1; j < source_num; j++) {
                for(int k = 1; k < res[0].get(j-1).size(); k++){
                    int left = res[0].get(j-1).get(k), right = res[0].get(j-1).get(k-1);
                    if(left >= detection_start_index)
                        left -= detection_start_index;
                    else
                        left += source_num;
                    if(right >= detection_start_index)
                        right -= detection_start_index;
                    else
                        right += source_num;
                    String str = "link: " + left + " " + right + " ";
                    if(!set.contains(str)){
                        set.add(str);
                        sb.insert(0, str);
                    }
                }
            }
            String write_path = "fixed_arr_file/restore_tree_str/real_topo/";
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

        return res;
    }

}

class PathSave{
    String path;
    Chou.PathSave next;
    PathSave(String path){
        this.path = path;
    }
}

class PathCount{
    int count;
    String str;
    PathCount(int count, String str){
        this.count = count;
        this.str = str;
    }
}
