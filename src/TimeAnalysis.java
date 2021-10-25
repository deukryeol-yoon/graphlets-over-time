package src;
import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import java.util.List;
import it.unimi.dsi.fastutil.ints.*;

import java.io.*;
import java.lang.Math;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class TimeAnalysis {
    DirectedNodeMap node_map; // Graph
    Random rd;
    int[] budget_array;
    long[] current = new long[DirectedMotifType.values().length]; // graph의 현재 motif 개수
    int count = 0;
    double[] target_motif_distribution; 
    static int UNKNOWN = -1;

    String projectPath = System.getProperty("user.dir");
    String resultFolder;
    String read_path;
    String dataset;
    BufferedWriter filewriter;
    CSVWriter cw;
    CSVWriter mw;
    int num_pow = 2;
    ArrayList<Integer>[] selected_vertex ;
    long[][] graphlets;
    int numNodes;
    int maxDegree;
    boolean flagNodeTracking;
    int[] degreeList;

    public TimeAnalysis(String read_path, String dataset, int numNodes){
        this.node_map = new DirectedNodeMap(numNodes);
        rd = new Random();
        this.read_path = read_path;
        this.dataset = dataset;
        this.graphlets = new long[numNodes][DirectedGraphletType.values().length];
        this.numNodes = numNodes;
    }
    
    
    public void run(){
        String line = null ;
        resultFolder = projectPath + "/result/" + dataset;
        File folder = new File(resultFolder);
        if (!folder.exists()){
            folder.mkdir();
        }

        
        try{
            String path = resultFolder + "/degree.csv";
            File f = new File(path);
            if(!f.exists()){
                System.out.println("Graphlet Tracking Mode is On, but no degree file exists");
                return;
            }
            degreeList = new int[numNodes];

            BufferedReader fr = new BufferedReader(new FileReader(f));
            fr.readLine();
            while((line = fr.readLine()) != null){
                String[] vid_deg = line.replace("\"", "").split(",");
                int vid = Integer.parseInt(vid_deg[0]);
                int deg = Integer.parseInt(vid_deg[1]);
                for(int i = 2 ;;i = i * 2){
                    if (i >= deg){
                        deg = i;
                        break;
                    }
                }
                degreeList[Integer.parseInt(vid_deg[0])] = deg;
            }

            String resultPath = resultFolder + "/time.csv";
            cw = new CSVWriter(new FileWriter(new File(resultPath), false));
            File read_file = new File(read_path);
            // 분석하려고 하는 graph (edge)를 읽어오는 filereader
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            
            line = null;
            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                addEdge(v1, v2);
            }
            filereader.close();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // edge(v1, v2)를 추가하고, 이에 따라서 변경되는 motif distribution의 변화를 함께 분석하는 함수
    public void addEdge(int v1, int v2) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }

        DirectedEdge edge = new DirectedEdge(v1, v2);
        
        String[] writeResult = new String[2];
        if(!node_map.nodes.contains(v1)){
            try{
                writeResult[0] = Integer.toString(count);
                writeResult[1] = Integer.toString(degreeList[v1]);
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        if(!node_map.nodes.contains(v2)){
            try{
                writeResult[0] = Integer.toString(count);
                writeResult[1] = Integer.toString(degreeList[v2]);
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        // graph에 edge 추가
        node_map.addEdge(v1, v2);
        count++;
        
        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            
        }
        /*
        long[] graph_motif = countMotifInstance.countDirectedMotif(node_map);
        long[] motifs = graphlet2motif();
        if(!Arrays.equals(graph_motif, motifs)){
            System.out.println("1. Graph Motif From Count : " + Arrays.toString(graph_motif));
            System.out.println("2. Graph Motif From Graphlet : " + Arrays.toString(motifs));
        }
        */
        

    }
        /*
            String[] header = {     
                    "deg", "time",
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
        */

    public void printResult(int v, int degree){
        String path = resultFolder + "/" + String.format("%04d", degree) + ".csv";
        try{
            CSVWriter cw = new CSVWriter(new FileWriter(new File(path), true));
            long[] graphlet = graphlets[v];
            String[] result = new String[graphlet.length+2];
            float sum = 0;
            for(int i = 0; i < graphlet.length; i++){
                sum += graphlet[i];
            }
            result[0] = Integer.toString(v);
            result[1] = Integer.toString(count);
            for(int i = 2; i < result.length; i++){
                result[i] = Double.toString(graphlet[i-2] / sum * 1.0);
            }
            cw.writeNext(result);
            cw.flush();
            cw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
