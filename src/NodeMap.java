import java.util.ArrayList;
import it.unimi.dsi.fastutil.ints.*;

public class NodeMap {
    public IntSet[] neighbors;
    private int numNodes;
    public IntSet nodes;

    public NodeMap(int numNodes){
        this.numNodes = numNodes;
        neighbors = new IntSet[numNodes];
        nodes = new IntArraySet();
        for(int i = 0; i < numNodes; i++){
            neighbors[i] = new IntOpenHashSet();
        }
    }

    public void addEdge(int src, int dst){
        neighbors[src].add(dst);
        neighbors[dst].add(src);
        nodes.add(src);
        nodes.add(dst);
    }

    public int maxDegree(){
        int max_degree = -1;
        for(int i = 0; i < neighbors.length; i++){
            int degree = neighbors[i].size();
            if (max_degree < degree){
                max_degree = degree;
            }
        }
        return max_degree;
    }

    public boolean contains(StreamEdge edge){
        int src = edge.getSource();
        int dst = edge.getDestination();
        if(neighbors[src].contains(dst)) return true;
        return false;
    }

    public boolean contains(int src, int dst){
        if(neighbors[src].contains(dst)) return true;
        return false;
        
    }

    public int getDegree(int src){
        return neighbors[src].size();
    }

    public IntSet getNeighbors(int src){
       return neighbors[src];
    }

    public ArrayList<StreamEdge> getAllEdges(){
        ArrayList<StreamEdge> result = new ArrayList<StreamEdge>();
        for(int src = 0; src < numNodes; src++){
            for(int dst : neighbors[src]){
                result.add(new StreamEdge(src, dst));
            }
        }
        return result;
    }


    public IntSet getAllVertices(){
        return nodes;
    }
}