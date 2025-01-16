package Chou;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CalculateAverageAddDelay {
    static final int CHOOSE_TOPO_INDEX = 2;
    static final int OUTPUT_VALUE = 2; // 1 max 2 avg
    static int CHOOSE_EDGE_NODE_PRECENT = 20;
    static final int EXPERIMENT_INDEX = 2;

    public static void main(String[] args) {
        for(int i = 10; i <= 100; i+= 10){
            CHOOSE_EDGE_NODE_PRECENT = i;
            getCovDelay2();
        }
    }

    public static void getCovDelay2(){
        String path = "fake_delay/fake_delay_exp" + EXPERIMENT_INDEX + "/topofaker/";
        if(CHOOSE_TOPO_INDEX == 1)
            path += "dfn/sort_res/dfn_";
        else if(CHOOSE_TOPO_INDEX == 2)
            path += "cogentco/sort_res/cogentco_";
        else
            path += "deltacom/sort_res/deltacom_";
        path += CHOOSE_EDGE_NODE_PRECENT + ".txt";
        try {
            File file = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int sum = 0, count = 0, max = 0;
            while ((line = reader.readLine()) != null) {
                if(line.trim().equals(""))
                    continue;
                int temp = Integer.parseInt(line.trim().split("\\.")[0]);
                sum += temp;
                max = Math.max(max, temp);
                count++;
            }
            reader.close();
            if(OUTPUT_VALUE == 1)
                System.out.println(max);
            else if(OUTPUT_VALUE == 2)
                System.out.println((float)sum / count);
        } catch (IOException e) {
            System.err.println("can not read file: " + path + e.getMessage());
        }
    }
}
