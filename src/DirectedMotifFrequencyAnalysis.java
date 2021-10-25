package src;
import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.*;
import java.util.HashMap;
import java.util.HashSet;
import java.time.*;
import java.io.*;

import com.opencsv.CSVWriter;

public class  DirectedMotifFrequencyAnalysis {
    DirectedNodeMap node_map; // Graph
    Random rd;
    long numSubgraph = 0;
    int vertex_number;
    int edge_number;
    int[] budget_array;
    double[] prevprev = new double[DirectedMotifType.values().length]; 
    double[] prev = new double[DirectedMotifType.values().length]; 
    long[] current = new long[DirectedMotifType.values().length]; // graph의 현재 motif 개수
    int count = 0;
    double[] target_motif_distribution; 

    String project_path = System.getProperty("user.dir");
    String read_path;
    String dataset;
    BufferedWriter filewriter;
    BufferedWriter transitionWriter;
    HashMap<Integer, Integer> degree_map;
    CSVWriter cw;
    CSVWriter mw;
    CSVWriter mv;
    CSVWriter deriv_writer;
    CSVWriter cv;
    int num_pow = 2;
    int afterYear = 1;
    ArrayList<Integer>[] selected_vertex ;
    long[] motif_frequency = new long[DirectedMotifType.values().length];
    int numNodes;
    int numEdges;
    String[][] changeHeader = {
        //{"m2-m9", "m4-m5", "m2-m5", "m1-m5", "new-m2", "new-m1", "new-m4"} // hepph
        {"m3-m10", "m2-m9", "m2-m7", "m2-m5", "m8-m12", "m4-m7", "new-m3", "new-m2", "new-m1", 
        "new-m7", "new-m4", "m9-m10", "m10-m12", "m4-m5", "m2-m3", "m11-m12", "m3-m8", "m6-m12", 
        "m5-m10", "m1-m5", "m3-m6", "m5-m11", "m12-m13", "m7-m10", "m5-m6", "m7-m11", "m7-m8", "m1-m3"} // internet growth & caida
    };
    HashSet<String> changeSet = new HashSet<String>();
    long[] change = new long[changeHeader[0].length];
    String resultFolder;
    LocalDate startDate = null;
    int[] writeTime = new int[10];
    public DirectedMotifFrequencyAnalysis(String read_path, String dataset, int numNodes, int numEdges){
        rd = new Random();
        numSubgraph = 0;
        this.read_path = read_path;
        this.dataset = dataset;
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.node_map = new DirectedNodeMap(this.numNodes);
        resultFolder = project_path + "/result/" + dataset;
        for(int i = 0 ; i < 10; i++){
            writeTime[i] = (int) (numEdges * 0.1 * (i+1));
        }
    }

