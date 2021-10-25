package src;

import gnu.trove.map.hash.*;
import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.*;

public class DirectedNodeMap {
    public IntSet[] InNeighbors;
    public IntSet[] OutNeighbors;
    public IntSet[] Neighbors;
    public THashMap<Long, long[]> graphlet_map;
    public IntSet nodes;
    public ArrayList<DirectedEdge> edgeList;
    private int numNodes;
    public int[] arrivalTime;
    public int numEdges;

    public DirectedNodeMap(int numNodes) {
        this.InNeighbors = new IntSet[numNodes];
        this.OutNeighbors = new IntSet[numNodes];
        this.Neighbors = new IntSet[numNodes];
        for(int i = 0; i < numNodes; i++){
            this.InNeighbors[i] = new IntOpenHashSet();
            this.OutNeighbors[i] = new IntOpenHashSet();
            this.Neighbors[i] = new IntOpenHashSet();
        }
        this.nodes = new IntArraySet();
        this.numNodes = numNodes;
        this.graphlet_map = new THashMap<Long, long[]>();
        this.arrivalTime = new int[numNodes];
        this.numEdges = 0;
        //this.edgeList = new ArrayList<DirectedEdge>();
    }

    public void addEdge(int src, int dst){
        OutNeighbors[src].add(dst);
        InNeighbors[dst].add(src);
        Neighbors[src].add(dst);
        Neighbors[dst].add(src);
        nodes.add(src);
        nodes.add(dst);
        numEdges++;
        if(arrivalTime[src] == 0){
            arrivalTime[src] = numEdges;
        }
        if(arrivalTime[dst] == 0){
            arrivalTime[dst] = numEdges;
        }
        //edgeList.add(new DirectedEdge(src, dst));
    }

    public int maxOutDegree(){
        int maxDegree = -1;
        for(int i = 0; i < this.numNodes; i++)
            maxDegree = maxDegree > OutNeighbors[i].size() ? maxDegree : OutNeighbors[i].size();
        return maxDegree;
    }

    public int maxInDegree(){
        int maxDegree = -1;
        for(int i = 0; i < this.numNodes; i++)
            maxDegree = maxDegree > InNeighbors[i].size() ? maxDegree : InNeighbors[i].size();
        return maxDegree;
    }


    public boolean isConnected(int v1, int v2){
        if (contains(v1, v2) || contains(v2, v1)){
            return true;
        }
        return false;
    }
    public boolean contains(DirectedEdge edge){
        int src = edge.getSource();
        int dst = edge.getDestination();
        return OutNeighbors[src].contains(dst);
    }

    public boolean contains(int src, int dst){
        return OutNeighbors[src].contains(dst);
    }

    public int getOutDegree(int src){
        return OutNeighbors[src].size();
    }

    public int getInDegree(int dst){
        return InNeighbors[dst].size();
    }

    public int getDegree(int v){
        return OutNeighbors[v].size() + InNeighbors[v].size();
    }

    public IntSet getOutNeighbors(int src){
        return OutNeighbors[src];
    }

    // return Set(innerNeighbors union outerNeighbors)
    public IntSet getNeighbors(int v){
        return Neighbors[v];
    }

    public IntSet getInNeighbors(int v){
        return InNeighbors[v];
    }

    public ArrayList<DirectedEdge> getAllEdges(){
        ArrayList<DirectedEdge> result = new ArrayList<DirectedEdge>();
        for(int src = 0; src < numNodes; src++){
            for(int dst : OutNeighbors[src]){
                result.add(new DirectedEdge(src, dst));
            }
        }
        return result;
    }


    public IntSet getAllVertices(){
        return nodes;
    }


    /*
    public long[] motifCountfromGraphlet(){
        long[] graphlet = new long[DirectedGraphletType.values().length];
        long[] motifs = new long[DirectedMotifType.values().length];
        for(int key : this.graphlet_map.keySet()){
            DirectedGraphlet vertex_role = this.graphlet_map.get(key);
            for(int i = 0; i < DirectedGraphletType.values().length; i++){
                graphlet[i] += vertex_role.role[i];
            }
        }
        System.out.println(Arrays.toString(graphlet));
        motifs[DirectedMotifType.m1.ordinal()] = graphlet[DirectedGraphletType.m1_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m1_2.ordinal()];
        motifs[DirectedMotifType.m2.ordinal()] = graphlet[DirectedGraphletType.m2_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m2_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m2_3.ordinal()];
        motifs[DirectedMotifType.m3.ordinal()] = graphlet[DirectedGraphletType.m3_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m3_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m3_3.ordinal()];
        motifs[DirectedMotifType.m4.ordinal()] = graphlet[DirectedGraphletType.m4_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m4_2.ordinal()];
        motifs[DirectedMotifType.m5.ordinal()] = graphlet[DirectedGraphletType.m5_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m5_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m5_3.ordinal()];
        motifs[DirectedMotifType.m6.ordinal()] = graphlet[DirectedGraphletType.m6_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m6_2.ordinal()];
        motifs[DirectedMotifType.m7.ordinal()] = graphlet[DirectedGraphletType.m7_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m7_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m7_3.ordinal()];
        motifs[DirectedMotifType.m8.ordinal()] = graphlet[DirectedGraphletType.m8_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m8_2.ordinal()];
        motifs[DirectedMotifType.m9.ordinal()] = graphlet[DirectedGraphletType.m9_1.ordinal()];
        motifs[DirectedMotifType.m10.ordinal()] = graphlet[DirectedGraphletType.m10_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m10_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m10_3.ordinal()];
        motifs[DirectedMotifType.m11.ordinal()] = graphlet[DirectedGraphletType.m11_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m11_2.ordinal()];
        motifs[DirectedMotifType.m12.ordinal()] = graphlet[DirectedGraphletType.m12_1.ordinal()] +
                                                 graphlet[DirectedGraphletType.m12_2.ordinal()] +
                                                 graphlet[DirectedGraphletType.m12_3.ordinal()];
        motifs[DirectedMotifType.m13.ordinal()] = graphlet[DirectedGraphletType.m13_1.ordinal()];
        for(int i = 0 ; i < DirectedMotifType.values().length; i++){
            motifs[i] /= 3;
        }        
        return motifs;
    }
    */
    

}