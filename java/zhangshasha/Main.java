package zhangshasha;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
	static final int SOURCE_INDEX = 0;//
	static final byte CHOOSE_TOPO_INDEX = 3;//1：Dfn     2：Cogentco      other：Deltacom
	static int CHOOSE_EDGE_NODE_PRECENT = 10;//10, 20, ..., 100
	static int EXPERIMENT_INDEX = 1;
	static final int OUTPUT_MATRIX = 2;//1: tree edit distance  2: Similarity Score
	static final int SIMPLIFY_REAL_TREE = 1;//1 : simplify real topo
	static final int CALCULATE_TOPOFAKER_REPLACE = 1;//1 : use calculate not detection

	//real, fake, real topo, fake slm
	static int RES_TYPE_NUM = 4;

	public static void main(String[] args) throws IOException  {
		main1(args);
		//main2(args);
	}

	public static void main1(String[] args) throws IOException  {
		for(int i = 10; i <= 100; i+=10){
			CHOOSE_EDGE_NODE_PRECENT = i;
			main2(args);
		}
	}

	public static void main2(String[] args) throws IOException {
		String[] treeStrs = getTreeString();
		int host_num = getSourceHostNum();
		Tree[] trees = new Tree[RES_TYPE_NUM];
		for(int i = 0; i < trees.length; i++)
				trees[i] = covertStrToTree(treeStrs[i], host_num);
		if(SIMPLIFY_REAL_TREE == 1)
			deleteMidNode2(trees[trees.length-2].root.children.get(0), trees[trees.length-2]);
		if(CALCULATE_TOPOFAKER_REPLACE == 1)
			deleteMidNode2(trees[trees.length-1].root.children.get(0), trees[trees.length-1]);

		int[] distances = new int[trees.length];
		for(int i = 0; i < trees.length; i++)
				distances[i] = Tree.ZhangShasha(trees[i], trees[trees.length-2]);

		if(OUTPUT_MATRIX == 1) {
			System.out.print(distances[0] + "\t");//real tree edit distance
			if(CALCULATE_TOPOFAKER_REPLACE == 1)
				System.out.println(distances[trees.length - 1] + "\t");//topofaker tree edit distance
			else
				System.out.println(distances[1] + "\t");//topofaker tree edit distance
		}else if(OUTPUT_MATRIX == 2){
			System.out.print(getSS(distances[0], trees[0].total_tree_num, trees[trees.length - 2].total_tree_num) + "\t");
			if(CALCULATE_TOPOFAKER_REPLACE == 1)
				System.out.println(getSS(distances[trees.length - 1], trees[trees.length - 1].total_tree_num, trees[trees.length - 2].total_tree_num) + "\t");
			else
				System.out.println(getSS(distances[1], trees[1].total_tree_num, trees[trees.length - 2].total_tree_num) + "\t");
		}

	}

	public static float getSS(int tree_edit_distance, int fake_tree_node_num, int real_tree_node_num){
		float down = fake_tree_node_num + real_tree_node_num;
		return 1 - tree_edit_distance / down;
	}

	public static void mysout(Node node){
		System.out.println(node.label + " : " + node.children.size());
		if(node.children == null || node.children.size() ==0)
			return;
		for(Node child : node.children)
			mysout(child);
	}

	public static void deleteMidNode(Node node){
		if(node.children == null || node.children.size() ==0)
			return;
		if(node.children.size() > 1)
			for(Node child : node.children)
				deleteMidNode(child);
		//node.children.size() == 1
		if(node.children.get(0).children.size() == 1) {
			node.children = node.children.get(0).children;
			deleteMidNode(node);
		}else
			deleteMidNode(node.children.get(0));
	}

	public static void deleteMidNode2(Node node, Tree tree){
		if(node.children == null || node.children.size() == 0)
			return;
		if(node.children.size() > 1){
			for(Node child : node.children)
				deleteMidNode2(child, tree);
			return;
		}
		//node.children.size() == 1
		//System.out.println("delete one node " + node.label + " child num : " + node.children.size());
		node.label = node.children.get(0).label;
		node.children = node.children.get(0).children;
		tree.total_tree_num--;
		deleteMidNode2(node, tree);
	}

	public static Tree covertStrToTree(String str, int host_num){
		String[] strs = str.split("link: ");
		Map<Integer, Node> map = new HashMap<>();
		//map.put(SOURCE_INDEX, new Node(SOURCE_INDEX+""));
		map.put(SOURCE_INDEX, new Node("-"));

		for(int j = strs.length-1; j > 0; j--) {
			String[] temp = strs[j].trim().split(" ");
			int left = Integer.parseInt(temp[0]), right = Integer.parseInt(temp[1]);

			//Node child = new Node(left >= host_num ? "-" : left+"");
			Node child = new Node("-");
			map.put(left, child);
			Node parent = map.get(right);
			parent.children.add(child);
		}
		return new Tree(map.get(SOURCE_INDEX), strs.length);
	}

	public static String[] getTreeString() {
		String[] res = new String[RES_TYPE_NUM];
		for (int i = 0; i < RES_TYPE_NUM; i++) {
			res[i] = getSingleTreeString(i);
		}
		return res;
	}

	public static String getSingleTreeString(int tree_str_type) {
		String path = "fixed_arr_file/restore_tree_str/";
		if(tree_str_type == 0)
			path += "restore_real/";
		else if(tree_str_type == 1)
			path += "restore_topofaker/";
		else if(tree_str_type == 2)
			path += "real_topo/";
		else if(tree_str_type == 3)
			path += "fake_topo/";

		if(CHOOSE_TOPO_INDEX == 1)
			path += "dfn/";
		else if(CHOOSE_TOPO_INDEX == 2)
			path += "cogentco/";
		else if(CHOOSE_TOPO_INDEX == 3)
			path += "deltacom/";

		path += "str" + EXPERIMENT_INDEX + ".txt";
		String res = null;
		try {
			int count = 0, need_skip_line = CHOOSE_EDGE_NODE_PRECENT / 10 - 1;
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line;
			for(int i = 0; i < need_skip_line; i++){
				line = reader.readLine();
				if(line == null || line.equals(""))
					return null;
			}
			res = reader.readLine();
			reader.close();
		} catch (IOException e) {
			System.err.println("can not read file: " + path + e.getMessage());
		}
		return res;
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
}
