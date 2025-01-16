package Chou;

import java.util.Random;

public class GetRandomEdgeNode {
    static final boolean RAND_DETECTION_NODE = true;
    static long RAND_DETECTION_NODE_SEED = 1;
    static float CHOOSE_EDGE_NODE_PRECENT = 10;//10, 20, ..., 100

    static final byte CHOOSE_TOPO_INDEX = 2;//1：Dfn     2：Cogentco      other：Deltacom

    public static void main(String[] args) {
        int res[][] = new int[10][];
        for(int i = 10; i <= 100; i+=10){
            CHOOSE_EDGE_NODE_PRECENT = i;
            RAND_DETECTION_NODE_SEED = (int)CHOOSE_EDGE_NODE_PRECENT;
            res[i/10-1] = getRandomDetectionNode(null);
        }
        System.out.println("choose edge node for detection（10%~100%对应0-9）：\n");
        System.out.println("int* rand_edges[10];");
        for(int row = 0; row < 10; row++){
            System.out.print("rand_edges[" + row + "] = new int[" + res[row].length + "]{");
            for(int i = 0; i < res[row].length-1; i++)
                System.out.print(res[row][i] + ", ");
            System.out.println(res[row][res[row].length-1] + "};");
        }
        System.out.print("int rand_edges_len[] = {");
        for(int row = 0; row < 9; row++)
            System.out.print(res[row].length + ", ");
        System.out.println(res[9].length + "};\n\n");


        for(int row = 0; row < 10; row++){
            for(int i = 0; i < res[row].length-1; i++)
                System.out.print(res[row][i] + ", ");
            System.out.println(res[row][res[row].length-1]);
        }
    }


    public static int[] getRandomDetectionNode(int[][] topo){
        int[][] edge_nodes;
        int total_edge_num;
        if(CHOOSE_TOPO_INDEX == 1){
            total_edge_num = 18;
            edge_nodes = new int[][]{{8, 9, 12, 13, 15, 26, 29},
                    {18, 6, 16, 11, 0, 7, 2, 5, 20, 21, 4}
            };
        }else if(CHOOSE_TOPO_INDEX == 2){
            total_edge_num = 60;
            edge_nodes = new int[][]{{17, 24, 31, 33, 34, 81, 85, 90, 93, 125, 126, 136, 145, 151, 159, 161, 170, 178, 180, 182, 190, 192},
                    {66, 71, 27, 40, 54, 70, 43, 9, 18, 35, 36, 5, 39, 72, 25, 15, 11, 2, 52, 44, 21, 0, 20, 65, 57, 22, 10, 53, 56, 50, 58, 3, 60, 23, 29, 46, 59, 47}
            };
        }else{
            total_edge_num = 34;
            edge_nodes = new int[][]{{39, 44, 78, 79},
                    {5, 22, 41, 42, 12, 33, 32, 18, 2, 35, 52, 46, 21, 48, 9, 17, 27, 51, 28, 45, 38, 7, 29, 14, 1, 34, 15, 26, 23, 40}
            };
        }
        if(CHOOSE_EDGE_NODE_PRECENT >= 100)
            return mergeArray(edge_nodes[0], edge_nodes[1]);

        int num = (int)Math.ceil((total_edge_num-3) * CHOOSE_EDGE_NODE_PRECENT / 100)+3;//为保证实验节点个数，先剔除3个后进行选择
        if(num < edge_nodes[0].length)
            return getRandomArr(edge_nodes[0], num);
        else if(num == edge_nodes[0].length)
            return edge_nodes[0];
        else{
            int[] arr2 = getRandomArr(edge_nodes[1], num - edge_nodes[0].length);
            return mergeArray(edge_nodes[0], arr2);
        }
    }

    public static int[] getRandomArr(int[] arr, int num){
        int[] res = new int[num];

        Random rand;
        if(RAND_DETECTION_NODE)
            rand = new Random();
        else
            rand = new Random(RAND_DETECTION_NODE_SEED);

        int left_node = num;
        boolean[] choosed = new boolean[num];
        for(int i = 0; i < num; i++){
            int rand_num = rand.nextInt(left_node);
            for(int j = 0, k = 0; j < arr.length && k < left_node; j++){
                if(!choosed[j] && k == rand_num) {
                    res[i] = arr[j];
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
}
