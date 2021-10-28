
# For small datasets (except Patent, Stackoverflow)
sh generate-all-evolution.sh
cd result/graph-analysis
python draw-correlation-motif-transition.py 

 # For all datasets

#sh generate-all-evolution.sh
#cd src
#cd ../result/graph-analysis
#java -jar graphlets-over-time.jar 1 patent 3774362 776 16512782
#java -jar graphlets-over-time.jar 1 stackoverflow 2601977 44067 63497051
#python draw-correlation-motif-transition.py --a