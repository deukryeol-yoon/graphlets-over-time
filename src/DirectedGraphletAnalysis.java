package src;
import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import it.unimi.dsi.fastutil.ints.*;

import java.io.*;
import java.lang.Math;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class DirectedGraphletAnalysis {
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
    double[] centralityList;
    String centrality;
    static double[] boundary_centrality;
    public DirectedGraphletAnalysis(String read_path, String dataset, int numNodes, int maxDegree, String centrality){
        this.node_map = new DirectedNodeMap(numNodes);
        rd = new Random();
        this.read_path = read_path;
        this.dataset = dataset;
        this.graphlets = new long[numNodes][DirectedGraphletType.values().length];
        this.numNodes = numNodes;
        this.maxDegree = maxDegree;
        this.centrality = centrality;
    }
    
    
    public void run(){
        String line = null ;
        resultFolder = projectPath + "/result/" + dataset;
        File folder = new File(resultFolder);
        if (!folder.exists()){
            folder.mkdir();
        }

        
        try{

            String centralityPath = System.getProperty("user.dir") + "/data/temporal_graph/" + dataset + "-" + centrality + ".csv";
            File f = new File(centralityPath);
            if(!f.exists()){
                System.out.println("Graphlet Tracking Mode is On, but no degree file exists");
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
            // Result 폴더 만들어주기
            String directory = resultFolder + "/tracking/";
            folder = new File(directory);
            if (!folder.exists()){
                folder.mkdir();
            }

            directory = directory + centrality + "/";
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
            // 분석하려고 하는 graph (edge)를 읽어오는 filereader
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
                    //key = (((long)log_maxdegree2) << 32) | (degree2 & 0xffffffffL);
                    long key = (((long)i) <<32 | (j) & 0xffffffffL);
                    if(!node_map.graphlet_map.contains(key)){
                        continue;
                    }
                    long[] graphlet = node_map.graphlet_map.get(key);
                    
                    String[] outStrings = new String[DirectedGraphletType.values().length];
                    //outStrings[0] = Integer.toString(j);
                    double sum = 0.0;
                    for(int k = 0; k < DirectedGraphletType.values().length; k++){
                        sum += graphlet[k];
                    }
                    for(int k = 0; k < DirectedGraphletType.values().length; k++){
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

    // edge(v1, v2)를 추가하고, 이에 따라서 변경되는 motif distribution의 변화를 함께 분석하는 함수
    public void addEdge(int v1, int v2) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }

        // graph에 edge 추가
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

        // Degree에 따른 First motif role을 찾을 때 사용
        
        //int degree1 = node_map.getInDegree(v1);
        int degree2 = node_map.getInDegree(v2);
        /*
        if(log2(degree1) % 1 == 0 && degree1 > 1){
            printResult(v1, degree1);
        }
        */
        if(log2(degree2) % 1 == 0 && degree2 > 1){
            printResult(v2, degree2);
        }
        long[] graphlet ;
        /*
        int maxdegree1 = degreeList[v1];
        Long key = (((long)maxdegree1) << 32) | (degree1 & 0xffffffffL);
        if(!node_map.graphlet_map.contains(key)){
            graphlet = new long[DirectedGraphletType.values().length+1];
        }
        else graphlet = node_map.graphlet_map.get(key);
        for(int i = 0; i < DirectedGraphletType.values().length; i++){
            graphlet[i] += graphlets[v1][i];
        }
        graphlet[graphlet.length-1]++;
        node_map.graphlet_map.put(key, graphlet);
        */
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
                graphlet = new long[DirectedGraphletType.values().length+1];
            }
            else graphlet = node_map.graphlet_map.get(key);
    
            for(int i = 0; i < DirectedGraphletType.values().length; i++){
                graphlet[i] += graphlets[v2][i];
            }
            graphlet[graphlet.length-1]++;
            node_map.graphlet_map.put(key, graphlet);
        }

        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            long[] graph_motif = graphlet2motif();
            System.out.println(Arrays.toString(graph_motif));
            
        }
        /*
        
        long[] graph_motif = countMotif();
        long[] motifs = graphlet2motif();
        if(!Arrays.equals(graph_motif, motifs)){
            System.out.println("1. Graph Motif From Count : " + Arrays.toString(graph_motif));
            System.out.println("2. Graph Motif From Graphlet : " + Arrays.toString(motifs));
        }
        */
        
        
        
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
        motifs[DirectedMotifType.m10.ordinal()] /=4;
        motifs[DirectedMotifType.m11.ordinal()] /=4;
        motifs[DirectedMotifType.m12.ordinal()] /=5;
        motifs[DirectedMotifType.m13.ordinal()] /=6;
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
    public void printResult(int v, int degree){
        String path = resultFolder + "/node_feature/" + String.format("%04d", degree) + ".csv";
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

    public long[] graphlet2motif(){
        long[] motifs = new long[DirectedMotifType.values().length];
        long[] validate = new long[DirectedGraphletType.values().length];
        for(int i = 0; i < this.numNodes;i++){  
            motifs[DirectedMotifType.m1.ordinal()] += graphlets[i][DirectedGraphletType.m1_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m1_2.ordinal()];
            motifs[DirectedMotifType.m2.ordinal()] += graphlets[i][DirectedGraphletType.m2_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m2_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m2_3.ordinal()];
            motifs[DirectedMotifType.m3.ordinal()] += graphlets[i][DirectedGraphletType.m3_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m3_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m3_3.ordinal()];
            motifs[DirectedMotifType.m4.ordinal()] += graphlets[i][DirectedGraphletType.m4_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m4_2.ordinal()];
            motifs[DirectedMotifType.m5.ordinal()] += graphlets[i][DirectedGraphletType.m5_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m5_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m5_3.ordinal()];
            motifs[DirectedMotifType.m6.ordinal()] += graphlets[i][DirectedGraphletType.m6_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m6_2.ordinal()];
            motifs[DirectedMotifType.m7.ordinal()] += graphlets[i][DirectedGraphletType.m7_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m7_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m7_3.ordinal()];
            motifs[DirectedMotifType.m8.ordinal()] += graphlets[i][DirectedGraphletType.m8_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m8_2.ordinal()];
            motifs[DirectedMotifType.m9.ordinal()] += graphlets[i][DirectedGraphletType.m9_1.ordinal()];
            motifs[DirectedMotifType.m10.ordinal()] += graphlets[i][DirectedGraphletType.m10_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m10_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m10_3.ordinal()];
            motifs[DirectedMotifType.m11.ordinal()] += graphlets[i][DirectedGraphletType.m11_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m11_2.ordinal()];
            motifs[DirectedMotifType.m12.ordinal()] += graphlets[i][DirectedGraphletType.m12_1.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m12_2.ordinal()] +
                                                    graphlets[i][DirectedGraphletType.m12_3.ordinal()];
            motifs[DirectedMotifType.m13.ordinal()] += graphlets[i][DirectedGraphletType.m13_1.ordinal()];
            /*
            for(int j = 0; j <DirectedGraphletType.values().length; j++){
                validate[j] += graphlets[i][j];
            }
            */
        }
        
        // Validation
        //System.out.println(Arrays.toString(validate));
        for(int i = 0 ; i < DirectedMotifType.values().length; i++){
            motifs[i] /= 3;
        }       
        return motifs;
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
                    graphlets[v2][DirectedGraphletType.m7_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m7_3.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m7_1.ordinal()]++;
                }
                else {
                    //m3
                    graphlets[v1][DirectedGraphletType.m3_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m3_3.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m3_1.ordinal()]++;
                }
            }
            else if (maxOutDegree == 2){
                graphlets[v1][DirectedGraphletType.m1_2.ordinal()]++;
                graphlets[v2][DirectedGraphletType.m1_1.ordinal()]++;
                graphlets[v3][DirectedGraphletType.m1_1.ordinal()]++;
            }
            else if (maxInDegree == 2){
                graphlets[v2][DirectedGraphletType.m4_2.ordinal()]++;
                graphlets[v1][DirectedGraphletType.m4_1.ordinal()]++;
                graphlets[v3][DirectedGraphletType.m4_1.ordinal()]++;
            }
            else {
                //m2
                if (V1IN + V1OUT == 2){
                    graphlets[v1][DirectedGraphletType.m2_2.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m2_1.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m2_3.ordinal()]++;
                }
                else if (V2IN + V2OUT == 2){
                    graphlets[v2][DirectedGraphletType.m2_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m2_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m2_3.ordinal()]++;
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
            graphlets[v1][DirectedGraphletType.m12_3.ordinal()]--;
            graphlets[v2][DirectedGraphletType.m12_1.ordinal()]--;
            graphlets[v3][DirectedGraphletType.m12_2.ordinal()]--;

            graphlets[v1][DirectedGraphletType.m13_1.ordinal()]++;
            graphlets[v2][DirectedGraphletType.m13_1.ordinal()]++;
            graphlets[v3][DirectedGraphletType.m13_1.ordinal()]++;
        } 
        else if (numEdges == 4){
            //m8
            if (maxDegree == 4) {
                graphlets[v1][DirectedGraphletType.m8_1.ordinal()]--;
                graphlets[v1][DirectedGraphletType.m12_1.ordinal()]++;

                graphlets[v2][DirectedGraphletType.m8_1.ordinal()]--;
                graphlets[v2][DirectedGraphletType.m12_3.ordinal()]++;

                graphlets[v3][DirectedGraphletType.m8_2.ordinal()]--;
                graphlets[v3][DirectedGraphletType.m12_2.ordinal()]++;
            }
            else if (minInDegree == 0) {
                // m11
                graphlets[v1][DirectedGraphletType.m11_2.ordinal()]--;
                graphlets[v1][DirectedGraphletType.m12_2.ordinal()]++;
                
                graphlets[v2][DirectedGraphletType.m11_1.ordinal()]--;
                graphlets[v2][DirectedGraphletType.m12_1.ordinal()]++;

                graphlets[v3][DirectedGraphletType.m11_2.ordinal()]--;
                graphlets[v3][DirectedGraphletType.m12_3.ordinal()]++;
            }
            else if (minOutDegree == 0) {
                // m6
                graphlets[v1][DirectedGraphletType.m6_2.ordinal()]--;
                graphlets[v1][DirectedGraphletType.m12_3.ordinal()]++;

                graphlets[v2][DirectedGraphletType.m6_1.ordinal()]--;
                graphlets[v2][DirectedGraphletType.m12_2.ordinal()]++; 

                graphlets[v3][DirectedGraphletType.m6_1.ordinal()]--;
                graphlets[v3][DirectedGraphletType.m12_1.ordinal()]++;
            }
            else {
                // m10
                if((V1IN + V1OUT) == 2){
                    graphlets[v1][DirectedGraphletType.m10_1.ordinal()]--;
                    graphlets[v1][DirectedGraphletType.m12_1.ordinal()]++;

                    graphlets[v2][DirectedGraphletType.m10_2.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m12_2.ordinal()]++;

                    graphlets[v3][DirectedGraphletType.m10_3.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m12_3.ordinal()]++;
                }
                else {
                    graphlets[v1][DirectedGraphletType.m10_3.ordinal()]--;
                    graphlets[v1][DirectedGraphletType.m12_2.ordinal()]++;

                    graphlets[v2][DirectedGraphletType.m10_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m12_3.ordinal()]++;

                    graphlets[v3][DirectedGraphletType.m10_2.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m12_1.ordinal()]++;
                }

            }
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) {
                    // m7
                    if (V1IN == 2){
                        graphlets[v1][DirectedGraphletType.m7_2.ordinal()]--;
                        graphlets[v1][DirectedGraphletType.m8_2.ordinal()]++;

                        graphlets[v2][DirectedGraphletType.m7_3.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m8_1.ordinal()]++;

                        graphlets[v3][DirectedGraphletType.m7_1.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m8_1.ordinal()]++;
                    }
                    else if (V2IN == 1){
                        graphlets[v1][DirectedGraphletType.m7_3.ordinal()]--;
                        graphlets[v1][DirectedGraphletType.m11_1.ordinal()]++;

                        graphlets[v2][DirectedGraphletType.m7_1.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m11_2.ordinal()]++;

                        graphlets[v3][DirectedGraphletType.m7_2.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m11_2.ordinal()]++;
                    }
                    else {
                        graphlets[v1][DirectedGraphletType.m7_1.ordinal()]--;
                        graphlets[v1][DirectedGraphletType.m10_2.ordinal()]++;

                        graphlets[v2][DirectedGraphletType.m7_3.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m10_1.ordinal()]++;

                        graphlets[v3][DirectedGraphletType.m7_2.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m10_3.ordinal()]++;
                    }
                }
                else {
                    //m9
                    graphlets[v1][DirectedGraphletType.m9_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m9_1.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m9_1.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m10_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m10_3.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m10_1.ordinal()]++;
                    
                }
            }
            else {
                if (maxInDegree == 2) {
                    //m5
                    if (V1IN == 1 ){
                        graphlets[v1][DirectedGraphletType.m5_1.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m5_2.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m5_3.ordinal()]--;

                        graphlets[v1][DirectedGraphletType.m6_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m6_1.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m6_2.ordinal()]++;
                    }
                    else{
                        if (V2OUT == 2){
                            graphlets[v1][DirectedGraphletType.m5_3.ordinal()]--;
                            graphlets[v2][DirectedGraphletType.m5_2.ordinal()]--;
                            graphlets[v3][DirectedGraphletType.m5_1.ordinal()]--;
    
                            graphlets[v1][DirectedGraphletType.m10_3.ordinal()]++;
                            graphlets[v2][DirectedGraphletType.m10_2.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m10_1.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m5_3.ordinal()]--;
                            graphlets[v2][DirectedGraphletType.m5_1.ordinal()]--;
                            graphlets[v3][DirectedGraphletType.m5_2.ordinal()]--;
    
                            graphlets[v1][DirectedGraphletType.m11_2.ordinal()]++;
                            graphlets[v2][DirectedGraphletType.m11_2.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m11_1.ordinal()]++;
                        }
                    }
                }
                else {
                    //m3
                    if (V1OUT == 1){
                        graphlets[v1][DirectedGraphletType.m3_1.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m3_3.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m3_2.ordinal()]--;

                        graphlets[v1][DirectedGraphletType.m6_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m6_2.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m6_1.ordinal()]++;
                    }
                    else {
                        if (V2OUT == 2){
                            graphlets[v1][DirectedGraphletType.m3_3.ordinal()]--;
                            graphlets[v2][DirectedGraphletType.m3_2.ordinal()]--;
                            graphlets[v3][DirectedGraphletType.m3_1.ordinal()]--;
    
                            graphlets[v1][DirectedGraphletType.m8_1.ordinal()]++;
                            graphlets[v2][DirectedGraphletType.m8_2.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m8_1.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m3_3.ordinal()]--;
                            graphlets[v2][DirectedGraphletType.m3_1.ordinal()]--;
                            graphlets[v3][DirectedGraphletType.m3_2.ordinal()]--;
    
                            graphlets[v1][DirectedGraphletType.m10_1.ordinal()]++;
                            graphlets[v2][DirectedGraphletType.m10_3.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m10_2.ordinal()]++;
                        }
                    }
                }
            }
        }
        else {
            if (maxOutDegree == 2) {
                //m1
                if (V2OUT == 2){
                    graphlets[v1][DirectedGraphletType.m1_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m1_2.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m1_1.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m3_1.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m3_2.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m3_3.ordinal()]++;
                }
                else {
                    graphlets[v1][DirectedGraphletType.m1_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m1_1.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m1_2.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m5_1.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m5_3.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m5_2.ordinal()]++;
                }
            }
            else if (maxInDegree == 2) {
                //m4
                if (V1IN == 2){
                    graphlets[v1][DirectedGraphletType.m4_2.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m4_1.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m4_1.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m7_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m7_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m7_3.ordinal()]++;
                }
                else {
                    graphlets[v1][DirectedGraphletType.m4_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m4_1.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m4_2.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m5_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m5_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m5_3.ordinal()]++;
                }
            }
            else {
                //m2
                if (V1IN+V1OUT == 2){
                    graphlets[v1][DirectedGraphletType.m2_2.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m2_1.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m2_3.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m3_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m3_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m3_3.ordinal()]++;
                }
                else if (V1OUT == 1){
                    graphlets[v1][DirectedGraphletType.m2_1.ordinal()]--;
                    graphlets[v2][DirectedGraphletType.m2_3.ordinal()]--;
                    graphlets[v3][DirectedGraphletType.m2_2.ordinal()]--;

                    graphlets[v1][DirectedGraphletType.m5_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m5_3.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m5_1.ordinal()]++;
                }
                else {
                    if (V2IN == 0){
                        graphlets[v1][DirectedGraphletType.m2_3.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m2_1.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m2_2.ordinal()]--;
    
                        graphlets[v1][DirectedGraphletType.m9_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m9_1.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m9_1.ordinal()]++;
                    }
                    else {
                        graphlets[v1][DirectedGraphletType.m2_3.ordinal()]--;
                        graphlets[v2][DirectedGraphletType.m2_2.ordinal()]--;
                        graphlets[v3][DirectedGraphletType.m2_1.ordinal()]--;
    
                        graphlets[v1][DirectedGraphletType.m7_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m7_2.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m7_3.ordinal()]++;
                    }
                }
                    
            }
        }
    }
    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }
}
