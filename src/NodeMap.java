package src;
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
    /*
    public long[] motifCountFromVertexRole(){
        long[] vertex_role = new long[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        long[] result = new long[]{0, 0, 0, 0, 0, 0};
        for(int key : this.vertex_role_map.keySet()){
            VertexRole graphlet = this.vertex_role_map.get(key);
            vertex_role[VertexRoleType.CLIQUE.ordinal()] += graphlet.CLIQUE;
            vertex_role[VertexRoleType.CHORDAL_CYCLE_THREE.ordinal()] += graphlet.CHORDAL_CYCLE_THREE;
            vertex_role[VertexRoleType.CHORDAL_CYCLE_TWO.ordinal()] += graphlet.CHORDAL_CYCLE_TWO;
            vertex_role[VertexRoleType.CYCLE.ordinal()] += graphlet.CYCLE;
            vertex_role[VertexRoleType.TAILED_TRIANGLE_THREE.ordinal()] += graphlet.TAILED_TRIANGLE_THREE;
            vertex_role[VertexRoleType.TAILED_TRIANGLE_TWO.ordinal()] += graphlet.TAILED_TRIANGLE_TWO;
            vertex_role[VertexRoleType.TAILED_TRIANGLE_ONE.ordinal()] += graphlet.TAILED_TRIANGLE_ONE;
            vertex_role[VertexRoleType.PATH_TWO.ordinal()] += graphlet.PATH_TWO;
            vertex_role[VertexRoleType.PATH_ONE.ordinal()] += graphlet.PATH_ONE;
            vertex_role[VertexRoleType.STAR_THREE.ordinal()] += graphlet.STAR_THREE;
            vertex_role[VertexRoleType.STAR_ONE.ordinal()] += graphlet.STAR_ONE;
        }

        result[MotifType.CLIQUE.ordinal()] = vertex_role[VertexRoleType.CLIQUE.ordinal()];
        result[MotifType.CHORDAL_CYCLE.ordinal()] = vertex_role[VertexRoleType.CHORDAL_CYCLE_THREE.ordinal()] + 
                                                    vertex_role[VertexRoleType.CHORDAL_CYCLE_TWO.ordinal()];
        result[MotifType.TAILED_TRIANGLE.ordinal()] = vertex_role[VertexRoleType.TAILED_TRIANGLE_THREE.ordinal()] + 
                                                       vertex_role[VertexRoleType.TAILED_TRIANGLE_TWO.ordinal()] +
                                                       vertex_role[VertexRoleType.TAILED_TRIANGLE_ONE.ordinal()];
        result[MotifType.CYCLE.ordinal()] = vertex_role[VertexRoleType.CYCLE.ordinal()];
        result[MotifType.STAR.ordinal()] = vertex_role[VertexRoleType.STAR_THREE.ordinal()] + 
                                           vertex_role[VertexRoleType.STAR_ONE.ordinal()];
        result[MotifType.PATH.ordinal()] = vertex_role[VertexRoleType.PATH_TWO.ordinal()] + 
                                           vertex_role[VertexRoleType.PATH_ONE.ordinal()];
        
        
        for(int i = 0; i < 6; i++){
            result[i] = result[i] / 4;
        }
        
        return result;
    }

    
    public long[] tripletCountFromTripletRole(){
        long[] result = new long[]{0, 0};
        for(int key : this.triplet_role_map.keySet()){
            TripletRole graphlet = this.triplet_role_map.get(key);
            result[TripletType.TRIANGLE.ordinal()] += graphlet.CYCLE;
            result[TripletType.PATH.ordinal()] += graphlet.PATH_TWO;
            result[TripletType.PATH.ordinal()] += graphlet.PATH_ONE;
        }
    
        for(int i = 0; i < 2; i++){
            result[i] = result[i] / 3;
        }
        
        return result;
    }
    */
}