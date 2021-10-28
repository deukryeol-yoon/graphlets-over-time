# Graphlets-over-Time : A New Lens for Temporal Graph Analysis
Source code for the paper [Graphlets-over-Time: A New Lens for Temporal Graph Analysis]

Our code is provided like below:
* data : 9 real-world temporal graphs and their randomized graph used in this paper. Since the size of each file is limited to 100MB in the github, we do split-compression for large files. Please decompress them before demo: data/data.vol1.egg ~ data.vol7.egg, data/centrality/patent-pagerank.egg
* src : source code for counting graphlets, transitions of graphlets, node roles, and edge roles (implemented in Java).
* result : Three categorized results: (1) graph-analysis (2) node-analysis (3) edge-analysis. The code for visualization and prediction is also provided (implemented in Python). 

## Version
 * Our code works on either Windows10 or Linux. 
 * Java version : 15.0.1, Python version : 3.7.0.

## Input Format
 * The input format should be lines of directed edges with timestamp (src dst yyyy-mm-dd).

## Running Demo
Because of the size of the data, we only provide the original data and source code. You can create intermediate files and see the results using script files below:
 * generate-all-evolution.sh : generate ratios of graphlets in datasets. Because of time complexity, large datasets (patent, stackoverflow) is commented out.
 * graph-evolution.sh : draw ratios of graphlets over time (Figures in Table 4.) and calculate average $$R^2$$ score of them. 
 * graphlet-transition-graph.sh : draw graphlet transition graphs. (Figures in Table 5)
 * node_signal.sh :  draw monotonic increasing or decreasing node signals. (Figures in Figure 4)
 * node_prediction.sh : generate node features, which are node role, node prominence profile, and global statistics, and predict the centrality of nodes using them.  
 * edge_signal.sh : draw monotonic increasing or decreasing edge signals. (Figures in Figure 4)
 * edge_prediction.sh : generate edge features, which are edge role and global statistics, and predict the centrality of edges using them.