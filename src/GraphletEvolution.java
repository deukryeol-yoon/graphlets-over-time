import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.*;
import java.util.HashSet;
import java.io.*;

import com.opencsv.CSVWriter;

public class GraphletEvolution {
    DirectedNodeMap node_map;
    Random rd;
    long[] current = new long[GraphletType.values().length]; 
    int count = 0;

    String project_path = System.getProperty("user.dir");
    String read_path;
    String dataset;
    BufferedWriter filewriter;
    BufferedWriter transitionWriter;
    CSVWriter cw;
    CSVWriter mw;
    CSVWriter mv;
    long[] graphletFrequency = new long[GraphletType.values().length];
    int numNodes;
    int numEdges;
    String[][] transitionHeader = {
        {"m3-m10", "m2-m9", "m2-m7", "m2-m5", "m8-m12", "m4-m7", "new-m3", "new-m2", "new-m1", 
        "new-m7", "new-m4", "m9-m10", "m10-m12", "m4-m5", "m2-m3", "m11-m12", "m3-m8", "m6-m12", 
        "m5-m10", "m1-m5", "m3-m6", "m5-m11", "m12-m13", "m7-m10", "m5-m6", "m7-m11", "m7-m8", "m1-m3"} 
    };
    HashSet<String> changeSet = new HashSet<String>();
    long[] change = new long[transitionHeader[0].length];
    String resultFolder;
    int[] writeTime = new int[10];
    int step_count = 0;
    public GraphletEvolution(String read_path, String dataset, int numNodes, int numEdges){
        rd = new Random();
        this.read_path = read_path;
        this.dataset = dataset;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.node_map = new DirectedNodeMap(this.numNodes);
        this.step_count = this.numEdges / 1000;
        resultFolder = project_path + "/../result/graph-analysis/" + dataset;
        for(int i = 0 ; i < 10; i++){
            writeTime[i] = (int) (numEdges * 0.1 * (i+1));
        }
        
    }

