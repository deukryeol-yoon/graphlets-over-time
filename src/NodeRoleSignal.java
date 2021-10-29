import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import it.unimi.dsi.fastutil.ints.*;
import java.io.*;
import java.lang.Math;
import com.opencsv.CSVWriter;

public class NodeRoleSignal {
    DirectedNodeMap node_map; 
    long[] current = new long[GraphletType.values().length]; 
    int count = 0;
    String projectPath = System.getProperty("user.dir");
    String resultFolder;
    String read_path;
    String dataset;
    long[][] graphlets;
    int numNodes;
    int maxDegree;
    double[] centralityList;
    String centrality;
    static double[] boundary_centrality;
    public NodeRoleSignal(String read_path, String dataset, int numNodes, int maxDegree, String centrality){
        this.node_map = new DirectedNodeMap(numNodes);
        this.read_path = read_path;
        this.dataset = dataset;
        this.graphlets = new long[numNodes][NodeRoleType.values().length];
        this.numNodes = numNodes;
        this.maxDegree = maxDegree;
        this.centrality = centrality;
    }
    
    
    public void run(){
        String line = null ;
        resultFolder = projectPath + "/../result/node-analysis/signal-of-role/" + dataset;
        File folder = new File(resultFolder);
        if (!folder.exists()){
            folder.mkdir();
        }

        
        try{

            String centralityPath = System.getProperty("user.dir") + "/../data/centrality/" + dataset + "-" + centrality + ".csv";
            File f = new File(centralityPath);
            if(!f.exists()){
                System.out.println("No Centrality file exists");
                return;
            }
            centralityList = new double[numNodes];

            BufferedReader fr = new BufferedReader(new FileReader(f));
            int[] boundary_idx = new int[]{(int)Math.round(numNodes * 0.5), (int)Math.round(numNodes * 0.7), (int)Math.round(numNodes * 0.9), (int)Math.round(numNodes * 0.95), (int)Math.round(numNodes * 0.99)};
            
            ArrayList<Double> centrality_list = new ArrayList<Double>();
            while((line = fr.readLine()) != null){
                String[] vid_centrality = line.replace("\"", "").split(",");
                int vid = Integer.parseInt(vid_centrality[0]);
                double centrality = Double.parseDouble(vid_centrality[1]);
                centrality_list.add(centrality);
                centralityList[vid] = centrality;
            }

            Collections.sort(centrality_list);
            boundary_centrality = new double[]{centrality_list.get(boundary_idx[0]), centrality_list.get(boundary_idx[1]), centrality_list.get(boundary_idx[2]), centrality_list.get(boundary_idx[3]), centrality_list.get(boundary_idx[4])};
            System.out.println(Arrays.toString(boundary_centrality));
            String directory = resultFolder + "/" + centrality + "/";
            folder = new File(directory);
            if (!folder.exists()){
                folder.mkdir();
            }
            for(int i = 0 ; i <= boundary_centrality.length; i++){
                String path = directory + String.format("%04d", i) + ".csv";
                CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                String[] header = {
                    "m1_1", "m1_2",
                    "m2_1", "m2_2", "m2_3",
                    "m3_1", "m3_2", "m3_3",
                    "m4_1", "m4_2",
                    "m5_1", "m5_2", "m5_3",
                    "m6_1", "m6_2",
                    "m7_1", "m7_2", "m7_3",
                    "m8_1", "m8_2",
                    "m9_1",
                    "m10_1", "m10_2", "m10_3",
                    "m11_1", "m11_2",
                    "m12_1", "m12_2", "m12_3",
                    "m13_1"
                    };
                cw.writeNext(header);
                cw.flush();
                cw.close();
            }
            File read_file = new File(read_path);
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            
            line = null;
            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                addEdge(v1, v2);
            }
            for(int i = 0; i < boundary_centrality.length+1; i++){
                CSVWriter cw = new CSVWriter(new FileWriter(directory + String.format("%04d", i) + ".csv", true));
                for(int j = 2; j <= 8; j *= 2){
                    long key = (((long)i) <<32 | (j) & 0xffffffffL);
                    if(!node_map.graphlet_map.contains(key)){
                        continue;
                    }
                    long[] graphlet = node_map.graphlet_map.get(key);
                    
                    String[] outStrings = new String[NodeRoleType.values().length];
                    double sum = 0.0;
                    for(int k = 0; k < NodeRoleType.values().length; k++){
                        sum += graphlet[k];
                    }
                    for(int k = 0; k < NodeRoleType.values().length; k++){
                        outStrings[k] = Double.toString(graphlet[k]/sum);
                    }
                    cw.writeNext(outStrings);
                    cw.flush();
                }
                if(i > maxDegree){
                    cw.close();
                    break;
                }
            }


        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void addEdge(int v1, int v2) {

        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }
        node_map.addEdge(v1, v2);

        count++;
        IntOpenHashSet visited = new IntOpenHashSet();
        
        for(int v3 : node_map.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited.add(v3);
            update(v1, v2, v3);
        }
        for (int v3 : node_map.getNeighbors(v2)){
            if (visited.contains(v3)) continue;
            if (v1 == v3) continue;
            update(v1, v2, v3);
        }
        int degree2 = node_map.getInDegree(v2);
        long[] graphlet ;
        if(degree2 == 2 || degree2 == 4 || degree2 == 8){
            double maxCentralityNode2 = centralityList[v2];
            int class_id = boundary_centrality.length;
            for(int i = boundary_centrality.length-1; i >= 0 ; i--){
                if(maxCentralityNode2 <= boundary_centrality[i]){
                    class_id = i;
                }
            }
            Long key = (((long)class_id) << 32) | (degree2 & 0xffffffffL);
            if(!node_map.graphlet_map.contains(key)){
                graphlet = new long[NodeRoleType.values().length+1];
            }
            else graphlet = node_map.graphlet_map.get(key);
    
            for(int i = 0; i < NodeRoleType.values().length; i++){
                graphlet[i] += graphlets[v2][i];
            }
            graphlet[graphlet.length-1]++;
            node_map.graphlet_map.put(key, graphlet);
        }

        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            
        }

        
    }
    public void update(int v1, int v2, int v3){
        int V1IN = 0, V2IN = 0, V3IN = 0, V1OUT = 0, V2OUT = 0, V3OUT = 0;
        IntSet V1OUTSET = node_map.getOutNeighbors(v1);
        IntSet V2OUTSET = node_map.getOutNeighbors(v2);
        IntSet V3OUTSET = node_map.getOutNeighbors(v3);

        if (V2OUTSET.contains(v1)){
            V2OUT++;
            V1IN++;
        }
        if (V1OUTSET.contains(v3)){
            V1OUT++;
            V3IN++;
        }
        if (V3OUTSET.contains(v1)){
            V3OUT++;
            V1IN++;
        }
        if (V2OUTSET.contains(v3)){
            V2OUT++;
            V3IN++;
        }
        if (V3OUTSET.contains(v2)){
            V3OUT++;
            V2IN++;
        }
        if(V1IN + V1OUT == 0 || V2IN + V2OUT == 0 || V3IN + V3OUT == 0){
            V1OUT++;
            V2IN++;
            int numEdges = (V1IN + V2IN + V3IN + V1OUT + V2OUT + V3OUT)/2;
            int maxInDegree = Math.max(Math.max(V1IN, V2IN), V3IN);
            int maxOutDegree = Math.max(Math.max(V1OUT, V2OUT), V3OUT);
            if (numEdges == 3){
                if(maxInDegree == 2){
                    //m7
                    graphlets[v2][NodeRoleType.m7_2.ordinal()]++;
                    graphlets[v1][NodeRoleType.m7_3.ordinal()]++;
                    graphlets[v3][NodeRoleType.m7_1.ordinal()]++;
                }
                else {
                    //m3
                    graphlets[v1][NodeRoleType.m3_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m3_3.ordinal()]++;
                    graphlets[v3][NodeRoleType.m3_1.ordinal()]++;
                }
            }
            else if (maxOutDegree == 2){
                graphlets[v1][NodeRoleType.m1_2.ordinal()]++;
                graphlets[v2][NodeRoleType.m1_1.ordinal()]++;
                graphlets[v3][NodeRoleType.m1_1.ordinal()]++;
            }
            else if (maxInDegree == 2){
                graphlets[v2][NodeRoleType.m4_2.ordinal()]++;
                graphlets[v1][NodeRoleType.m4_1.ordinal()]++;
                graphlets[v3][NodeRoleType.m4_1.ordinal()]++;
            }
            else {
                //m2
                if (V1IN + V1OUT == 2){
                    graphlets[v1][NodeRoleType.m2_2.ordinal()]++;
                    graphlets[v3][NodeRoleType.m2_1.ordinal()]++;
                    graphlets[v2][NodeRoleType.m2_3.ordinal()]++;
                }
                else if (V2IN + V2OUT == 2){
                    graphlets[v2][NodeRoleType.m2_2.ordinal()]++;
                    graphlets[v1][NodeRoleType.m2_1.ordinal()]++;
                    graphlets[v3][NodeRoleType.m2_3.ordinal()]++;
                }
            }
            return;
        }

        int numEdges = (V1IN + V2IN + V3IN + V1OUT + V2OUT + V3OUT)/2;
        int maxDegree = Math.max(Math.max(V1IN + V1OUT, V2IN + V2OUT), V3IN+V3OUT);
        int minInDegree = Math.min(Math.min(V1IN, V2IN), V3IN);
        int maxInDegree = Math.max(Math.max(V1IN, V2IN), V3IN);
        int minOutDegree = Math.min(Math.min(V1OUT, V2OUT), V3OUT);
        int maxOutDegree = Math.max(Math.max(V1OUT, V2OUT), V3OUT);

        
        if (numEdges == 5){
            //m12
            graphlets[v1][NodeRoleType.m12_3.ordinal()]--;
            graphlets[v2][NodeRoleType.m12_1.ordinal()]--;
            graphlets[v3][NodeRoleType.m12_2.ordinal()]--;

            graphlets[v1][NodeRoleType.m13_1.ordinal()]++;
            graphlets[v2][NodeRoleType.m13_1.ordinal()]++;
            graphlets[v3][NodeRoleType.m13_1.ordinal()]++;
        } 
        else if (numEdges == 4){
            //m8
            if (maxDegree == 4) {
                graphlets[v1][NodeRoleType.m8_1.ordinal()]--;
                graphlets[v1][NodeRoleType.m12_1.ordinal()]++;

                graphlets[v2][NodeRoleType.m8_1.ordinal()]--;
                graphlets[v2][NodeRoleType.m12_3.ordinal()]++;

                graphlets[v3][NodeRoleType.m8_2.ordinal()]--;
                graphlets[v3][NodeRoleType.m12_2.ordinal()]++;
            }
            else if (minInDegree == 0) {
                // m11
                graphlets[v1][NodeRoleType.m11_2.ordinal()]--;
                graphlets[v1][NodeRoleType.m12_2.ordinal()]++;
                
                graphlets[v2][NodeRoleType.m11_1.ordinal()]--;
                graphlets[v2][NodeRoleType.m12_1.ordinal()]++;

                graphlets[v3][NodeRoleType.m11_2.ordinal()]--;
                graphlets[v3][NodeRoleType.m12_3.ordinal()]++;
            }
            else if (minOutDegree == 0) {
                // m6
                graphlets[v1][NodeRoleType.m6_2.ordinal()]--;
                graphlets[v1][NodeRoleType.m12_3.ordinal()]++;

                graphlets[v2][NodeRoleType.m6_1.ordinal()]--;
                graphlets[v2][NodeRoleType.m12_2.ordinal()]++; 

                graphlets[v3][NodeRoleType.m6_1.ordinal()]--;
                graphlets[v3][NodeRoleType.m12_1.ordinal()]++;
            }
            else {
                // m10
                if((V1IN + V1OUT) == 2){
                    graphlets[v1][NodeRoleType.m10_1.ordinal()]--;
                    graphlets[v1][NodeRoleType.m12_1.ordinal()]++;

                    graphlets[v2][NodeRoleType.m10_2.ordinal()]--;
                    graphlets[v2][NodeRoleType.m12_2.ordinal()]++;

                    graphlets[v3][NodeRoleType.m10_3.ordinal()]--;
                    graphlets[v3][NodeRoleType.m12_3.ordinal()]++;
                }
                else {
                    graphlets[v1][NodeRoleType.m10_3.ordinal()]--;
                    graphlets[v1][NodeRoleType.m12_2.ordinal()]++;

                    graphlets[v2][NodeRoleType.m10_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m12_3.ordinal()]++;

                    graphlets[v3][NodeRoleType.m10_2.ordinal()]--;
                    graphlets[v3][NodeRoleType.m12_1.ordinal()]++;
                }

            }
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) {
                    // m7
                    if (V1IN == 2){
                        graphlets[v1][NodeRoleType.m7_2.ordinal()]--;
                        graphlets[v1][NodeRoleType.m8_2.ordinal()]++;

                        graphlets[v2][NodeRoleType.m7_3.ordinal()]--;
                        graphlets[v2][NodeRoleType.m8_1.ordinal()]++;

                        graphlets[v3][NodeRoleType.m7_1.ordinal()]--;
                        graphlets[v3][NodeRoleType.m8_1.ordinal()]++;
                    }
                    else if (V2IN == 1){
                        graphlets[v1][NodeRoleType.m7_3.ordinal()]--;
                        graphlets[v1][NodeRoleType.m11_1.ordinal()]++;

                        graphlets[v2][NodeRoleType.m7_1.ordinal()]--;
                        graphlets[v2][NodeRoleType.m11_2.ordinal()]++;

                        graphlets[v3][NodeRoleType.m7_2.ordinal()]--;
                        graphlets[v3][NodeRoleType.m11_2.ordinal()]++;
                    }
                    else {
                        graphlets[v1][NodeRoleType.m7_1.ordinal()]--;
                        graphlets[v1][NodeRoleType.m10_2.ordinal()]++;

                        graphlets[v2][NodeRoleType.m7_3.ordinal()]--;
                        graphlets[v2][NodeRoleType.m10_1.ordinal()]++;

                        graphlets[v3][NodeRoleType.m7_2.ordinal()]--;
                        graphlets[v3][NodeRoleType.m10_3.ordinal()]++;
                    }
                }
                else {
                    //m9
                    graphlets[v1][NodeRoleType.m9_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m9_1.ordinal()]--;
                    graphlets[v3][NodeRoleType.m9_1.ordinal()]--;

                    graphlets[v1][NodeRoleType.m10_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m10_3.ordinal()]++;
                    graphlets[v3][NodeRoleType.m10_1.ordinal()]++;
                    
                }
            }
            else {
                if (maxInDegree == 2) {
                    //m5
                    if (V1IN == 1 ){
                        graphlets[v1][NodeRoleType.m5_1.ordinal()]--;
                        graphlets[v2][NodeRoleType.m5_2.ordinal()]--;
                        graphlets[v3][NodeRoleType.m5_3.ordinal()]--;

                        graphlets[v1][NodeRoleType.m6_1.ordinal()]++;
                        graphlets[v2][NodeRoleType.m6_1.ordinal()]++;
                        graphlets[v3][NodeRoleType.m6_2.ordinal()]++;
                    }
                    else{
                        if (V2OUT == 2){
                            graphlets[v1][NodeRoleType.m5_3.ordinal()]--;
                            graphlets[v2][NodeRoleType.m5_2.ordinal()]--;
                            graphlets[v3][NodeRoleType.m5_1.ordinal()]--;
    
                            graphlets[v1][NodeRoleType.m10_3.ordinal()]++;
                            graphlets[v2][NodeRoleType.m10_2.ordinal()]++;
                            graphlets[v3][NodeRoleType.m10_1.ordinal()]++;
                        }
                        else {
                            graphlets[v1][NodeRoleType.m5_3.ordinal()]--;
                            graphlets[v2][NodeRoleType.m5_1.ordinal()]--;
                            graphlets[v3][NodeRoleType.m5_2.ordinal()]--;
    
                            graphlets[v1][NodeRoleType.m11_2.ordinal()]++;
                            graphlets[v2][NodeRoleType.m11_2.ordinal()]++;
                            graphlets[v3][NodeRoleType.m11_1.ordinal()]++;
                        }
                    }
                }
                else {
                    //m3
                    if (V1OUT == 1){
                        graphlets[v1][NodeRoleType.m3_1.ordinal()]--;
                        graphlets[v2][NodeRoleType.m3_3.ordinal()]--;
                        graphlets[v3][NodeRoleType.m3_2.ordinal()]--;

                        graphlets[v1][NodeRoleType.m6_1.ordinal()]++;
                        graphlets[v2][NodeRoleType.m6_2.ordinal()]++;
                        graphlets[v3][NodeRoleType.m6_1.ordinal()]++;
                    }
                    else {
                        if (V2OUT == 2){
                            graphlets[v1][NodeRoleType.m3_3.ordinal()]--;
                            graphlets[v2][NodeRoleType.m3_2.ordinal()]--;
                            graphlets[v3][NodeRoleType.m3_1.ordinal()]--;
    
                            graphlets[v1][NodeRoleType.m8_1.ordinal()]++;
                            graphlets[v2][NodeRoleType.m8_2.ordinal()]++;
                            graphlets[v3][NodeRoleType.m8_1.ordinal()]++;
                        }
                        else {
                            graphlets[v1][NodeRoleType.m3_3.ordinal()]--;
                            graphlets[v2][NodeRoleType.m3_1.ordinal()]--;
                            graphlets[v3][NodeRoleType.m3_2.ordinal()]--;
    
                            graphlets[v1][NodeRoleType.m10_1.ordinal()]++;
                            graphlets[v2][NodeRoleType.m10_3.ordinal()]++;
                            graphlets[v3][NodeRoleType.m10_2.ordinal()]++;
                        }
                    }
                }
            }
        }
        else {
            if (maxOutDegree == 2) {
                //m1
                if (V2OUT == 2){
                    graphlets[v1][NodeRoleType.m1_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m1_2.ordinal()]--;
                    graphlets[v3][NodeRoleType.m1_1.ordinal()]--;

                    graphlets[v1][NodeRoleType.m3_1.ordinal()]++;
                    graphlets[v2][NodeRoleType.m3_2.ordinal()]++;
                    graphlets[v3][NodeRoleType.m3_3.ordinal()]++;
                }
                else {
                    graphlets[v1][NodeRoleType.m1_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m1_1.ordinal()]--;
                    graphlets[v3][NodeRoleType.m1_2.ordinal()]--;

                    graphlets[v1][NodeRoleType.m5_1.ordinal()]++;
                    graphlets[v2][NodeRoleType.m5_3.ordinal()]++;
                    graphlets[v3][NodeRoleType.m5_2.ordinal()]++;
                }
            }
            else if (maxInDegree == 2) {
                //m4
                if (V1IN == 2){
                    graphlets[v1][NodeRoleType.m4_2.ordinal()]--;
                    graphlets[v2][NodeRoleType.m4_1.ordinal()]--;
                    graphlets[v3][NodeRoleType.m4_1.ordinal()]--;

                    graphlets[v1][NodeRoleType.m7_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m7_1.ordinal()]++;
                    graphlets[v3][NodeRoleType.m7_3.ordinal()]++;
                }
                else {
                    graphlets[v1][NodeRoleType.m4_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m4_1.ordinal()]--;
                    graphlets[v3][NodeRoleType.m4_2.ordinal()]--;

                    graphlets[v1][NodeRoleType.m5_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m5_1.ordinal()]++;
                    graphlets[v3][NodeRoleType.m5_3.ordinal()]++;
                }
            }
            else {
                //m2
                if (V1IN+V1OUT == 2){
                    graphlets[v1][NodeRoleType.m2_2.ordinal()]--;
                    graphlets[v2][NodeRoleType.m2_1.ordinal()]--;
                    graphlets[v3][NodeRoleType.m2_3.ordinal()]--;

                    graphlets[v1][NodeRoleType.m3_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m3_1.ordinal()]++;
                    graphlets[v3][NodeRoleType.m3_3.ordinal()]++;
                }
                else if (V1OUT == 1){
                    graphlets[v1][NodeRoleType.m2_1.ordinal()]--;
                    graphlets[v2][NodeRoleType.m2_3.ordinal()]--;
                    graphlets[v3][NodeRoleType.m2_2.ordinal()]--;

                    graphlets[v1][NodeRoleType.m5_2.ordinal()]++;
                    graphlets[v2][NodeRoleType.m5_3.ordinal()]++;
                    graphlets[v3][NodeRoleType.m5_1.ordinal()]++;
                }
                else {
                    if (V2IN == 0){
                        graphlets[v1][NodeRoleType.m2_3.ordinal()]--;
                        graphlets[v2][NodeRoleType.m2_1.ordinal()]--;
                        graphlets[v3][NodeRoleType.m2_2.ordinal()]--;
    
                        graphlets[v1][NodeRoleType.m9_1.ordinal()]++;
                        graphlets[v2][NodeRoleType.m9_1.ordinal()]++;
                        graphlets[v3][NodeRoleType.m9_1.ordinal()]++;
                    }
                    else {
                        graphlets[v1][NodeRoleType.m2_3.ordinal()]--;
                        graphlets[v2][NodeRoleType.m2_2.ordinal()]--;
                        graphlets[v3][NodeRoleType.m2_1.ordinal()]--;
    
                        graphlets[v1][NodeRoleType.m7_1.ordinal()]++;
                        graphlets[v2][NodeRoleType.m7_2.ordinal()]++;
                        graphlets[v3][NodeRoleType.m7_3.ordinal()]++;
                    }
                }
                    
            }
        }
    }
    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }
}
