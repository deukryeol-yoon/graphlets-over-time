# Graphlets-over-Time : A New Lens for Temporal Graph Analysis
Source code for the paper [Graphlets-over-Time: A New Lens for Temporal Graph Analysis]

Our code is provided like below:
* data : 9 real-world temporal graph and their randomized graph used in this paper.
* src : source code for counting graphlet, their transitions, node roles, and edge roles (implemented in Java).
* result : Three categorized results: (1) graph-analysis (2) node-analysis (3) edge-analysis. The code for visualization and prediction is also provided (implemented in Python).

## Version
 * Our code works on either Windows10 or Linux. 
 * Java version : 15.0.1, Python version : 3.7.0.

## Input Format
 * The input format should be lines of directed edges with timestamp (yyyy-mm-dd).

## Notice
 * Sice the maximum volumn of each file is 100MB, we do split compression for large datasets. Please decompress those files: data/data.vol1.egg ~ data.vol7.egg, data/centrality/patent-pagerank.egg

## Running Demo
 Because of data sizes, we only provide original datasets and source codes. You could generate intermediate files by following script files.
 * generate-all-evolution.sh : generate ratios of graphlets in datasets. Because of time complexity, large datasets (patent, stackoverflow) is commented out.
 * generate-all-node-signal.sh : generate node signals.
 * generate-all-edge-signal.sh : generate edge signals.
 * graph-evolution.sh : draw ratios of graphlets over time (Figures in Table 4.) and calculate average $$R^2$$ score of them. 
 * graphlet-transition-graph.sh : draw graphlet transition graphs. (Figures in Table 5)
 * node_signal.sh :  draw monotonic increase or decrease node signals. (Figures in Figure 4)
 * node_prediction.sh : generate features, which are node role, node prominence profile, and global statistics, and predict node centrality.  
 * edge_signal.sh : draw monotonic increase or decrease edge signals. (Figures in Figure 4)
 * edge_prediction.sh : generate features, which are edge role and global statistics, and predict edge centrality.
 

