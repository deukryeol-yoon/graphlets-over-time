import matplotlib
import matplotlib.pyplot as plt 
import numpy as np
import os.path as osp
import pandas as pd 
import os
import csv
import seaborn as sns
from pandas import DataFrame
from matplotlib.backends.backend_pdf import PdfPages
from tqdm import tqdm
title = {"hepph": "HepPh", "hepth" : "HepTh", "patent" : "Patent", "email-eu" : "EU", "enron" : "Enron", "college_msg" : "College", "askubuntu" : "Ask", "mathoverflow" : "Math", "stackoverflow" : "Stack"}
def draw(dataset):
	path = osp.abspath("")
	path = osp.join(path, dataset, "graphlet_ratio.csv")
	
	colors = sns.color_palette("bright", 10)
	colors.append("#003366")
	colors.append("#834C24")
	colors.append("#9CFF00")
	plt.figure(figsize=(6, 6))
	df = pd.read_csv(path, encoding='euc-kr')
	X = np.array(df.index.values.tolist())
	X = X / np.max(X)

	for i, column in enumerate(df.columns):
		plt.plot(X, df[column], color=colors[i], linewidth=1.8, label=column)

	plt.ylabel("Graphlet Ratio", fontsize=40)
	plt.yticks([0, 0.5, 1], fontsize=40)

	plt.xlabel("Evolution Ratio", fontsize=40)
	plt.xticks([0, 0.5, 1], fontsize=40)
	if dataset.endswith("-random"):
		plt.title(title[dataset[:-7]]+"-random", fontsize=40)
	else:
		plt.title(title[dataset], fontsize=40)

	plt.tight_layout()
	plt.show()
	
if __name__ == '__main__':
	from tqdm import tqdm
	import sys
	if len(sys.argv) > 1:
		dataset = sys.argv[1]
		draw(dataset)
	else:
		list_dataset=["hepph", "hepth", "patent", "email-eu", "enron", "college_msg", "askubuntu", "mathoverflow", "stackoverflow" ]
		for dataset in tqdm(list_dataset):
			draw(dataset)
			draw(dataset+'-random')
	