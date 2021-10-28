public class main {
    static String path = System.getProperty("user.dir");
    // Dataset Argument
    public static String[][] dataset = {
        //format : {dataset, numNodes, maxInDegree, numEdges}
        {"hepph", "30565", "660", "346849"},
        //{"hepph-random", "30565", "660", "346849"},
        //{"hepth", "18477", "1069", "136190"},
        //{"hepth-random", "18477", "1069", "136190"},
        //{"patent", "3774362", "776", "16512782"},
        //{"patent-random", "3774768", "776", "16512782"}
        //{"enron", "55655", "1335", "297456"}, 
        //{"enron-random", "55655", "1335", "1918412"},
        //{"email-eu", "986", "211", "24929"},
        //{"email-eu-random", "986", "211", "24929"},
        //{"college_msg", "1899", "137", "20296"},
        //{"college_msg-random", "1899", "137", "20296"},
        //{"stackoverflow", "2601977","44067",  "63497051"},
        //{"stackoverflow-random", "2601977","44067",  "63497051"},
        //{"mathoverflow", "21688", "969", "506551"}
        //{"mathoverflow-random", "21688", "969", "506551"}
        //{"askubuntu", "137517","1954", "596933"},
        //{"askubuntu-random", "137517", "1954", "964438"}
    };
    static int MODE = 1;
    
    public static void main(String args[]){
        String dataset_name = dataset[0][0];
        String numNodes = dataset[0][1];
        String maxDegree = dataset[0][2];
        String numEdges = dataset[0][3];
        //int MODE = Integer.parseInt(args[0]);
        //String dataset_name = args[1];
        //String numNodes = args[2];
        //String maxDegree = args[3];
        //String numEdges = args[4];
        String dpath = path + "/../data/" +dataset_name + ".out";
        if(MODE == 1){
            // Observation 1, 2, 3
            GraphletEvolution graphAnalysis = new GraphletEvolution(dpath, dataset_name, Integer.parseInt(numNodes), Integer.parseInt(numEdges));
            graphAnalysis.run();
        }
        else if (MODE == 2){
            // Observation 4, 5
            String[] centrality_list = new String[]{"degree", "between", "closeness", "pagerank"};
            for(String centrality: centrality_list){
                NodeRoleSignal nodeRoleSignal = new NodeRoleSignal(dpath, dataset_name, Integer.parseInt(numNodes), Integer.parseInt(maxDegree), centrality);
                nodeRoleSignal.run();
            }
        }
        else if(MODE == 3){
            // Observation 6, 7, 8, 9
            NodeCentralityPredictionFeatures graphAnalysis = new NodeCentralityPredictionFeatures(dpath, dataset_name, Integer.parseInt(numNodes), Integer.parseInt(maxDegree), Integer.parseInt(numEdges));
            graphAnalysis.run();
        }
        //
        else if(MODE == 4){
            // Observation 10, 11, 12
            EdgeRoleSignalAndCentralityPredictionFeatures edgeroleAnalysis = new EdgeRoleSignalAndCentralityPredictionFeatures(dpath, dataset_name, Integer.parseInt(numEdges), Integer.parseInt(maxDegree), Integer.parseInt(numEdges), "edge-betweenness");
            edgeroleAnalysis.run();
        }
    }
    
}