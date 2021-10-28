import java.util.ArrayList;
import java.util.Random;


import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.Collections;

import java.io.*;
import java.lang.Math;
import com.opencsv.CSVWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class EdgeRoleSignalAndCentralityPredictionFeatures {
    DirectedNodeMap node_map; // Graph
    long[] current = new long[GraphletType.values().length]; 
    int count = 0;
    String projectPath = System.getProperty("user.dir");
    String resultFolder;
    String read_path;
    String dataset;
    BufferedWriter filewriter;
    CSVWriter cw;
    CSVWriter mw;
    CSVWriter[] degreeWriter = new CSVWriter[3];
    int maxDegree;
    int[] degreeList;
    long[] edgeRoleList = new long[DirectedEdgeRole.values().length];
    Long2ObjectOpenHashMap<long[]> edgeRoleMap = new Long2ObjectOpenHashMap<long[]>();
    Long2ObjectOpenHashMap<long[]> initialEdgeRoleMap = new Long2ObjectOpenHashMap<long[]>();
    int numEdges;
    LocalDate[] timestampList;
    DirectedEdge[] edgeList;
    String centrality;
    Long2DoubleOpenHashMap centralityMap = new Long2DoubleOpenHashMap();
    static double[] boundary_centrality;

    
    public EdgeRoleSignalAndCentralityPredictionFeatures(String read_path, String dataset, int numNodes, int maxDegree, int numEdges, String centrality){
        this.node_map = new DirectedNodeMap(numNodes);
        this.read_path = read_path;
        this.dataset = dataset;
        this.maxDegree = maxDegree;
        this.numEdges = numEdges;
        timestampList = new LocalDate[numEdges];
        edgeList = new DirectedEdge[numEdges];
        this.centrality = centrality;
    }
    
    
    public void run(){
        String line = null ;
        resultFolder = projectPath + "/../result/edge-analysis/" + dataset + "/";
        File folder = new File(resultFolder);
        if (!folder.exists()){
            folder.mkdir();
        }

        try{
            String centralityPath = System.getProperty("user.dir") + "/../data/centrality/" + dataset + "-" + centrality + ".csv";
            File f = new File(centralityPath);
            if(!f.exists()){
                System.out.println("No Centrality Files exists");
                return;
            }
            BufferedReader fr = new BufferedReader(new FileReader(f));
            
            ArrayList<Double> collection_centrality = new ArrayList<Double>();
            while((line = fr.readLine()) != null){
                String[] vid_centrality = line.replace("\"", "").split(",");
                int src = Integer.parseInt(vid_centrality[0]);
                int dst = Integer.parseInt(vid_centrality[1]);
                long key = (((long)src) << 32) | (dst & 0xffffffffL);
                double centrality = Double.parseDouble(vid_centrality[2]);
                collection_centrality.add(centrality);
                centralityMap.put(key, centrality);
            }

            Collections.sort(collection_centrality);
            int[] boundary_idx = new int[]{(int)Math.round(collection_centrality.size() * 0.5), (int)Math.round(collection_centrality.size() * 0.7), (int)Math.round(collection_centrality.size() * 0.9), (int)Math.round(collection_centrality.size() * 0.95), (int)Math.round(collection_centrality.size() * 0.99)};
            boundary_centrality = new double[]{collection_centrality.get(boundary_idx[0]), collection_centrality.get(boundary_idx[1]), collection_centrality.get(boundary_idx[2]), collection_centrality.get(boundary_idx[3]), collection_centrality.get(boundary_idx[4])};
            System.out.println(Arrays.toString(boundary_centrality));

            String path = resultFolder + "/" + centrality +"/";
            folder = new File(path);
            if (!folder.exists()){
                folder.mkdir();
            }
            String[] features = {
                "m1_1", 
                "m2_1", "m2_2",
                "m3_1", "m3_2", "m3_3",
                "m4_1",
                "m5_1", "m5_2", "m5_3",
                "m6_1", "m6_2",
                "m7_1", "m7_2", "m7_3",
                "m8_1", "m8_2",
                "m9_1",
                "m10_1", "m10_2", "m10_3", "m10_4",
                "m11_1", "m11_2",
                "m12_1", "m12_2", "m12_3", "m12_4", "m12_5",
                "m13_1"
                };
            for(int i = 2, j = 0 ; i <= maxDegree * 2; i = i * 2, j++){
                path = resultFolder + "/" + centrality + "/" + String.format("%04d", j) + ".csv";
                CSVWriter cw = new CSVWriter(new FileWriter(new File(path), false));
                cw.writeNext(features);
                cw.flush();
                cw.close();
            }

            String[] result_name = new String[]{"edge-indegree2", "edge-indegree4", "edge-indegree8"};
            for(int i = 0 ; i < 3; i++){
                String name = result_name[i];
                String graphlet_role_path = resultFolder + name + ".csv";
                System.out.println(graphlet_role_path);
                degreeWriter[i] = new CSVWriter(new FileWriter(new File(graphlet_role_path), false));
                String[] motif_header = {
                    "src", "dst", "num_nodes", "num_edges",
                    "m1_1", 
                    "m2_1", "m2_2",
                    "m3_1", "m3_2", "m3_3",
                    "m4_1",
                    "m5_1", "m5_2", "m5_3",
                    "m6_1", "m6_2",
                    "m7_1", "m7_2", "m7_3",
                    "m8_1", "m8_2",
                    "m9_1",
                    "m10_1", "m10_2", "m10_3", "m10_4",
                    "m11_1", "m11_2",
                    "m12_1", "m12_2", "m12_3", "m12_4", "m12_5",
                    "m13_1",
                    "m1_1", 
                    "m2_1", "m2_2",
                    "m3_1", "m3_2", "m3_3",
                    "m4_1",
                    "m5_1", "m5_2", "m5_3",
                    "m6_1", "m6_2",
                    "m7_1", "m7_2", "m7_3",
                    "m8_1", "m8_2",
                    "m9_1",
                    "m10_1", "m10_2", "m10_3", "m10_4",
                    "m11_1", "m11_2",
                    "m12_1", "m12_2", "m12_3", "m12_4", "m12_5",
                    "m13_1"
                };
                degreeWriter[i].writeNext(motif_header);
                degreeWriter[i].flush();
            }


            File read_file = new File(read_path);
            BufferedReader filereader = new BufferedReader(new FileReader(read_file));
            
            line = null;
            while((line = filereader.readLine()) != null){
                String[] l = line.split("\t");
                int v1 = Integer.parseInt(l[0]);
                int v2 = Integer.parseInt(l[1]);
                String[] time = l[2].split(" ");
                LocalDate t = LocalDate.parse(time[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                addEdge(v1, v2, t);
            }
            filereader.close();

            for(int i = 0; i < boundary_centrality.length+1; i++){
                CSVWriter cw = new CSVWriter(new FileWriter( resultFolder + "/" + centrality + "/" + String.format("%04d", i) + ".csv", true));
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

    public void addEdge(int v1, int v2, LocalDate timestamp) {
        
        if (node_map.contains(v1, v2) || v1 == v2){
            return;
        }

        node_map.addEdge(v1, v2);
        edgeList[count] = new DirectedEdge(v1, v2);
        timestampList[count] = timestamp;
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

        if (count % 1000 == 0){
            System.out.println("**********************");
            System.out.println("iter : " + count);
            System.out.println(Arrays.toString(edgeRoleList));
        }
        
        long edgeKey = (((long)v1) << 32) | (v2 & 0xffffffffL);
        if(!initialEdgeRoleMap.containsKey(edgeKey)){
            long[] role_list = deepCopy(edgeRoleMap.get(edgeKey));
            if(role_list == null){
                role_list = new long[30];
            }
            initialEdgeRoleMap.put(edgeKey, role_list);
        }

        int srcDegree = node_map.getInDegree(v1);
        int dstDegree = node_map.getInDegree(v2);
        int edgeDegree = srcDegree + dstDegree;
        switch(edgeDegree){
            case 2: 
                printIndegreeRole(v1, v2, 0); 
                addEdgeRoleInfo(v1, v2, 2);
                break;
            case 4: 
                printIndegreeRole(v1, v2, 1); 
                addEdgeRoleInfo(v1, v2, 4);
                break;
            case 8: 
                printIndegreeRole(v1, v2, 2); 
                addEdgeRoleInfo(v1, v2, 8);
                break;
            default: break;
        }

        for(int v3 : node_map.getOutNeighbors(v2)){
            int v3Degree = node_map.getInDegree(v3);
            int neighborEdgeDegree = dstDegree + v3Degree;
            switch(neighborEdgeDegree){
                case 2: 
                    printIndegreeRole(v2, v3, 0); 
                    addEdgeRoleInfo(v2, v3, 2);
                    break;
                case 4: 
                    printIndegreeRole(v2, v3, 1);
                    addEdgeRoleInfo(v2, v3, 4);
                    break;
                case 8: 
                    printIndegreeRole(v2, v3, 2); 
                    addEdgeRoleInfo(v2, v3, 8);
                    break;
                default: break;
            }
        }
        count++;
    }
    public void addEdgeRoleInfo(int src, int dst, int degree){
        long key = (((long)src) << 32) | (dst & 0xffffffffL);
        if (!edgeRoleMap.containsKey(key)){
            return;
        }
        long[] edge_role = edgeRoleMap.get(key);

        double edgeFutureCentrality = centralityMap.get(key);
        int class_id = boundary_centrality.length;
        for(int i = boundary_centrality.length-1; i >= 0 ; i--){
            if(edgeFutureCentrality <= boundary_centrality[i]){
                class_id = i;
            }
        }
        long role_key = (((long)class_id) << 32) | (degree & 0xffffffffL);
        
        long[] average_feature ;
        if(!node_map.graphlet_map.contains(role_key)){
            average_feature = new long[DirectedEdgeRole.values().length+1];
        }
        else average_feature = node_map.graphlet_map.get(role_key);

        for(int i = 0; i < DirectedEdgeRole.values().length; i++){
            average_feature[i] += edge_role[i];
        }
        average_feature[average_feature.length-1]++;
        node_map.graphlet_map.put(role_key, average_feature);
    }

    public void printIndegreeRole(int src, int dst, int idx){
        long key = (((long)src) << 32) | (dst & 0xffffffffL);
        if (!edgeRoleMap.containsKey(key)){
            return;
        }
        long[] role_list = edgeRoleMap.get(key);
        try{ 
            String[] write_line = new String[4 + DirectedEdgeRole.values().length * 2];
            write_line[0] = Integer.toString(src);
            write_line[1] = Integer.toString(dst);
            write_line[3] = Integer.toString(node_map.nodes.size());
            write_line[2] = Integer.toString(count);
            for(int i = 0; i < DirectedEdgeRole.values().length; i++){
                write_line[i+4] = Long.toString(role_list[i]);
            }
            double[] neighbor_info = getNeighborInfo(src, dst);
            for(int i = 0; i < DirectedEdgeRole.values().length; i++){
                write_line[i+34] = Double.toString(neighbor_info[i]);
            }
            degreeWriter[idx].writeNext(write_line);
            degreeWriter[idx].flush();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public double[] getNeighborInfo(int v1, int v2){
        double[] score = new double[30];
        int degree = node_map.getInDegree(v1) + node_map.getInDegree(v2);
        ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
        for(DirectedEdge edge : node_map.getAllEdges()){
            int src = edge.getSource();
            int dst = edge.getDestination();
            if(node_map.getInDegree(src) + node_map.getInDegree(dst) == degree){
                edges.add(edge);
            }
        }
        long[][] roles = new long[edges.size()][30];
        for(int i = 0 ; i < edges.size(); i++){
            DirectedEdge edge = edges.get(i);
            long key = (((long)edge.getSource()) << 32) | (edge.getDestination() & 0xffffffffL);
            long[] role = edgeRoleMap.get(key);
            if (role == null){
                return score;
            }
            for(int j = 0; j < 30; j++){
                roles[i][j] = role[j];
            }
        }
        long targetkey = (((long)v1) << 32) | (v2 & 0xffffffffL);
        long[] target_role = edgeRoleMap.get(targetkey);
        for(int i = 0 ; i < 30; i++){
            double avg = 0.0, stdev = 0.0;
            for(int j = 0; j < edges.size(); j++){
                avg += roles[j][i];
            }
            avg /= edges.size();
            for(int j = 0; j < edges.size(); j++) {
                stdev += Math.pow(roles[j][i] - avg, 2);
            }
            stdev = Math.sqrt(stdev);
            score[i] = (target_role[i] - avg) / (stdev + 1);
        }

        return score;
    }

    public void printIndegreeRoleRatio(int src, int dst, int idx){
        long key = (((long)src) << 32) | (dst & 0xffffffffL);
        if (!edgeRoleMap.containsKey(key)){
            return;
        }
        long[] role_list = edgeRoleMap.get(key);
        try{ 
            String[] write_line = new String[2 + DirectedEdgeRole.values().length];
            write_line[0] = Integer.toString(src);
            write_line[1] = Integer.toString(dst);
            write_line[2] = Integer.toString(count);
            long total = 0;
            for(int i = 0 ; i < DirectedEdgeRole.values().length; i++){
                total += role_list[i];
            }
            for(int i = 0; i < DirectedEdgeRole.values().length; i++){
                write_line[i+3] = Double.toString(role_list[i] * 1.0 / total);
            }
            degreeWriter[idx].writeNext(write_line);
            degreeWriter[idx].flush();
        }
        catch(Exception e){
            e.printStackTrace();
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
                    //motif m7
                    update(v1, v2, DirectedEdgeRole.e7_3);
                    update(v2, v3, DirectedEdgeRole.e7_1);
                    update(v3, v2, DirectedEdgeRole.e7_2);
                }
                else {
                    update(v1, v2, DirectedEdgeRole.e3_3);
                    update(v1, v3, DirectedEdgeRole.e3_1);
                    update(v3, v1, DirectedEdgeRole.e3_2);
                }
            }
            else if (maxOutDegree == 2){
                update(v1, v2, DirectedEdgeRole.e1_1);
                update(v1, v3, DirectedEdgeRole.e1_1);
            }
            else if (maxInDegree == 2){
                update(v3, v2, DirectedEdgeRole.e4_1);
                update(v1, v2, DirectedEdgeRole.e4_1);
            }
            else {
                if (V1IN == 1){
                    update(v1, v2, DirectedEdgeRole.e2_2);
                    update(v3, v1, DirectedEdgeRole.e2_1);
                }
                else {
                    update(v1, v2, DirectedEdgeRole.e2_1);
                    update(v2, v3, DirectedEdgeRole.e2_2);
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
            update(v1, v2, DirectedEdgeRole.e13_1);
            update(v2, v1, DirectedEdgeRole.e12_5, DirectedEdgeRole.e13_1);

            update(v1, v3, DirectedEdgeRole.e12_4, DirectedEdgeRole.e13_1);
            update(v3, v1, DirectedEdgeRole.e12_3, DirectedEdgeRole.e13_1);

            update(v2, v3, DirectedEdgeRole.e12_1, DirectedEdgeRole.e13_1);
            update(v3, v2, DirectedEdgeRole.e12_2, DirectedEdgeRole.e13_1);
        } 
        else if (numEdges == 4){
            //m8
            if (maxDegree == 4) {
                update(v1, v2, DirectedEdgeRole.e12_5);
    
                update(v1, v3, DirectedEdgeRole.e8_1, DirectedEdgeRole.e12_1);
                update(v3, v1, DirectedEdgeRole.e8_2, DirectedEdgeRole.e12_2);
    
                update(v2, v3, DirectedEdgeRole.e8_1, DirectedEdgeRole.e12_4);
                update(v3, v2, DirectedEdgeRole.e8_2, DirectedEdgeRole.e12_3);
            }
            else if (minInDegree == 0) {
                // m11
                update(v1, v2, DirectedEdgeRole.e12_2);
    
                update(v1, v3, DirectedEdgeRole.e11_2, DirectedEdgeRole.e12_3);
                update(v3, v1, DirectedEdgeRole.e11_2, DirectedEdgeRole.e12_4);
    
                update(v2, v3, DirectedEdgeRole.e11_1, DirectedEdgeRole.e12_5);
                update(v2, v1, DirectedEdgeRole.e11_1, DirectedEdgeRole.e12_1);
            }
            else if (minOutDegree == 0) {
                // m6
                update(v1, v2, DirectedEdgeRole.e12_4);
    
                update(v3, v1, DirectedEdgeRole.e6_2, DirectedEdgeRole.e12_5);
                update(v3, v2, DirectedEdgeRole.e6_1, DirectedEdgeRole.e12_1);

                update(v2, v3, DirectedEdgeRole.e6_1, DirectedEdgeRole.e12_2);
                update(v2, v1, DirectedEdgeRole.e6_2, DirectedEdgeRole.e12_3);
            }
            else {
                // m10
                if((V1IN + V1OUT) == 2){
                    update(v1, v2, DirectedEdgeRole.e12_1);
                    update(v1, v3, DirectedEdgeRole.e10_2, DirectedEdgeRole.e12_5);

                    update(v3, v2, DirectedEdgeRole.e10_4, DirectedEdgeRole.e12_4);
                    update(v2, v3, DirectedEdgeRole.e10_3, DirectedEdgeRole.e12_3);
                    
                    update(v2, v1, DirectedEdgeRole.e10_1, DirectedEdgeRole.e12_2);
                    
                }
                else {
                    update(v1, v2, DirectedEdgeRole.e12_3);
                    update(v1, v3, DirectedEdgeRole.e10_4, DirectedEdgeRole.e12_2);

                    update(v3, v2, DirectedEdgeRole.e10_1, DirectedEdgeRole.e12_5);
                    update(v3, v1, DirectedEdgeRole.e10_3, DirectedEdgeRole.e12_1);
                    
                    update(v2, v1, DirectedEdgeRole.e10_2, DirectedEdgeRole.e12_4);
                }
            }
        }
        else if (numEdges == 3){
            if (maxOutDegree == 1) {
                if (maxInDegree == 2) {
                    // m7
                    if (V1IN == 2){
                        update(v1, v2, DirectedEdgeRole.e8_2);
                        update(v2, v1, DirectedEdgeRole.e7_3, DirectedEdgeRole.e8_1);

                        update(v3, v1, DirectedEdgeRole.e7_2, DirectedEdgeRole.e8_1);
                        update(v1, v3, DirectedEdgeRole.e7_1, DirectedEdgeRole.e8_2);
                    }
                    else if (V2IN == 1){
                        update(v1, v2, DirectedEdgeRole.e11_1);
                        update(v1, v3, DirectedEdgeRole.e7_3, DirectedEdgeRole.e11_1);

                        update(v3, v2, DirectedEdgeRole.e7_1, DirectedEdgeRole.e11_2);
                        update(v2, v3, DirectedEdgeRole.e7_2, DirectedEdgeRole.e11_2);
                    }
                    else {
                        update(v1, v2, DirectedEdgeRole.e10_1);
                        update(v2, v3, DirectedEdgeRole.e7_3, DirectedEdgeRole.e10_2);

                        update(v3, v1, DirectedEdgeRole.e7_1, DirectedEdgeRole.e10_4);
                        update(v1, v3, DirectedEdgeRole.e7_2, DirectedEdgeRole.e10_3);
                    }
                }
                else {
                    //m9
                    update(v1, v2, DirectedEdgeRole.e10_3);
                    update(v2, v1, DirectedEdgeRole.e9_1, DirectedEdgeRole.e10_4);

                    update(v1, v3, DirectedEdgeRole.e9_1, DirectedEdgeRole.e10_1);
                    update(v3, v2, DirectedEdgeRole.e9_1, DirectedEdgeRole.e10_2);
                }
            }
            else {
                if (maxInDegree == 2) {
                    //m5
                    if (V1IN == 1 ){
                        update(v1, v2, DirectedEdgeRole.e6_1);
                        update(v2, v1, DirectedEdgeRole.e5_1, DirectedEdgeRole.e6_1);

                        update(v2, v3, DirectedEdgeRole.e5_2, DirectedEdgeRole.e6_2);
                        update(v1, v3, DirectedEdgeRole.e5_3, DirectedEdgeRole.e6_2);
                    }
                    else{
                        if (V2OUT == 2){
                            update(v1, v2, DirectedEdgeRole.e10_4);
                            update(v2, v1, DirectedEdgeRole.e5_2, DirectedEdgeRole.e10_3);

                            update(v2, v3, DirectedEdgeRole.e5_1, DirectedEdgeRole.e10_1);
                            update(v3, v1, DirectedEdgeRole.e5_3, DirectedEdgeRole.e10_2);
                        }
                        else {
                            update(v1, v2, DirectedEdgeRole.e11_2);
                            update(v2, v1, DirectedEdgeRole.e5_3, DirectedEdgeRole.e11_2);

                            update(v3, v1, DirectedEdgeRole.e5_2, DirectedEdgeRole.e11_1);
                            update(v3, v2, DirectedEdgeRole.e5_1, DirectedEdgeRole.e11_1);
                        }
                    }
                }
                else {
                    //m3
                    if (V1OUT == 1){
                        update(v1, v2, DirectedEdgeRole.e6_2);
                        update(v1, v3, DirectedEdgeRole.e3_2, DirectedEdgeRole.e6_1);

                        update(v3, v1, DirectedEdgeRole.e3_1, DirectedEdgeRole.e6_1);
                        update(v3, v2, DirectedEdgeRole.e3_3, DirectedEdgeRole.e6_2);
                    }
                    else {
                        if (V2OUT == 2){
                            update(v1, v2, DirectedEdgeRole.e8_1);
                            update(v2, v1, DirectedEdgeRole.e3_3, DirectedEdgeRole.e8_2);

                            update(v2, v3, DirectedEdgeRole.e3_1, DirectedEdgeRole.e8_2);
                            update(v3, v2, DirectedEdgeRole.e3_2, DirectedEdgeRole.e8_1);
                        }
                        else {
                            update(v1, v2, DirectedEdgeRole.e10_2);
                            update(v3, v1, DirectedEdgeRole.e3_3, DirectedEdgeRole.e10_1);

                            update(v2, v3, DirectedEdgeRole.e3_2, DirectedEdgeRole.e10_4);
                            update(v3, v2, DirectedEdgeRole.e3_1, DirectedEdgeRole.e10_3);
                        }
                    }
                }
            }
        }
        else {
            if (maxOutDegree == 2) {
                //m1
                if (V2OUT == 2){
                    update(v1, v2, DirectedEdgeRole.e3_2);

                    update(v2, v1, DirectedEdgeRole.e1_1, DirectedEdgeRole.e3_1);
                    update(v2, v3, DirectedEdgeRole.e1_1, DirectedEdgeRole.e3_3);
                }
                else {
                    update(v1, v2, DirectedEdgeRole.e5_3);

                    update(v3, v1, DirectedEdgeRole.e1_1, DirectedEdgeRole.e5_1);
                    update(v3, v2, DirectedEdgeRole.e1_1, DirectedEdgeRole.e5_2);
                }
            }
            else if (maxInDegree == 2) {
                //m4
                if (V1IN == 2){
                    update(v1, v2, DirectedEdgeRole.e7_1);

                    update(v2, v1, DirectedEdgeRole.e4_1, DirectedEdgeRole.e7_2);
                    update(v3, v1, DirectedEdgeRole.e4_1, DirectedEdgeRole.e7_3);
                }
                else {
                    update(v1, v2, DirectedEdgeRole.e5_1);

                    update(v1, v3, DirectedEdgeRole.e4_1, DirectedEdgeRole.e5_2);
                    update(v2, v3, DirectedEdgeRole.e4_1, DirectedEdgeRole.e5_3);
                }
            }
            else {
                //m2
                if (V1IN+V1OUT == 2){
                    update(v1, v2, DirectedEdgeRole.e3_1);

                    update(v1, v3, DirectedEdgeRole.e2_2, DirectedEdgeRole.e3_3);
                    update(v2, v1, DirectedEdgeRole.e2_1, DirectedEdgeRole.e3_2);
                }
                else if (V1OUT == 1){
                    update(v1, v2, DirectedEdgeRole.e5_2);

                    update(v1, v3, DirectedEdgeRole.e2_1, DirectedEdgeRole.e5_1);
                    update(v3, v2, DirectedEdgeRole.e2_2, DirectedEdgeRole.e5_3);
                }
                else {
                    if (V2IN == 0){
                        update(v1, v2, DirectedEdgeRole.e9_1);

                        update(v2, v3, DirectedEdgeRole.e2_1, DirectedEdgeRole.e9_1);
                        update(v3, v1, DirectedEdgeRole.e2_2, DirectedEdgeRole.e9_1);
                    }
                    else {
                        update(v1, v2, DirectedEdgeRole.e7_2);

                        update(v2, v1, DirectedEdgeRole.e2_2, DirectedEdgeRole.e7_1);
                        update(v3, v2, DirectedEdgeRole.e2_1, DirectedEdgeRole.e7_3);
                    }
                }
                    
            }
        }
    }
    public static double log2(double x){
        return Math.log(x) / Math.log(2);
    }

    public void update(int src, int dst, DirectedEdgeRole role){
        long key = (((long)src) << 32) | (dst & 0xffffffffL);
        long[] role_list;
        role_list = edgeRoleMap.get(key);
        if (role_list == null){
            role_list = new long[DirectedEdgeRole.values().length];
        }
        role_list[role.ordinal()]++;
        edgeRoleList[role.ordinal()]++;
        edgeRoleMap.put(key, role_list);
    }

    public void update(int src, int dst, DirectedEdgeRole before, DirectedEdgeRole after){
        long key = (((long)src) << 32) | (dst & 0xffffffffL);
        long[] role_list = edgeRoleMap.get(key);
        if (role_list == null){
            role_list = new long[DirectedEdgeRole.values().length];
        }
        role_list[before.ordinal()]--;
        role_list[after.ordinal()]++;
        edgeRoleList[before.ordinal()]--;
        edgeRoleList[after.ordinal()]++;
        edgeRoleMap.put(key, role_list);
    }

    public long[] countMotif(){
        long[] motifs = new long[GraphletType.values().length];
        
        for (DirectedEdge e : node_map.getAllEdges()){
            int u = e.getSource();
            int v = e.getDestination();
            for(int w : node_map.getNeighbors(u)){
                if (w == v){
                    continue;
                }
                motifs[getMotifType(u, v, w).ordinal()]++;
            }
            for(int w : node_map.getNeighbors(v)){
                if (w == u){
                    continue;
                }
                if (!node_map.isConnected(w, u)){
                    motifs[getMotifType(u, v, w).ordinal()]++;
                }
            }
        }
        motifs[GraphletType.m1.ordinal()] /= 2;
        motifs[GraphletType.m2.ordinal()] /= 2;
        motifs[GraphletType.m3.ordinal()] /= 3;
        motifs[GraphletType.m4.ordinal()] /= 2;
        motifs[GraphletType.m5.ordinal()] /= 3;
        motifs[GraphletType.m6.ordinal()] /= 4;
        motifs[GraphletType.m7.ordinal()] /= 3;
        motifs[GraphletType.m8.ordinal()] /= 4;
        motifs[GraphletType.m9.ordinal()] /= 3;
        motifs[GraphletType.m10.ordinal()] /=4;
        motifs[GraphletType.m11.ordinal()] /=4;
        motifs[GraphletType.m12.ordinal()] /=5;
        motifs[GraphletType.m13.ordinal()] /=6;
        return motifs;
    }


    public GraphletType getMotifType(int v1, int v2, int v3){
        int V1IN = 0, V2IN = 0, V3IN = 0, V1OUT = 0, V2OUT = 0, V3OUT = 0;
        IntSet V1OUTSET = node_map.getOutNeighbors(v1);
        IntSet V2OUTSET = node_map.getOutNeighbors(v2);
        IntSet V3OUTSET = node_map.getOutNeighbors(v3);
        if (V1OUTSET.contains(v2)){
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

    public static long[] deepCopy(long[] src){
        if (src == null) return null;
        long[] dst = new long[src.length];
        dst = src.clone();
        return dst;
    }
}
