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

public class TriangleAnalysis {
    NodeMap node_map; // Graph
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
    long[][] NPP;
    int numNodes;
    int maxDegree;
    boolean flagNodeTracking;
    int[] degreeList;

    public TriangleAnalysis(String read_path, String dataset, int numNodes, int maxDegree){
        this.node_map = new NodeMap(numNodes);
        rd = new Random();
        this.read_path = read_path;
        this.dataset = "NPP_"+dataset;
        this.NPP = new long[numNodes][NPPType.values().length];
        this.numNodes = numNodes;
        this.maxDegree = maxDegree;
    }
    
    
    public void run(){
        String line = null ;
        resultFolder = projectPath + "/result/" + dataset;
        File folder = new File(resultFolder);
        if (!folder.exists()){
            folder.mkdir();
        }

        
        try{
            if (flagNodeTracking){
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
            }

            // Result 폴더 만들어주기
            for(int i = num_pow ; i < maxDegree; i = i * num_pow){
                String path = resultFolder + "/" + String.format("%04d", i) + ".csv";
                CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                String[] header = {"degree",
                    "cycle",
                    "path_two",
                    "path_one",
                    "line_connected",
                    "line_isolated"};
                cw.writeNext(header);
                cw.flush();
                cw.close();
            }


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
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // edge(v1, v2)를 추가하고, 이에 따라서 변경되는 motif distribution의 변화를 함께 분석하는 함수
    public void addEdge(int v1, int v2) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }
        node_map.addEdge(v1, v2);
        node_map.addEdge(v2, v1);

        count++;
        IntOpenHashSet visited = new IntOpenHashSet();
        
        for(int v3 : node_map.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited.add(v3);
            if (node_map.contains(v2, v3)){
                NPP[v1][NPPType.PATH_ONE.ordinal()]--;
                NPP[v2][NPPType.PATH_ONE.ordinal()]--;
                NPP[v3][NPPType.PATH_TWO.ordinal()]--;
                NPP[v1][NPPType.CYCLE.ordinal()]++;
                NPP[v2][NPPType.CYCLE.ordinal()]++;
                NPP[v3][NPPType.CYCLE.ordinal()]++;
            }
            else {
                NPP[v1][NPPType.PATH_TWO.ordinal()]++;
                NPP[v2][NPPType.PATH_ONE.ordinal()]++;
                NPP[v3][NPPType.PATH_ONE.ordinal()]++;
            }
        }
        for (int v3 : node_map.getNeighbors(v2)){
            if (visited.contains(v3)) continue;
            if (v1 == v3) continue;
            NPP[v1][NPPType.PATH_ONE.ordinal()]++;
            NPP[v2][NPPType.PATH_TWO.ordinal()]++;
            NPP[v3][NPPType.PATH_ONE.ordinal()]++;
        }
        int degree1 = node_map.getDegree(v1);
        int degree2 = node_map.getDegree(v2);
        int currNodes = node_map.nodes.size();
        NPP[v1][NPPType.LINE_CONNECTED.ordinal()] = degree1;
        NPP[v1][NPPType.LINE_ISOLATED.ordinal()] = currNodes - degree1;
        NPP[v2][NPPType.LINE_CONNECTED.ordinal()] = degree2;
        NPP[v2][NPPType.LINE_ISOLATED.ordinal()] = currNodes - degree2;

        if(log2(degree1) % 1 == 0 && degree1 > 1){
            printResult(v1, degree1);
        }
        
        if(log2(degree2) % 1 == 0 && degree2 > 1){
            printResult(v2, degree2);
        }
        
        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            //long[] graph_motif = graphlet2motif();
            //System.out.println(Arrays.toString(graph_motif));
            
        }
        
        //long[] graph_motif = countMotifInstance.countTriangle(node_map);
        //long[] motifs = NPP2Triangle();
        /*
        if(!Arrays.equals(graph_motif, motifs)){
            System.out.println("1. Graph Motif From Count : " + Arrays.toString(graph_motif));
            System.out.println("2. Graph Motif From Graphlet : " + Arrays.toString(motifs));
        }
        */

    }

    public long[] NPP2Triangle(){
        long[] motifs = new long[TripletType.values().length];
        long[] validate = new long[NPPType.values().length];
        for(int i = 0; i < this.numNodes;i++){  
            motifs[TripletType.TRIANGLE.ordinal()] += NPP[i][NPPType.CYCLE.ordinal()];
            motifs[TripletType.PATH.ordinal()] += NPP[i][NPPType.PATH_ONE.ordinal()] +
                                                    NPP[i][NPPType.PATH_TWO.ordinal()];
            /*                                    
            for(int j = 0; j <NPPType.values().length; j++){
                validate[j] += NPP[i][j];
            }
            */

        }
        
        // Validation
        //System.out.println(Arrays.toString(validate));
        for(int i = 0 ; i < TripletType.values().length; i++){
            motifs[i] /= 3;
        }       
        return motifs;
    }



    public void printResult(int v, int degree){
        String path = resultFolder + "/" + String.format("%04d", degree) + ".csv";
        try{
            CSVWriter cw = new CSVWriter(new FileWriter(new File(path), true));
            long[] nodePP = NPP[v];
            String[] result = new String[nodePP.length + 1];
            result[0] = Integer.toString(v);
            for(int i = 1; i < result.length; i++){
                result[i] = Long.toString(nodePP[i-1]);
            }
            cw.writeNext(result);
            cw.flush();
            cw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }
}
