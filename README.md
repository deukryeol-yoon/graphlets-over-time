# Graphlets over Time : A New Lens for Temporal Graph Analysis
Source code for the paper [Graphlets over Time: A New Lens for Temporal Graph Analysis]

Our code is provided like below:
* data : 9 real-world temporal graphs and their randomized graph used in this paper. 
* src : source code for counting graphlets, transitions of graphlets, node roles, and edge roles (implemented in Java).
* result : Three categorized results: (1) graph-analysis (2) node-analysis (3) edge-analysis. The code for visualization and prediction is also provided (implemented in Python). 

## Datasets
The preprocessed datasets and the centralites of nodes used in the paper can be provided in [here](https://www.dropbox.com/sh/8vkizmq2mzknav4/AACGR-ZOWHJ4JkLqWWMeZSIGa?dl=0)r. Please download the datasets from the links, and put the files under the "./data" folder so that the hierarchy would be like, for example, 
```
data
  |__centrality
      |__askubuntu-degree.csv
      |__askubuntu-between.csv
      |__...
  |__askubuntu.out
  |__askubuntu-random.out
  |__ ...
src
```
The original datasets used in the papers are listed as follows:

| Name                             	| #Nodes    | #Edges    	| Description                                                             		| Download Original Data                                                                       |
|-----------------------------------|-----------|---------------|-------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| cite-HepPh (hepph)    			| 34,545    | 346,849   	| [Citation](https://snap.stanford.edu/data/cit-HepPh.html) 					| [Link](https://snap.stanford.edu/data/cit-HepPh.txt.gz) |
| cite-HepTh (hepph)    			| 18,477    | 136,190      	| [Citation](https://snap.stanford.edu/data/cit-HepTh.html) 					| [Link](https://snap.stanford.edu/data/cit-HepTh.txt.gz) |
| cite-Patents (hepph)    			| 3,774,362 | 16,512,782   	| [Citation](https://snap.stanford.edu/data/cit-Patents.html) 					| [Link](https://snap.stanford.edu/data/cit-Patents.txt.gz) |
| email-EU-core-temporal (email-eu) | 986     	| 209,203   	| [Email/Message](https://snap.stanford.edu/data/email-Eu-core-temporal.html) 	| [Link](https://snap.stanford.edu/data/email-Eu-core-temporal.txt.gz) |
| CollegeMsg (college_msg) 			| 1,899     | 24,929   		| [Email/Message](https://snap.stanford.edu/data/CollegeMsg.html) 				| [Link](https://snap.stanford.edu/data/CollegeMsg.txt.gz) |
| Enron (enron)    					| 55,655    | 20,296	   	| [Email/Message](http://www.cs.cmu.edu/~enron) 								| [Link](https://www.cs.cmu.edu/~./enron/enron_mail_20150507.tar.gz) |
| sx-mathoverflow (mathoverflow)    | 24,818    | 262,106   	| [Online Q/A](https://snap.stanford.edu/data/sx-mathoverflow.html) 			| [Link](https://snap.stanford.edu/data/sx-mathoverflow.txt.gz) |
| sx-askubuntu (askubuntu)    		| 159,316   | 90,489   		| [Online Q/A](https://snap.stanford.edu/data/sx-askubuntu.html) 				| [Link](https://snap.stanford.edu/data/sx-askubuntu.txt.gz) |
| sx-stackoverflow (stackoverflow)    		| 2,601,977 | 16,266,395	| [Online Q/A](https://snap.stanford.edu/data/sx-stackoverflow.html) 			| [Link](https://snap.stanford.edu/data/sx-stackoverflow.txt.gz) |


## Version
 * Our code works on both Windows10 and Linux. 
 * JDK version : 15.0.1, Python version : 3.7.0.

## Input Format
 * The input file should contain a set of temporal edges. Each temporal edge is represented by 1) index of the source node, 2) index of the destination node, and 3) its timestamp written in a line.
 * The node index starts from 0 and increases by 1 whenever a new node arrives (i.e., 0, 1, 2, ..., |V|-2, |V|-1).
 * For example, for a set of 3 temporal edges (0 → 1, 2001-01-01), (0 → 2, 2001-01-01), and (1 → 3, 2001-01-02), the input file should be: 
```
  0 1 2001-01-01
  0 2 2001-01-01
  1 3 2001-01-02
```

## Running Demo
You can create intermediate files and see the results using script files below. Because of the time complexity, large datasets (Patent, Stackoverflow) are commented out.
 * draw-correlation-heatmap.sh : draw a heatmap of similarity of graphs in dataset. (Figure 2 and 9)
 * generate-all-evolution.sh : generate ratios of graphlets in datasets. 
 * graph-evolution.sh : draw ratios of graphlets over time (Figure 3) and calculate average R-squared value of them. 
 * graphlet-transition-graph.sh : draw graphlet transition graphs. (Figure 4)
 * node_signal.sh :  draw monotonic increasing or decreasing node signals. (Figure 7)
 * node_prediction.sh : generate node features, which are node role, node prominence profile, and global statistics, and predict the centrality of nodes using them.  
 * edge_signal.sh : draw monotonic increasing or decreasing edge signals. (Figure 8)
 * edge_prediction.sh : generate edge features, which are edge role and global statistics, and predict the centrality of edges using them.
