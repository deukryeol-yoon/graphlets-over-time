import matplotlib
import matplotlib.pyplot as plt 
import numpy as np
import os.path as osp
import pandas as pd 
import os
import csv
import seaborn as sns
from pandas import DataFrame
from tqdm import tqdm
from sklearn import datasets, linear_model
from sklearn.metrics import r2_score

def fitting(list_dataset):
	for dataset in list_dataset:
		path = osp.abspath("")
		path = osp.join(path, dataset, "graphlet_ratio.csv")
		df = pd.read_csv(path, encoding='euc-kr')
		X = np.array(df.index.values.tolist())
		x_test = [X[int(len(X) * 0.1)],
					X[int(len(X) * 0.2)],
					X[int(len(X) * 0.3)],
					X[int(len(X) * 0.4)],
					X[int(len(X) * 0.5)],
					X[int(len(X) * 0.6)],
					X[int(len(X) * 0.7)],
					X[int(len(X) * 0.8)],
					X[int(len(X) * 0.9)],
					X[int(len(X))-1]]
		x_start = int(len(X) * 0.1)
		output = []
		for i, column in enumerate(df.columns):
			linreg = linear_model.LinearRegression()
			model = linreg.fit(np.array(x_test).reshape(-1, 1), df[column].iloc[x_test]) 
			y_pred = model.predict(X.reshape(-1, 1)) 

			output.append(r2_score(df[column][x_start:], y_pred[x_start:])) 
		print("r2score for " + dataset + " : " + str(sum(output) / len(output)))

if __name__ == '__main__':
	import sys
	if len(sys.argv) > 1:
		dataset = [sys.argv[1]]
		fitting(dataset)
	else :
		list_dataset = ["hepph", "hepph-random",
			"hepth", "hepth-random",
			"patent", "patent-random",
			"email-eu", "email-eu-random",
			"enron", "enron-random",
			"college_msg", "college_msg-random",
			"askubuntu", "askubuntu-random",
			"mathoverflow", "mathoverflow-random",
			"stackoverflow", "stackoverflow-random"]
		fitting(list_dataset)