    public void run(){
        String line = null ;
        File folder = new File(resultFolder);
        if(folder.exists()){
            File[] files = folder.listFiles();
            for(File currentFile : files)
                currentFile.delete();
            folder.delete();
        }
        folder.mkdir();
        try{
            String motif_path = resultFolder + "/graphlet_ratio.csv";
            String motif_abs_path = resultFolder + "/graphlet_abs.csv";
            mw = new CSVWriter(new FileWriter(new File(motif_path), false));
            mv = new CSVWriter(new FileWriter(new File(motif_abs_path), false));
            String[] motif_header = {
                "idx", "m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13"
            };
            mw.writeNext(motif_header);
            mw.flush();
            mv.writeNext(motif_header);
            mv.flush();

            cw = new CSVWriter(new FileWriter(new File(resultFolder + "/change.csv"), false));
            cw.writeNext(transitionHeader[0]);
            cw.flush();
            
            File read_file = new File(read_path);
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            
            line = null;
            long beforeTime = System.currentTimeMillis();
            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                addEdge(v1, v2);
            }
            long afterTime = System.currentTimeMillis();
            double secDiffTime = (afterTime - beforeTime)/1000.0;

            String[] current_string = new String[current.length];
            for(int i = 0; i < current.length; i++){
                current_string[i] = Long.toString(graphletFrequency[i]);
            }
            mv.writeNext(current_string);
            mv.flush();

            System.out.println("time : "+secDiffTime);
            System.out.println(Arrays.toString(graphletFrequency));
        } catch(Exception e){
            e.printStackTrace();
        }
        fileWriteChange();
    }

    public void addEdge(int v1, int v2) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }
        
        IntOpenHashSet visited1 = new IntOpenHashSet();
        IntOpenHashSet visited2 = new IntOpenHashSet();

        for(int v3 : node_map.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited1.add(v3);

            if (node_map.isConnected(v2, v3) || node_map.contains(v2, v1)){
                GraphletType prevMotifType = getGraphletType(v1, v2, v3, false);
                GraphletType newMotifType = getGraphletType(v1, v2, v3, true);
                graphletFrequency[prevMotifType.ordinal()]--;
                graphletFrequency[newMotifType.ordinal()]++;
                recordChangeMotif(prevMotifType, newMotifType);
            }
            else {
                GraphletType newMotifType = getGraphletType(v1, v2, v3, true);
                graphletFrequency[newMotifType.ordinal()]++;
                recordChangeMotif("new", newMotifType);
            }
        }
        for (int v3 : node_map.getNeighbors(v2)){
            if (v1 == v3) continue;
            visited2.add(v3);
            if (node_map.isConnected(v1, v3) || node_map.contains(v2, v1)){
                GraphletType prevMotifType = getGraphletType(v1, v2, v3, false);
                GraphletType newMotifType = getGraphletType(v1, v2, v3, true);
                graphletFrequency[prevMotifType.ordinal()]--;
                graphletFrequency[newMotifType.ordinal()]++;
                recordChangeMotif(prevMotifType, newMotifType);
            }
            else {
                GraphletType newMotifType = getGraphletType(v1, v2, v3, true);
                graphletFrequency[newMotifType.ordinal()]++;
                recordChangeMotif("new", newMotifType);
            }
        }
        visited1.retainAll(visited2); 
        for (int v4 : visited1){
            GraphletType prevMotifType = getGraphletType(v1, v2, v4, false);
            GraphletType newMotifType = getGraphletType(v1, v2, v4, true);
            graphletFrequency[prevMotifType.ordinal()]++;
            graphletFrequency[newMotifType.ordinal()]--;
            recordChangeMotif(prevMotifType, newMotifType);
        }
        
        node_map.addEdge(v1, v2);
        count++;
        
        float total = 0;
        for(int i =0; i < graphletFrequency.length; i++){
            total = total + graphletFrequency[i];
        }
        if (total == 0) return;
        

        String[] current_string = new String[current.length + 1];
        
        try{
            
            // Write ratio distribution
            if ( (count+1) % step_count == 0){
                current_string[0] = Integer.toString(count);
                for(int i = 0; i < current.length; i++){
                    current_string[i+1] = Float.toString(graphletFrequency[i] / total);
                }
                mw.writeNext(current_string);
                mw.flush();
            }
            
            // Write absolute count distribution
            
            //for(int i = 0; i < current.length; i++){
            //    current_string[i] = Long.toString(graphletFrequency[i]);
            //}
            //mv.writeNext(current_string);
            //mv.flush();
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        if (count % 1000000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            System.out.println(Arrays.toString(graphletFrequency));
        }
        

    }

    public int getIdx(String[] l, String x){
        for(int i = 0 ; i < l.length ; i++){
            if (l[i].equals(x)){
                return i;
            }
        }
        return -1;
    }
    public void recordChangeMotif(GraphletType before, GraphletType after){
        String key = before.toString() + "-" + after.toString();
        int idx = getIdx(transitionHeader[0], key);
        change[idx]++;
    }

    public void recordChangeMotif(String new_, GraphletType after){
        String key = new_ + "-" + after.toString();
        int idx = getIdx(transitionHeader[0], key);
        change[idx]++;
    }

    public void fileWriteChange(){
        try{
            String[] result = new String[transitionHeader[0].length];
            for(int i = 0; i < transitionHeader[0].length; i++){
                result[i] = Long.toString(change[i]);
            }
            cw.writeNext(result);
            cw.flush();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public GraphletType getGraphletType(int v1, int v2, int v3, boolean flag){
        int V1IN = 0, V2IN = 0, V3IN = 0, V1OUT = 0, V2OUT = 0, V3OUT = 0;
        IntSet V1OUTSET = node_map.getOutNeighbors(v1);
        IntSet V2OUTSET = node_map.getOutNeighbors(v2);
        IntSet V3OUTSET = node_map.getOutNeighbors(v3);
        if (flag){
            V1OUT++;
            V2IN++;
        }
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
        int numEdges = (V1IN + V2IN + V3IN + V1OUT + V2OUT + V3OUT)/2;
        int maxDegree = Math.max(Math.max(V1IN + V1OUT, V2IN + V2OUT), V3IN+V3OUT);
        int minInDegree = Math.min(Math.min(V1IN, V2IN), V3IN);
        int maxInDegree = Math.max(Math.max(V1IN, V2IN), V3IN);
        int minOutDegree = Math.min(Math.min(V1OUT, V2OUT), V3OUT);
        int maxOutDegree = Math.max(Math.max(V1OUT, V2OUT), V3OUT);

        if (numEdges == 6) return GraphletType.m13;
        else if (numEdges == 5) return GraphletType.m12;
        else if (numEdges == 4){
            if (maxDegree == 4) return GraphletType.m8;
            else if (minInDegree == 0) return GraphletType.m11;
            else if (minOutDegree == 0) return GraphletType.m6;
            else return GraphletType.m10;
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) return GraphletType.m7;
                else return GraphletType.m9;
            }
            else {
                if (maxInDegree == 2) return GraphletType.m5;
                else return GraphletType.m3;
            }
        }
        else {
            if (maxOutDegree == 2) return GraphletType.m1;
            else if (maxInDegree == 2) return GraphletType.m4;
            else return GraphletType.m2;
        }
    }
    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }

    public long[] countGraphlet(DirectedNodeMap node_map){
        long[] result = new long[GraphletType.values().length];
        for(DirectedEdge e : node_map.getAllEdges()){
            int v1 = e.getSource();
            int v2 = e.getDestination();
            for (int v3 : node_map.getNeighbors(v1)){
                if (v2 == v3){
                    continue;
                }
                GraphletType type = getGraphletType(v1, v2, v3, true);
                result[type.ordinal()]++;
            }

            for (int v3 : node_map.getNeighbors(v2)){
                if (v3 == v1 || node_map.getNeighbors(v1).contains(v3)){
                    continue;
                }
                GraphletType type = getGraphletType(v1, v2, v3, true);
                result[type.ordinal()]++;
            }
        }

        result[GraphletType.m1.ordinal()] /= 2;
        result[GraphletType.m2.ordinal()] /= 2;
        result[GraphletType.m3.ordinal()] /= 3;
        result[GraphletType.m4.ordinal()] /= 2;
        result[GraphletType.m5.ordinal()] /= 3;
        result[GraphletType.m6.ordinal()] /= 4;
        result[GraphletType.m7.ordinal()] /= 3;
        result[GraphletType.m8.ordinal()] /= 4;
        result[GraphletType.m9.ordinal()] /= 3;
        result[GraphletType.m10.ordinal()] /= 4;
        result[GraphletType.m11.ordinal()] /= 4;
        result[GraphletType.m12.ordinal()] /= 5;
        result[GraphletType.m13.ordinal()] /= 6;


        return result;
    }

}
