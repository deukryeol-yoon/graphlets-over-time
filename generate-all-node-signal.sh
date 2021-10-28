: <<'END'
    {"hepph", "30565", "660", "346849"},
    {"hepph-random", "30565", "660", "346849"},
    {"hepth", "18477", "1069", "136190"},
    {"hepth-random", "18477", "1069", "136190"},
    {"enron", "55655", "1335", "297456"}, 
    {"enron-random", "55655", "1335", "1918412"},
    {"email-eu", "986", "211", "24929"},
    {"email-eu-random", "986", "211", "24929"},
    {"college_msg", "1899", "137", "20296"},
    {"college_msg-random", "1899", "137", "20296"},
    {"mathoverflow", "21688", "969", "506551"}
    {"mathoverflow-random", "21688", "969", "506551"}
    {"askubuntu", "137517","1954", "596933"},
    {"askubuntu-random", "137517", "1954", "964438"}

    If you want to observe other dataset, follow below:
    	cd src
    	java -jar graphlets-over-time.jar 2 dataset_name num_nodes max_in_degree num_edges
    	cd ../result/graph-analysis
		python draw-graph-evolution.py dataset_name
END

cd src
java -jar graphlets-over-time.jar 2 hepph 30565 660 346849
java -jar graphlets-over-time.jar 2 hepth 18477 1069 136190
java -jar graphlets-over-time.jar 2 enron 55655 1335 1918412
java -jar graphlets-over-time.jar 2 email-eu 986 211 24929
java -jar graphlets-over-time.jar 2 college_msg 1899 137 20296
java -jar graphlets-over-time.jar 2 mathoverflow 21688 969 506551
java -jar graphlets-over-time.jar 2 askubuntu 137517 1954 964438