    public void run(){
        System.out.println("[Directed Graph Analysis] Run\n");
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
            String motif_path = resultFolder + "/motif_ratio.csv";
            String motif_abs_path = resultFolder + "/motif_abs.csv";
            String deriv_path = resultFolder + "/deriv.csv";
            mw = new CSVWriter(new FileWriter(new File(motif_path), false));
            mv = new CSVWriter(new FileWriter(new File(motif_abs_path), false));
            deriv_writer = new CSVWriter(new FileWriter(new File(deriv_path), false));
            String[] motif_header = {
                "m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13"
            };
            mw.writeNext(motif_header);
            mw.flush();
            mv.writeNext(motif_header);
            mv.flush();
            deriv_writer.writeNext(motif_header);
            deriv_writer.flush();

            cw = new CSVWriter(new FileWriter(new File(resultFolder + "/change.csv"), false));
            cw.writeNext(changeHeader[0]);
            cw.flush();
            
            File read_file = new File(read_path);
            // 분석하려고 하는 graph (edge)를 읽어오는 filereader
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            
            line = null;
            long beforeTime = System.currentTimeMillis();
            line = filereader.readLine();
            String[] l_ = line.split("\t");
            int v1_ = Integer.parseInt(l_[0]);
            int v2_ = Integer.parseInt(l_[1]);
            startDate = null;
            //startDate = LocalDate.parse(l_[2]);
            // Edge를 읽어서 graph에 edge를 넣어 줌. (함수는 아래에 있습니다!)
            addEdge(v1_, v2_, startDate);

            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                //LocalDate date = LocalDate.parse(l[2]);
                LocalDate date = null;
                // Edge를 읽어서 graph에 edge를 넣어 줌. (함수는 아래에 있습니다!)
                addEdge(v1, v2, date);
            }
            long afterTime = System.currentTimeMillis();
            double secDiffTime = (afterTime - beforeTime)/1000.0; //두 시간에 차 계산
            System.out.println("time : "+secDiffTime);
            System.out.println(Arrays.toString(motif_frequency));
            //fileWriteChange();
        } catch(Exception e){
            e.printStackTrace();
        }
        fileWriteChange();
        try{
            String[] current_string = new String[motif_frequency.length];
            for(int i = 0; i < motif_frequency.length; i++){
                current_string[i] = Long.toString(motif_frequency[i]);
            }
            mv.writeNext(current_string);
            mv.flush();

            String arrival_time_path = resultFolder + "/arrival.csv";
            CSVWriter arrival_writer = new CSVWriter(new FileWriter(new File(arrival_time_path), false));
            String[] header = {"id", "arrival_time"};
            arrival_writer.writeNext(header);
            arrival_writer.flush();
            for(int node : node_map.nodes){
                int node_arrival_time = node_map.arrivalTime[node];
                String[] item = {Integer.toString(node), Integer.toString(node_arrival_time)};
                arrival_writer.writeNext(item);
                arrival_writer.flush();
            }
            arrival_writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // edge(v1, v2)를 추가하고, 이에 따라서 변경되는 motif distribution의 변화를 함께 분석하는 함수
    public void addEdge(int v1, int v2, LocalDate date) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }

        IntOpenHashSet visited = new IntOpenHashSet();
        
        //change = new int[changeHeader[0].length];

        // node v1의 이웃에 대해서
        for(int v3 : node_map.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited.add(v3);

            // 만약 v2랑 연결되어 있음 -> 이미 motif가 존재함 -> 이전 motif-1, 새로운 motif+1
            if (node_map.isConnected(v2, v3) || node_map.contains(v2, v1)){
                DirectedMotifType prevMotifType = getMotifType(v1, v2, v3, false);
                DirectedMotifType newMotifType = getMotifType(v1, v2, v3, true);
                motif_frequency[prevMotifType.ordinal()]--;
                motif_frequency[newMotifType.ordinal()]++;
                //String changeMotif = prevMotifType.toString() + "-" + newMotifType.toString();
                //changeSet.add(changeMotif);
                recordChangeMotif(prevMotifType, newMotifType);
            }
            // 아닐경우 새로운 motif+1
            else {
                DirectedMotifType newMotifType = getMotifType(v1, v2, v3, true);
                motif_frequency[newMotifType.ordinal()]++;
                //String changeMotif = "new-" + newMotifType.toString();
                //changeSet.add(changeMotif);
                recordChangeMotif("new", newMotifType);
            }
        }
        for (int v3 : node_map.getNeighbors(v2)){
            if (visited.contains(v3)) continue;
            if (v1 == v3) continue;
            if (node_map.isConnected(v1, v3) || node_map.contains(v2, v1)){
                DirectedMotifType prevMotifType = getMotifType(v1, v2, v3, false);
                DirectedMotifType newMotifType = getMotifType(v1, v2, v3, true);
                motif_frequency[prevMotifType.ordinal()]--;
                motif_frequency[newMotifType.ordinal()]++;
                //String changeMotif = prevMotifType.toString() + "-" + newMotifType.toString();
                //changeSet.add(changeMotif);
                recordChangeMotif(prevMotifType, newMotifType);
            }
            else {
                DirectedMotifType newMotifType = getMotifType(v1, v2, v3, true);
                motif_frequency[newMotifType.ordinal()]++;
                //String changeMotif = "new-" + newMotifType.toString();
                //changeSet.add(changeMotif);
                recordChangeMotif("new", newMotifType);
            }

        }

        // graph에 edge 추가
        node_map.addEdge(v1, v2);

        count++;
        
        float total = 0;
        for(int i =0; i < motif_frequency.length; i++){
            total = total + motif_frequency[i];
        }
        if (total == 0) return;
        

        String[] current_string = new String[current.length];
        try{
            
            for(int i = 0; i < current.length; i++){
                current_string[i] = Float.toString(motif_frequency[i] / total);
            }
            mw.writeNext(current_string);
            mw.flush();
            
            for(int i = 0; i < current.length; i++){
                current_string[i] = Long.toString(motif_frequency[i]);
            }
            mv.writeNext(current_string);
            mv.flush();
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        
        
        // Degree에 따른 First motif role을 찾을 때 사용
        
        /*
        int degree1 = node_map.getInnerDegree(v1);
        int degree2 = node_map.getInnerDegree(v2);
        //int degree1 = node_map.getDegree(v1);
        //int degree2 = node_map.getDegree(v2);
        if (log2(degree1) % 1 == 0 && degree1 > 1){
            long key1 = (((long)v1) << 32) | (degree1 & 0xffffffffL);
            if(node_map.graphlet_map.contains(v1)){
                DirectedGraphlet role = new DirectedGraphlet(node_map.graphlet_map.get(v1));
                node_map.degree_graphlet_map.put(key1, role);
                node_map.time_map.put(key1, count);
            }
        }
        if (log2(degree2) % 1 == 0 && degree2 > 1){
            long key2 = (((long)v2) << 32) | (degree2 & 0xffffffffL);
            if(node_map.graphlet_map.contains(v2)){
                DirectedGraphlet role = new DirectedGraphlet(node_map.graphlet_map.get(v2));
                node_map.degree_graphlet_map.put(key2, role);
                node_map.time_map.put(key2, count);
            }
        }
        */
        
        //Out-degree -> In-degree test
        /*
        int degree1 = node_map.getInnerDegree(v1);
        long key1 = (((long)v1) << 32) | (degree1 & 0xffffffffL);
        if(node_map.graphlet_map.contains(v1)){
            DirectedGraphlet role = new DirectedGraphlet(node_map.graphlet_map.get(v1));
            node_map.degree_graphlet_map.put(key1, role);
            node_map.time_map.put(key1, count);
        }
        */
        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            System.out.println(Arrays.toString(motif_frequency));
        }
        //fileWriteChange();
        /*
        if(startDate.plusMonths(afterYear).isBefore(date)){
            fileWriteChange();
            afterYear++;
        }
        */
        /*
        long[] graph_motif = countMotif();
        
        if(!Arrays.equals(graph_motif, motif_frequency)){
            System.out.println("1. Graph Motif From Count : " + Arrays.toString(graph_motif));
            System.out.println("2. Graph Motif From  : " + Arrays.toString(motif_frequency));
        }
        */
        
        
        

    }

    public int getIdx(String[] l, String x){
        for(int i = 0 ; i < l.length ; i++){
            if (l[i].equals(x)){
                return i;
            }
        }
        return -1;
    }
    public void recordChangeMotif(DirectedMotifType before, DirectedMotifType after){
        String key = before.toString() + "-" + after.toString();
        int idx = getIdx(changeHeader[0], key);
        change[idx]++;
    }

    public void recordChangeMotif(String new_, DirectedMotifType after){
        String key = new_ + "-" + after.toString();
        int idx = getIdx(changeHeader[0], key);
        change[idx]++;
    }

    public void fileWriteChange(){
        try{
            String[] result = new String[changeHeader[0].length];
            for(int i = 0; i < changeHeader[0].length; i++){
                result[i] = Long.toString(change[i]);
            }
            cw.writeNext(result);
            cw.flush();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public long[] countMotif(){
        long[] motifs = new long[DirectedMotifType.values().length];
        
        for (DirectedEdge e : node_map.getAllEdges()){
            int u = e.getSource();
            int v = e.getDestination();
            for(int w : node_map.getNeighbors(u)){
                if (w == v){
                    continue;
                }
                motifs[getMotifType(u, v, w, true).ordinal()]++;
            }
            for(int w : node_map.getNeighbors(v)){
                if (w == u){
                    continue;
                }
                if (!node_map.isConnected(w, u)){
                    motifs[getMotifType(u, v, w, true).ordinal()]++;
                }
            }
        }
        motifs[DirectedMotifType.m1.ordinal()] /= 2;
        motifs[DirectedMotifType.m2.ordinal()] /= 2;
        motifs[DirectedMotifType.m3.ordinal()] /= 3;
        motifs[DirectedMotifType.m4.ordinal()] /= 2;
        motifs[DirectedMotifType.m5.ordinal()] /= 3;
        motifs[DirectedMotifType.m6.ordinal()] /= 4;
        motifs[DirectedMotifType.m7.ordinal()] /= 3;
        motifs[DirectedMotifType.m8.ordinal()] /= 4;
        motifs[DirectedMotifType.m9.ordinal()] /= 3;
        motifs[DirectedMotifType.m10.ordinal()] /= 4;
        motifs[DirectedMotifType.m11.ordinal()] /= 4;
        motifs[DirectedMotifType.m12.ordinal()] /= 5;
        motifs[DirectedMotifType.m13.ordinal()] /= 6;
        return motifs;
    }
    public DirectedMotifType getMotifType(int v1, int v2, int v3, boolean flag){
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

        if (numEdges == 6) return DirectedMotifType.m13;
        else if (numEdges == 5) return DirectedMotifType.m12;
        else if (numEdges == 4){
            if (maxDegree == 4) return DirectedMotifType.m8;
            else if (minInDegree == 0) return DirectedMotifType.m11;
            else if (minOutDegree == 0) return DirectedMotifType.m6;
            else return DirectedMotifType.m10;
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) return DirectedMotifType.m7;
                else return DirectedMotifType.m9;
            }
            else {
                if (maxInDegree == 2) return DirectedMotifType.m5;
                else return DirectedMotifType.m3;
            }
        }
        else {
            if (maxOutDegree == 2) return DirectedMotifType.m1;
            else if (maxInDegree == 2) return DirectedMotifType.m4;
            else return DirectedMotifType.m2;
        }
    }
    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }
}
