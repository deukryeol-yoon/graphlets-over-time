package src;

public class main {
    //static String file_path = "C:\\workspace\\data\\web_polblogs.txt";
    static String path = System.getProperty("user.dir");
    //static String read_path = "C:\\workspace\\data\\facebook_combined.txt\\facebook_combined.txt";
    public static String[][] dataset = {
        //format : {dataset, numNodes, maxInDegree, numEdges}
        //{"hepph", "30565", "660", "346849"},
        //{"hepph-random", "30565", "660", "346849"},
        //{"hepph-randomized", "30565", "660", "346849"}
        //{"hepth", "18477", "1069", "136190"},
        //{"hepth-random", "18477", "1069", "136190"},
        //{"hepth-randomized", "18477", "1069", "136190"}
        //{"patent", "3774362", "776", "16512782"},
        //{"patent-random", "3774768", "776", "16512782"}
        //{"patent-randomized", "3774768", "776", "16512782"}
        //{"enron", "55655", "1335", "297456"}, 
        //{"enron-random", "55655", "1335", "1918412"},
        //{"enron-randomized", "86978", "1335", "297456"},
        //{"email-eu", "986", "211", "24929"},
        //{"email-eu-randomized", "986", "211", "24929"},
        //{"email-eu-random", "986", "211", "24929"},
        //{"college_msg", "1899", "137", "20296"},
        //{"college_msg-random", "1899", "137", "20296"},
        //{"college_msg-randomized", "1899", "137", "20296"},
        //{"stackoverflow", "2601977","44067",  "63497051"},
        //{"stackoverflow-random", "2601977","44067",  "63497051"},
        //{"stackoverflow-randomized", "2601977", "44067",  "63497051"},
        //{"mathoverflow","21688", "969", "239978"},
        //{"mathoverflow-c2a-randomized","21687", "969", "239978"},
        //{"mathoverflow-random", "21688", "969", "506551"}
        //{"mathoverflow-randomized", "21688", "969", "506551"}
        {"askubuntu", "137517","1954", "596933"},
        //{"askubuntu-random", "137517", "1954", "964438"}
        //{"askubuntu-randomized", "137517", "1954", "964438"}
        //{"temp_graph", "100", "100", "100"}
    };
    static String data_path = path + "/data/temporal_graph/" + dataset[0][0] + ".out";
    static int max_index;
    static int MODE = 1;
    static boolean record = true;
    
    public static void main(String args[]){
        if(MODE == 1){
            DirectedMotifFrequencyAnalysis graphAnalysis = new DirectedMotifFrequencyAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][3]));
            graphAnalysis.run();
            /*
            for(int i = 0 ; i <10; i++){
                String d_path = path + "/data/temporal_graph/" + dataset[0][0] + "-" + Integer.toString(i) + ".out";
                DirectedMotifFrequencyAnalysis graphAnalysis = new DirectedMotifFrequencyAnalysis(d_path, dataset[0][0]+"-"+Integer.toString(i), Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][3]));
                graphAnalysis.run();
            }
            */
            
            
        }
        else if (MODE == 2){
            String[] centrality_list = new String[]{"degree", "between", "closeness", "pagerank"};
            for(String centrality: centrality_list){
                DirectedGraphletAnalysis graphAnalysis = new DirectedGraphletAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][2]), centrality);
                graphAnalysis.run();
            }
        }
        else if (MODE == 4){
            TriangleAnalysis triangleAnalysis = new TriangleAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][2]));
            triangleAnalysis.run();
        }
        else if (MODE == 5){
            PowerlawAnalysis powerlawAnalysis = new PowerlawAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][2]), Integer.parseInt(dataset[0][3]));
            powerlawAnalysis.run();
        }
        else if(MODE == 6){
            DUDGraphletAnalysis graphAnalysis = new DUDGraphletAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][2]), Integer.parseInt(dataset[0][3]));
            graphAnalysis.run();
        }
        else if(MODE == 7){
            DirectedEdgeRoleAnalysis edgeroleAnalysis = new DirectedEdgeRoleAnalysis(data_path, dataset[0][0], Integer.parseInt(dataset[0][3]), Integer.parseInt(dataset[0][2]), Integer.parseInt(dataset[0][3]), "edge-betweenness");
            edgeroleAnalysis.run();
        }
        //else if(MODE == 8){
        //    UndirectedGraphletOrbit graphletOrbit = new UndirectedGraphletOrbit(data_path, dataset[0][0], Integer.parseInt(dataset[0][1]), Integer.parseInt(dataset[0][2]), Integer.parseInt(dataset[0][3]));
        //    graphletOrbit.run();
        //}
    }
    
}

