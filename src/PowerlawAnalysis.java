package src;
import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import it.unimi.dsi.fastutil.ints.*;

import java.io.*;
import java.lang.Math;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.text.SimpleDateFormat; 
import java.time.*;
import java.time.format.DateTimeFormatter;


public class PowerlawAnalysis {
    DirectedNodeMap DMap; // Graph
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
    int numEdges;

    ArrayList<InfoDate> listDate = new ArrayList<InfoDate>();
    int[] arrSrcNode;
    int yearInterval = 9;
    int[] idxCurrent = new int[yearInterval];
    int idxTime = 0;
    int[] nodeDegree;
    String[] features = {
        "vid", "numEdges", "numNodes",
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
        "m13_1",
        "cycle",
        "path_two",
        "path_one",
        "line_connected",
        "line_isolated"
        };

    public PowerlawAnalysis(String read_path, String dataset, int numNodes, int maxDegree, int numEdges){
        this.DMap = new DirectedNodeMap(numNodes);
        rd = new Random();
        this.read_path = read_path;
        this.dataset = dataset;
        this.graphlets = new long[numNodes][DirectedGraphletType.values().length];
        this.numNodes = numNodes;
        this.maxDegree = maxDegree;
        this.numEdges = numEdges;
        arrSrcNode = new int[numEdges];
    }
    
    
    public void run(){
        if (!init()){
            return;
        }
        try{
            File read_file = new File(read_path);
            // 분석하려고 하는 graph (edge)를 읽어오는 filereader
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            String line = null;
            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                String[] time = l[2].split(" ");
                LocalDate t = LocalDate.parse(time[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                addEdge(v1, v2, t);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(idxCurrent));
    }

    // edge(v1, v2)를 추가하고, 이에 따라서 변경되는 motif distribution의 변화를 함께 분석하는 함수
    public void addEdge(int v1, int v2, LocalDate t) {
        
        if (DMap.contains(v1, v2) || v1 == v2){
            return;
        }

        // graph에 edge 추가
        DMap.addEdge(v1, v2);

        // Directed Graphlet Analysis
        IntOpenHashSet visited = new IntOpenHashSet();
        
        for(int v3 : DMap.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited.add(v3);
            update(v1, v2, v3);
        }

        for (int v3 : DMap.getNeighbors(v2)){
            if (visited.contains(v3)) continue;
            if (v1 == v3) continue;
            update(v1, v2, v3);
        }
        
        int degree2 = DMap.getInDegree(v2);
        
        if(log2(degree2) % 1 == 0 && degree2 > 1){
            InfoDate infoDate = new InfoDate(t, v2, count, graphlets[v2]);
            listDate.add(infoDate);
        }   
        
        if(!listDate.isEmpty()){
            /*
            for(int i = 0; i < yearInterval; i++){
                InfoDate currYear = listDate.get(idxCurrent[i]);
                while(currYear.date.plusYears(i+1).isBefore(t)){
                    // Do the work
                    try{
                        //printDegree(currYear.node, DMap.getInDegree(currYear.node), i+1);
                        printGraphlets(currYear.node, currYear, i+1);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    idxCurrent[i]++;
                    currYear = listDate.get(idxCurrent[i]);
                }
            }
            */
            InfoDate currYear = listDate.get(0);
            while(currYear.date.plusMonths(1).isBefore(t)){
                // Do the work
                try{
                    //printDegree(currYear.node, DMap.getInDegree(currYear.node), i+1);
                    printGraphlets(currYear.node, currYear, 1);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                listDate.remove(0);
                if (listDate.size() == 0){
                    break;
                }
                currYear = listDate.get(0);
            }
        }
        /*
        if(log2(degree1) % 1 == 0 && degree1 > 1){
            printResult(v1, degree1);
        }
        */
    
        count++;
        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            long[] graph_motif = graphlet2motif();
            System.out.println(Arrays.toString(graph_motif));
        }


    }
    public Boolean init(){
        try{
            String line = null ;
            resultFolder = projectPath + "/result/" + dataset;
            File folder = new File(resultFolder);
            if (!folder.exists()){
                folder.mkdir();
            }
            String path = resultFolder + "/degree.csv";
            //File f = new File(path);
            //if(!f.exists()){
            //    System.out.println("No Degree File ERROR");
            //    return false;
            //}
            /*
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

            */
            // Result 폴더 만들어주기
            for(int i = num_pow ; i < maxDegree; i = i * num_pow){
                path = resultFolder + "/" + String.format("%04d", i) + ".csv";
                CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                cw.writeNext(features);
                cw.flush();
                cw.close();
            }

            // time 폴더 만들어주기
            path = resultFolder + "/time/" ;
            File file = new File(path);
            if(!file.exists()){
                file.mkdir();
            }
            for(int i = 0; i < yearInterval+1; i++){
                path = resultFolder + "/time/" + String.format("%02d", i) +".csv";
                try{
                    CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                    String[] header = {
                        "vid",
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
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void printDegree(int v, int degree, int year){
        String path = resultFolder + "/time/" + String.format("%02d", year) +".csv";
        try{
            CSVWriter cw = new CSVWriter(new FileWriter(new File(path), true));
            long[] graphlet = graphlets[v];
            String[] result = new String[graphlet.length + 2];
            float sum = 0;
            for(int i = 0; i < graphlet.length; i++){
                sum += graphlet[i];
            }
            result[0] = Integer.toString(v);
            result[1] = Integer.toString(degree);
            for(int i = 2; i < 2 + graphlet.length; i++){
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

    public void printGraphlets(int v, InfoDate currYear, int year){
        String path = resultFolder + "/time/" + String.format("%02d", year) +".csv";
        try{
            
            CSVWriter cw = new CSVWriter(new FileWriter(new File(path), true));
            String[] result = new String[31];
            long[] graphlet = graphlets[v];
            result[0] = Integer.toString(v);
            for(int i = 0; i < graphlet.length; i++){
                result[i+1] = Long.toString(graphlet[i] - currYear.graphlets[i]);
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
        }
        
        // Validation
        for(int i = 0 ; i < DirectedMotifType.values().length; i++){
            motifs[i] /= 3;
        }       
        return motifs;
    }


    public void update(int v1, int v2, int v3){
        int V1IN = 0, V2IN = 0, V3IN = 0, V1OUT = 0, V2OUT = 0, V3OUT = 0;
        IntSet V1OUTSET = DMap.getOutNeighbors(v1);
        IntSet V2OUTSET = DMap.getOutNeighbors(v2);
        IntSet V3OUTSET = DMap.getOutNeighbors(v3);

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
                    if(V1IN==2){
                        graphlets[v1][DirectedGraphletType.m7_2.ordinal()]++;
                        if(V2IN == 1){
                            graphlets[v2][DirectedGraphletType.m7_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_3.ordinal()]++;
                        }
                        else {
                            graphlets[v2][DirectedGraphletType.m7_3.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_1.ordinal()]++;
                        }
                    }
                    else if(V2IN == 2){
                        graphlets[v2][DirectedGraphletType.m7_2.ordinal()]++;
                        if(V1IN == 1){
                            graphlets[v1][DirectedGraphletType.m7_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_3.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m7_3.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_1.ordinal()]++;
                        }
                    }
                    else {
                        graphlets[v3][DirectedGraphletType.m7_2.ordinal()]++;
                        if(V1IN == 1){
                            graphlets[v1][DirectedGraphletType.m7_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_2.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m7_2.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m7_1.ordinal()]++;
                        }
                    }
                }
                else {
                    if(V1OUT==2){
                        graphlets[v1][DirectedGraphletType.m3_2.ordinal()]++;
                        if(V2OUT == 1){
                            graphlets[v2][DirectedGraphletType.m3_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_3.ordinal()]++;
                        }
                        else {
                            graphlets[v2][DirectedGraphletType.m3_3.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_1.ordinal()]++;
                        }
                    }
                    else if(V2OUT == 2){
                        graphlets[v2][DirectedGraphletType.m3_2.ordinal()]++;
                        if(V1OUT == 1){
                            graphlets[v1][DirectedGraphletType.m3_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_3.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m3_3.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_1.ordinal()]++;
                        }
                    }
                    else {
                        graphlets[v3][DirectedGraphletType.m3_2.ordinal()]++;
                        if(V1OUT == 1){
                            graphlets[v1][DirectedGraphletType.m3_1.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_2.ordinal()]++;
                        }
                        else {
                            graphlets[v1][DirectedGraphletType.m3_2.ordinal()]++;
                            graphlets[v3][DirectedGraphletType.m3_1.ordinal()]++;
                        }
                    }
                }
            }
            else if (maxOutDegree == 2){
                if (V1OUT == 2){
                    graphlets[v1][DirectedGraphletType.m1_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m1_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m1_1.ordinal()]++;
                }
                else if (V2OUT == 2){
                    graphlets[v2][DirectedGraphletType.m1_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m1_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m1_1.ordinal()]++;
                }
                else {
                    graphlets[v3][DirectedGraphletType.m1_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m1_1.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m1_1.ordinal()]++;
                }
            }
            else if (maxInDegree == 2){
                if (V1IN == 2){
                    graphlets[v1][DirectedGraphletType.m4_2.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m4_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m4_1.ordinal()]++;
                }
                else if (V2IN == 2){
                    graphlets[v2][DirectedGraphletType.m4_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m4_1.ordinal()]++;
                    graphlets[v3][DirectedGraphletType.m4_1.ordinal()]++;
                }
                else {
                    graphlets[v3][DirectedGraphletType.m4_2.ordinal()]++;
                    graphlets[v1][DirectedGraphletType.m4_1.ordinal()]++;
                    graphlets[v2][DirectedGraphletType.m4_1.ordinal()]++;
                }
            }
            else {
                if (V1IN + V1OUT == 2){
                    graphlets[v1][DirectedGraphletType.m2_2.ordinal()]++;
                    if (V2OUT == 1){
                        graphlets[v2][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m2_3.ordinal()]++;
                    }
                    else {
                        graphlets[v3][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m2_3.ordinal()]++;
                    }
                }
                else if (V2IN + V2OUT == 2){
                    graphlets[v2][DirectedGraphletType.m2_2.ordinal()]++;
                    if (V1OUT == 1){
                        graphlets[v1][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v3][DirectedGraphletType.m2_3.ordinal()]++;
                    }
                    else {
                        graphlets[v3][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v1][DirectedGraphletType.m2_3.ordinal()]++;
                    }
                }
                else {
                    graphlets[v3][DirectedGraphletType.m2_2.ordinal()]++;
                    if (V1OUT == 1){
                        graphlets[v1][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v2][DirectedGraphletType.m2_3.ordinal()]++;
                    }
                    else {
                        graphlets[v2][DirectedGraphletType.m2_1.ordinal()]++;
                        graphlets[v1][DirectedGraphletType.m2_3.ordinal()]++;
                    }
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
