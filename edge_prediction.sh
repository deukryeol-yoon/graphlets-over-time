: <<'END'
    format : {dataset_name, num_nodes, max_in_degree, num_edges}
    {"hepph", "30565", "660", "346849"},
    {"hepph-random", "30565", "660", "346849"},
    {"hepth", "18477", "1069", "136190"},
    {"hepth-random", "18477", "1069", "136190"},
    {"patent", "3774362", "776", "16512782"},
    {"patent-random", "3774768", "776", "16512782"}
    {"enron", "55655", "1335", "297456"}, 
    {"enron-random", "55655", "1335", "1918412"},
    {"email-eu", "986", "211", "24929"},
    {"email-eu-random", "986", "211", "24929"},
    {"college_msg", "1899", "137", "20296"},
    {"college_msg-random", "1899", "137", "20296"},
    {"stackoverflow", "2601977","44067",  "63497051"},
    {"stackoverflow-random", "2601977","44067",  "63497051"},
    {"mathoverflow", "21688", "969", "506551"}
    {"mathoverflow-random", "21688", "969", "506551"}
    {"askubuntu", "137517","1954", "596933"},
    {"askubuntu-random", "137517", "1954", "964438"}

    If you want to observe other temporal graphs, follow below:
        cd src
        java -jar graphlets-over-time.jar 4 dataset_name num_nodes max_in_degree num_edges
        cd result/edge-analysis/
        python edge_prediction.py dataset_name target_degree
            - target_degree : one of 2, 4, and 8
END

cd src
java -jar graphlets-over-time.jar 4 hepph 30565 660 346849
cd ../result/edge-analysis/
python edge_prediction.py hepph 2