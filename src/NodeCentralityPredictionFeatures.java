import java.util.HashSet;
import it.unimi.dsi.fastutil.ints.*;
import java.io.*;
import java.lang.Math;
import com.opencsv.CSVWriter;


public class NodeCentralityPredictionFeatures {
    DirectedNodeMap DMap;
    NodeMap UDMap;
    long[] current = new long[GraphletType.values().length]; 
    int count = 0;
    String projectPath = System.getProperty("user.dir");
    String resultFolder;
    String read_path;
    String dataset;
    BufferedWriter filewriter;
    CSVWriter cw;
    CSVWriter mw;
    long[][] nodeRole;
    long[][] NPP;

    int numNodes;
    int maxDegree;
    int[] degreeList;
    int numEdges;

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
        "line_isolated",
        };

    HashSet<String> transition = new HashSet<String>();

    public NodeCentralityPredictionFeatures(String read_path, String dataset, int numNodes, int maxDegree, int numEdges){
        this.DMap = new DirectedNodeMap(numNodes);
        this.UDMap = new NodeMap(numNodes);
        this.read_path = read_path;
        this.dataset = dataset;
        this.nodeRole = new long[numNodes][NodeRoleType.values().length];
        this.NPP = new long[numNodes][NPPType.values().length];
        this.numNodes = numNodes;
        this.maxDegree = maxDegree;
        this.numEdges = numEdges;
    }
    
    
    public void run(){
        if (!init()){
            return;
        }
        try{
            File read_file = new File(read_path);
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            String line = null;
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

    public void addEdge(int v1, int v2) {
        
        if (DMap.contains(v1, v2) || v1 == v2){
            return;
        }

        DMap.addEdge(v1, v2);
        UDMap.addEdge(v1, v2);
        UDMap.addEdge(v2, v1);

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
        
        // Node Prominance Profile Analysis
        visited = new IntOpenHashSet();
        for(int v3 : UDMap.getNeighbors(v1)){
            if (v2 == v3) continue;
            visited.add(v3);
            if (UDMap.contains(v2, v3)){
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

        // Disconnected part of Node Prominance Profile of node v2
        int numTriangle = 0; // Total number of triangles with v2
        NPP[v2][NPPType.LINE_CONNECTED.ordinal()] = 0;
        NPP[v2][NPPType.LINE_ISOLATED.ordinal()] = 0;
        for (int v3 : UDMap.getNeighbors(v2)){
            int triangleV2V3 = 0;
            for(int v4 : UDMap.getNeighbors(v2)){
                if(UDMap.contains(v3, v4) && v3 != v4){
                    triangleV2V3++;
                }
            }
            // # of connected nodes with v2 and v3 = neighbors(v2) + neighbors(v3) - triangle(v2, v3)
            int numConnected = UDMap.getNeighbors(v2).size() + UDMap.getNeighbors(v3).size() - triangleV2V3; 

            // Position 1 (Line_connected) of NPP += disconnected nodes with (v2, v3) 
            NPP[v2][NPPType.LINE_CONNECTED.ordinal()] += (DMap.nodes.size() - numConnected);
            numTriangle += triangleV2V3;

            if (visited.contains(v3)) continue;
            if (v1 == v3) continue;
            NPP[v1][NPPType.PATH_ONE.ordinal()]++;
            NPP[v2][NPPType.PATH_TWO.ordinal()]++;
            NPP[v3][NPPType.PATH_ONE.ordinal()]++;
        }
        numTriangle /= 2;
        // Position 2 (Line_isolated) of NPP = # of edges - degree of v2 - num triangles
        NPP[v2][NPPType.LINE_ISOLATED.ordinal()] = count - UDMap.getDegree(v2) - numTriangle;
        
        int degree2 = DMap.getInDegree(v2);
        
        if(degree2 == 2 || degree2 == 4 || degree2 == 8){
            printResult(v2, degree2);
        }

        count++;
        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
        }


    }
    public Boolean init(){
        try{
            String line = null ;
            resultFolder = projectPath + "/../result/node-analysis/prediction/" + dataset +"/";
            File folder = new File(resultFolder);
            if (!folder.exists()){
                folder.mkdir();
            }
            String path = resultFolder + "/degree.csv";

            for(int i = 2 ; i <=8 ; i = i * 2){
                path = resultFolder + "/" +  String.format("%04d", i) + ".csv";
                CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                cw.writeNext(features);
                cw.flush();
                cw.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void printResult(int v, int degree){
        String path = resultFolder + String.format("%04d", degree) + ".csv";
        try{
            CSVWriter cw = new CSVWriter(new FileWriter(new File(path), true));
            long[] graphlet = nodeRole[v];
            long[] nodePP = NPP[v];
            String[] result = new String[features.length];
            result[0] = Integer.toString(v);
            result[1] = Integer.toString(count);
            result[2] = Integer.toString(DMap.nodes.size());
            for(int i = 3; i < 3 + graphlet.length; i++){
                result[i] = Long.toString(graphlet[i-3]);
            }
            double[] neighborInfo = getNeighborInfo(v, degree);
            for(int i = 0 ; i < neighborInfo.length; i++){
                result[i + graphlet.length + 3] = Double.toString(neighborInfo[i]);
            }

            for(int i = 0; i < nodePP.length; i++){
                result[i + graphlet.length + neighborInfo.length + 3] = Long.toString(nodePP[i]);
            }
            
            cw.writeNext(result);
            cw.flush();
            cw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public double[] getNeighborInfo(int v, int degree){
        double[] score = new double[30];
         
        IntArrayList nodes = new IntArrayList();
        for(int node : DMap.nodes){
            if (DMap.getInDegree(node) == degree){
                nodes.add(node);
            }
        }
        long[][] degree_graphlet = new long[nodes.size()][30];
        for(int i = 0 ; i <nodes.size(); i++){
            for(int j = 0; j < 30; j++){
                degree_graphlet[i][j] = nodeRole[nodes.getInt(i)][j];
            }
        }

        for(int i = 0 ; i < 30; i++){
            double avg = 0.0, stdev = 0.0;
            for(int j = 0; j < nodes.size(); j++){
                avg += degree_graphlet[j][i];
            }
            avg /= nodes.size();
            for(int j = 0; j < nodes.size(); j++) {
                stdev += Math.pow(degree_graphlet[j][i] - avg, 2);
            }
            stdev = Math.sqrt(stdev);
            score[i] = (nodeRole[v][i] - avg) / (stdev + 1);
        }

        return score;
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
                        nodeRole[v1][NodeRoleType.m7_2.ordinal()]++;
                        if(V2IN == 1){
                            nodeRole[v2][NodeRoleType.m7_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_3.ordinal()]++;
                        }
                        else {
                            nodeRole[v2][NodeRoleType.m7_3.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_1.ordinal()]++;
                        }
                    }
                    else if(V2IN == 2){
                        nodeRole[v2][NodeRoleType.m7_2.ordinal()]++;
                        if(V1IN == 1){
                            nodeRole[v1][NodeRoleType.m7_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_3.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m7_3.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_1.ordinal()]++;
                        }
                    }
                    else {
                        nodeRole[v3][NodeRoleType.m7_2.ordinal()]++;
                        if(V1IN == 1){
                            nodeRole[v1][NodeRoleType.m7_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_2.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m7_2.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m7_1.ordinal()]++;
                        }
                    }
                }
                else {
                    if(V1OUT==2){
                        nodeRole[v1][NodeRoleType.m3_2.ordinal()]++;
                        if(V2OUT == 1){
                            nodeRole[v2][NodeRoleType.m3_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_3.ordinal()]++;
                        }
                        else {
                            nodeRole[v2][NodeRoleType.m3_3.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_1.ordinal()]++;
                        }
                    }
                    else if(V2OUT == 2){
                        nodeRole[v2][NodeRoleType.m3_2.ordinal()]++;
                        if(V1OUT == 1){
                            nodeRole[v1][NodeRoleType.m3_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_3.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m3_3.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_1.ordinal()]++;
                        }
                    }
                    else {
                        nodeRole[v3][NodeRoleType.m3_2.ordinal()]++;
                        if(V1OUT == 1){
                            nodeRole[v1][NodeRoleType.m3_1.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_2.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m3_2.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m3_1.ordinal()]++;
                        }
                    }
                }
            }
            else if (maxOutDegree == 2){
                if (V1OUT == 2){
                    nodeRole[v1][NodeRoleType.m1_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m1_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m1_1.ordinal()]++;
                }
                else if (V2OUT == 2){
                    nodeRole[v2][NodeRoleType.m1_2.ordinal()]++;
                    nodeRole[v1][NodeRoleType.m1_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m1_1.ordinal()]++;
                }
                else {
                    nodeRole[v3][NodeRoleType.m1_2.ordinal()]++;
                    nodeRole[v1][NodeRoleType.m1_1.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m1_1.ordinal()]++;
                }
            }
            else if (maxInDegree == 2){
                if (V1IN == 2){
                    nodeRole[v1][NodeRoleType.m4_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m4_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m4_1.ordinal()]++;
                }
                else if (V2IN == 2){
                    nodeRole[v2][NodeRoleType.m4_2.ordinal()]++;
                    nodeRole[v1][NodeRoleType.m4_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m4_1.ordinal()]++;
                }
                else {
                    nodeRole[v3][NodeRoleType.m4_2.ordinal()]++;
                    nodeRole[v1][NodeRoleType.m4_1.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m4_1.ordinal()]++;
                }
            }
            else {
                if (V1IN + V1OUT == 2){
                    nodeRole[v1][NodeRoleType.m2_2.ordinal()]++;
                    if (V2OUT == 1){
                        nodeRole[v2][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m2_3.ordinal()]++;
                    }
                    else {
                        nodeRole[v3][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m2_3.ordinal()]++;
                    }
                }
                else if (V2IN + V2OUT == 2){
                    nodeRole[v2][NodeRoleType.m2_2.ordinal()]++;
                    if (V1OUT == 1){
                        nodeRole[v1][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m2_3.ordinal()]++;
                    }
                    else {
                        nodeRole[v3][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v1][NodeRoleType.m2_3.ordinal()]++;
                    }
                }
                else {
                    nodeRole[v3][NodeRoleType.m2_2.ordinal()]++;
                    if (V1OUT == 1){
                        nodeRole[v1][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m2_3.ordinal()]++;
                    }
                    else {
                        nodeRole[v2][NodeRoleType.m2_1.ordinal()]++;
                        nodeRole[v1][NodeRoleType.m2_3.ordinal()]++;
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
            nodeRole[v1][NodeRoleType.m12_3.ordinal()]--;
            nodeRole[v2][NodeRoleType.m12_1.ordinal()]--;
            nodeRole[v3][NodeRoleType.m12_2.ordinal()]--;

            nodeRole[v1][NodeRoleType.m13_1.ordinal()]++;
            nodeRole[v2][NodeRoleType.m13_1.ordinal()]++;
            nodeRole[v3][NodeRoleType.m13_1.ordinal()]++;

        } 
        else if (numEdges == 4){
            //m8
            if (maxDegree == 4) {
                nodeRole[v1][NodeRoleType.m8_1.ordinal()]--;
                nodeRole[v1][NodeRoleType.m12_1.ordinal()]++;

                nodeRole[v2][NodeRoleType.m8_1.ordinal()]--;
                nodeRole[v2][NodeRoleType.m12_3.ordinal()]++;

                nodeRole[v3][NodeRoleType.m8_2.ordinal()]--;
                nodeRole[v3][NodeRoleType.m12_2.ordinal()]++;
            }
            else if (minInDegree == 0) {
                // m11
                nodeRole[v1][NodeRoleType.m11_2.ordinal()]--;
                nodeRole[v1][NodeRoleType.m12_2.ordinal()]++;
                
                nodeRole[v2][NodeRoleType.m11_1.ordinal()]--;
                nodeRole[v2][NodeRoleType.m12_1.ordinal()]++;

                nodeRole[v3][NodeRoleType.m11_2.ordinal()]--;
                nodeRole[v3][NodeRoleType.m12_3.ordinal()]++;
            }
            else if (minOutDegree == 0) {
                // m6
                nodeRole[v1][NodeRoleType.m6_2.ordinal()]--;
                nodeRole[v1][NodeRoleType.m12_3.ordinal()]++;

                nodeRole[v2][NodeRoleType.m6_1.ordinal()]--;
                nodeRole[v2][NodeRoleType.m12_2.ordinal()]++; 

                nodeRole[v3][NodeRoleType.m6_1.ordinal()]--;
                nodeRole[v3][NodeRoleType.m12_1.ordinal()]++;
            }
            else {
                // m10
                if((V1IN + V1OUT) == 2){
                    nodeRole[v1][NodeRoleType.m10_1.ordinal()]--;
                    nodeRole[v1][NodeRoleType.m12_1.ordinal()]++;

                    nodeRole[v2][NodeRoleType.m10_2.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m12_2.ordinal()]++;

                    nodeRole[v3][NodeRoleType.m10_3.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m12_3.ordinal()]++;
                }
                else {
                    nodeRole[v1][NodeRoleType.m10_3.ordinal()]--;
                    nodeRole[v1][NodeRoleType.m12_2.ordinal()]++;

                    nodeRole[v2][NodeRoleType.m10_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m12_3.ordinal()]++;

                    nodeRole[v3][NodeRoleType.m10_2.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m12_1.ordinal()]++;
                }

            }
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) {
                    // m7
                    if (V1IN == 2){
                        nodeRole[v1][NodeRoleType.m7_2.ordinal()]--;
                        nodeRole[v1][NodeRoleType.m8_2.ordinal()]++;

                        nodeRole[v2][NodeRoleType.m7_3.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m8_1.ordinal()]++;

                        nodeRole[v3][NodeRoleType.m7_1.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m8_1.ordinal()]++;
                    }
                    else if (V2IN == 1){
                        nodeRole[v1][NodeRoleType.m7_3.ordinal()]--;
                        nodeRole[v1][NodeRoleType.m11_1.ordinal()]++;

                        nodeRole[v2][NodeRoleType.m7_1.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m11_2.ordinal()]++;

                        nodeRole[v3][NodeRoleType.m7_2.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m11_2.ordinal()]++;
                    }
                    else {
                        nodeRole[v1][NodeRoleType.m7_1.ordinal()]--;
                        nodeRole[v1][NodeRoleType.m10_2.ordinal()]++;

                        nodeRole[v2][NodeRoleType.m7_3.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m10_1.ordinal()]++;

                        nodeRole[v3][NodeRoleType.m7_2.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m10_3.ordinal()]++;
                    }
                }
                else {
                    //m9
                    nodeRole[v1][NodeRoleType.m9_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m9_1.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m9_1.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m10_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m10_3.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m10_1.ordinal()]++;
                    
                }
            }
            else {
                if (maxInDegree == 2) {
                    //m5
                    if (V1IN == 1 ){
                        nodeRole[v1][NodeRoleType.m5_1.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m5_2.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m5_3.ordinal()]--;

                        nodeRole[v1][NodeRoleType.m6_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m6_1.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m6_2.ordinal()]++;
                    }
                    else{
                        if (V2OUT == 2){
                            nodeRole[v1][NodeRoleType.m5_3.ordinal()]--;
                            nodeRole[v2][NodeRoleType.m5_2.ordinal()]--;
                            nodeRole[v3][NodeRoleType.m5_1.ordinal()]--;
    
                            nodeRole[v1][NodeRoleType.m10_3.ordinal()]++;
                            nodeRole[v2][NodeRoleType.m10_2.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m10_1.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m5_3.ordinal()]--;
                            nodeRole[v2][NodeRoleType.m5_1.ordinal()]--;
                            nodeRole[v3][NodeRoleType.m5_2.ordinal()]--;
    
                            nodeRole[v1][NodeRoleType.m11_2.ordinal()]++;
                            nodeRole[v2][NodeRoleType.m11_2.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m11_1.ordinal()]++;
                        }
                    }
                }
                else {
                    //m3
                    if (V1OUT == 1){
                        nodeRole[v1][NodeRoleType.m3_1.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m3_3.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m3_2.ordinal()]--;

                        nodeRole[v1][NodeRoleType.m6_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m6_2.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m6_1.ordinal()]++;
                    }
                    else {
                        if (V2OUT == 2){
                            nodeRole[v1][NodeRoleType.m3_3.ordinal()]--;
                            nodeRole[v2][NodeRoleType.m3_2.ordinal()]--;
                            nodeRole[v3][NodeRoleType.m3_1.ordinal()]--;
    
                            nodeRole[v1][NodeRoleType.m8_1.ordinal()]++;
                            nodeRole[v2][NodeRoleType.m8_2.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m8_1.ordinal()]++;
                        }
                        else {
                            nodeRole[v1][NodeRoleType.m3_3.ordinal()]--;
                            nodeRole[v2][NodeRoleType.m3_1.ordinal()]--;
                            nodeRole[v3][NodeRoleType.m3_2.ordinal()]--;
    
                            nodeRole[v1][NodeRoleType.m10_1.ordinal()]++;
                            nodeRole[v2][NodeRoleType.m10_3.ordinal()]++;
                            nodeRole[v3][NodeRoleType.m10_2.ordinal()]++;
                        }
                    }
                }
            }
        }
        else {
            if (maxOutDegree == 2) {
                //m1
                if (V2OUT == 2){
                    nodeRole[v1][NodeRoleType.m1_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m1_2.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m1_1.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m3_1.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m3_2.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m3_3.ordinal()]++;
                }
                else {
                    nodeRole[v1][NodeRoleType.m1_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m1_1.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m1_2.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m5_1.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m5_3.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m5_2.ordinal()]++;
                }
            }
            else if (maxInDegree == 2) {
                //m4
                if (V1IN == 2){
                    nodeRole[v1][NodeRoleType.m4_2.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m4_1.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m4_1.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m7_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m7_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m7_3.ordinal()]++;
                }
                else {
                    nodeRole[v1][NodeRoleType.m4_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m4_1.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m4_2.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m5_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m5_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m5_3.ordinal()]++;
                }
            }
            else {
                //m2
                if (V1IN+V1OUT == 2){
                    nodeRole[v1][NodeRoleType.m2_2.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m2_1.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m2_3.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m3_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m3_1.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m3_3.ordinal()]++;
                }
                else if (V1OUT == 1){
                    nodeRole[v1][NodeRoleType.m2_1.ordinal()]--;
                    nodeRole[v2][NodeRoleType.m2_3.ordinal()]--;
                    nodeRole[v3][NodeRoleType.m2_2.ordinal()]--;

                    nodeRole[v1][NodeRoleType.m5_2.ordinal()]++;
                    nodeRole[v2][NodeRoleType.m5_3.ordinal()]++;
                    nodeRole[v3][NodeRoleType.m5_1.ordinal()]++;
                }
                else {
                    if (V2IN == 0){
                        nodeRole[v1][NodeRoleType.m2_3.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m2_1.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m2_2.ordinal()]--;
    
                        nodeRole[v1][NodeRoleType.m9_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m9_1.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m9_1.ordinal()]++;
                    }
                    else {
                        nodeRole[v1][NodeRoleType.m2_3.ordinal()]--;
                        nodeRole[v2][NodeRoleType.m2_2.ordinal()]--;
                        nodeRole[v3][NodeRoleType.m2_1.ordinal()]--;
    
                        nodeRole[v1][NodeRoleType.m7_1.ordinal()]++;
                        nodeRole[v2][NodeRoleType.m7_2.ordinal()]++;
                        nodeRole[v3][NodeRoleType.m7_3.ordinal()]++;
                    }
                }
            }
        }
    }
}
