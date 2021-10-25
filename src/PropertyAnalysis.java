package src;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.Arrays;
import java.util.List;
import it.unimi.dsi.fastutil.ints.*;

import java.io.*;
import java.lang.Math;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class PropertyAnalysis {
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
    int[] degreeList;
    String property;

    public PropertyAnalysis(String read_path, String dataset, int numNodes, String property){
        this.node_map = new DirectedNodeMap(numNodes);
        rd = new Random();
        this.read_path = read_path;
        this.dataset = dataset;
        this.graphlets = new long[numNodes][DirectedGraphletType.values().length];
        this.property = property;
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
                int deg = Integer.parseInt(vid_deg[1]);
                for(int i = 2 ;;i = i * 2){
                    if (i >= deg){
                        deg = i;
                        break;
                    }
                }
                degreeList[Integer.parseInt(vid_deg[0])] = deg;
            }
            String resultPath = resultFolder + "/" + property + ".csv";
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
        

        if(property == "degree-sum"){
            String[] writeResult = new String[3];
            writeResult[0] = Integer.toString(count);
            int degree1 = node_map.getInDegree(v1);
            int degree2 = node_map.getInDegree(v2);
            int deg1_final = degreeList[v1];
            int deg2_final = degreeList[v2];

            writeResult[1] = Integer.toString(degree1 + degree2);
            writeResult[2] = Integer.toString(deg1_final + deg2_final);
            try{
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else if(property=="common-neighbors"){
            String[] writeResult = new String[3];
            writeResult[0] = Integer.toString(count);
            IntSet neighbors1 = node_map.getNeighbors(v1);
            IntSet neighbors2 = node_map.getNeighbors(v2);
            int commNeighbors = 0;
            for(int n1 : neighbors1){
                if(neighbors2.contains(n1)){
                    commNeighbors++;
                }
            }
            writeResult[1] = Integer.toString(commNeighbors);

            neighbors1 = node_map.getInNeighbors(v1);
            neighbors2 = node_map.getInNeighbors(v2);
            int InCommNeighbors = 0;
            for(int n1 : neighbors1){
                if(neighbors2.contains(n1)){
                    InCommNeighbors++;
                }
            }
            writeResult[2] = Integer.toString(InCommNeighbors);
            try{
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else if (property == "adamic-adar"){
            String[] writeResult = new String[3];
            writeResult[0] = Integer.toString(count);
            IntSet neighbors1 = node_map.getNeighbors(v1);
            IntSet neighbors2 = node_map.getNeighbors(v2);

            IntSet commNeighbors = new IntOpenHashSet();
            for(int n1 : neighbors1){
                if(neighbors2.contains(n1)){
                    commNeighbors.add(n1);
                }
            }

            IntSet adarSet = new IntOpenHashSet();
            for(int n : commNeighbors){
                IntSet neighborNeighbor = node_map.getNeighbors(n);
                adarSet.addAll(neighborNeighbor);
            }

            double adar = 1.0 / Math.log(adarSet.size());
            writeResult[1] = Double.toString(adar);

            neighbors1 = node_map.getInNeighbors(v1);
            neighbors2 = node_map.getInNeighbors(v2);

            commNeighbors = new IntOpenHashSet();
            for(int n1 : neighbors1){
                if(neighbors2.contains(n1)){
                    commNeighbors.add(n1);
                }
            }

            adarSet = new IntOpenHashSet();
            for(int n : commNeighbors){
                IntSet neighborNeighbor = node_map.getInNeighbors(n);
                adarSet.addAll(neighborNeighbor);
            }

            adar = 1.0 / Math.log(adarSet.size());
            writeResult[2] = Double.toString(adar);
            try{
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else if (property == "jaccard-coeff"){
            String[] writeResult = new String[2];
            writeResult[0] = Integer.toString(count);
            IntSet neighbors1 = node_map.getNeighbors(v1);
            IntSet neighbors2 = node_map.getNeighbors(v2);
            IntSet union = new IntOpenHashSet();
            union.addAll(neighbors1);
            union.addAll(neighbors2);
            int commNeighbors = 0;
            for(int n1 : neighbors1){
                if(neighbors2.contains(n1)){
                    commNeighbors++;
                }
            }
            if(union.size() != 0){
                writeResult[1] = Double.toString(commNeighbors * 1.0 / union.size());
                try{
                    cw.writeNext(writeResult);
                    cw.flush();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        else if (property == "prefer-attach"){
            String[] writeResult = new String[3];
            writeResult[0] = Integer.toString(count);
            int degree1 = node_map.getInDegree(v1);
            int degree2 = node_map.getInDegree(v2);
            int deg1_final = degreeList[v1];
            int deg2_final = degreeList[v2];

            writeResult[1] = Integer.toString(degree1 * degree2);
            writeResult[2] = Integer.toString(deg1_final * deg2_final);
            try{
                cw.writeNext(writeResult);
                cw.flush();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Error");
            return;
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
