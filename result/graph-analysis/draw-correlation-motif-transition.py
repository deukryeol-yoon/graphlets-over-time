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
from sklearn.metrics import mean_squared_error
import math
pd.set_option('display.max_columns', 1000)
list_dataset = ["hepph", "hepth","patent", "enron", "email-eu", "college_msg", "mathoverflow", "askubuntu", "stackoverflow"]
dataset_label=["HepPh", "HepTh","Patent", "Enron", "EU", "College", "Math", "Ask", "Stack"]
domain = ["citation", "citation","citation", "email", "email", "email", "interaction", "interaction", "interaction"]
motifs = ["m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "m11", "m12", "m13"]

edges = ["m3-m10", "m2-m9", "m2-m7", "m2-m5", "m8-m12", "m4-m7", "new-m3", "new-m2", "new-m1", 
        "new-m7", "new-m4", "m9-m10", "m10-m12", "m4-m5", "m2-m3", "m11-m12", "m3-m8", "m6-m12", 
        "m5-m10", "m1-m5", "m3-m6", "m5-m11", "m12-m13", "m7-m10", "m5-m6", "m7-m11", "m7-m8", "m1-m3"]

small_dataset = ["hepph", "hepth", "enron", "email-eu", "college_msg", "mathoverflow", "askubuntu", ]
small_label =["HepPh", "HepTh", "Enron", "EU", "College", "Math", "Ask"]
numRandom = 3
def transition_diagram():
	print('transition_diagram')
	list_mtx = []
	for dataset in list_dataset:
		print(dataset)
		path = osp.join(osp.abspath(""), dataset, "change.csv")
		real_items = list(csv.reader(open(path, newline="")))
		real_motif = list(map(int, real_items[-1]))
		df = pd.DataFrame(columns = edges)
		for i in range(0, numRandom):
		    rand = osp.join(osp.abspath(""), 'result-random', dataset + "-random-" + str(i), "change.csv")
		    random_items = list(csv.reader(open(rand, newline="")))
		    random_motifs = list(map(int, random_items[-1]))
		    df.loc[len(df)] = random_motifs
		mean = []
		std = []

		for column in edges:
			mean.append(df[column].mean())
			std.append(df[column].std())
		Z = []
		for i in range(len(edges)):
			Z_i = (real_motif[i] * 1.0 - mean[i]) / (real_motif[i] + mean[i] + 4)
			Z.append(Z_i)
		denominator = 0.0
		for z in Z:
		    denominator += z * z
		denominator = denominator ** 0.5
		SP = []
		for i in range(len(edges)):
			SP_i = Z[i] / math.sqrt(denominator)
			SP.append(SP_i)

		list_mtx.append(SP)

	list_SP_T = np.array(list_mtx).T
	df = pd.DataFrame(list_SP_T, columns = list_dataset)
	draw(df, 'transition_diagram')


def transition_diagram_small():
	print('transition_diagram_small')
	list_mtx = []
	for dataset in small_dataset:
		print(dataset)
		path = osp.join(osp.abspath(""), dataset, "change.csv")
		real_items = list(csv.reader(open(path, newline="")))
		real_motif = list(map(int, real_items[-1]))
		df = pd.DataFrame(columns = edges)
		for i in range(0, numRandom):
		    rand = osp.join(osp.abspath(""), 'result-random', dataset + "-random-" + str(i), "change.csv")
		    random_items = list(csv.reader(open(rand, newline="")))
		    random_motifs = list(map(int, random_items[-1]))
		    df.loc[len(df)] = random_motifs
		mean = []
		std = []

		for column in edges:
			mean.append(df[column].mean())
			std.append(df[column].std())
		Z = []
		for i in range(len(edges)):
			Z_i = (real_motif[i] * 1.0 - mean[i]) / (real_motif[i] + mean[i] + 4)
			Z.append(Z_i)
		denominator = 0.0
		for z in Z:
		    denominator += z * z
		denominator = denominator ** 0.5
		SP = []
		for i in range(len(edges)):
			SP_i = Z[i] / math.sqrt(denominator)
			SP.append(SP_i)

		list_mtx.append(SP)

	list_SP_T = np.array(list_mtx).T
	df = pd.DataFrame(list_SP_T, columns = small_dataset)
	draw_small(df, 'transition_diagram_small')


def subgraph_ratio_profile():
	print('subgraph_ratio_profile')
	list_SP = []
	for dataset in list_dataset:
		df = pd.DataFrame(columns = motifs)
		for i in range(0, numRandom):
			rand = osp.join(osp.abspath(""), dataset + "-randomized-" + str(i), "graphlet_abs.csv")
			random_items = list(csv.reader(open(rand, newline="")))
			random_motifs = list(map(int, random_items[-1]))
			#print(random_motifs)
			df.loc[len(df)] = random_motifs
		mean = []
		std = []
		for column in motifs:
			mean.append(df[column].mean())
			std.append(df[column].std())
		real = osp.join(osp.abspath(""), dataset, "motif_abs.csv")
		real_items = list(csv.reader(open(real, newline="")))
		real_motif = list(map(int, real_items[-1]))
		Z = []
		for i in range(len(motifs)):
			Z_i = (real_motif[i] * 1.0 - mean[i]) / (real_motif[i] + mean[i] + 4)
			Z.append(Z_i)
		denominator = 0.0
		for z in Z:
			denominator += z * z
		denominator = denominator ** 0.5
		SP = []
		for i in range(len(motifs)):
			SP_i = Z[i] / denominator
			SP.append(SP_i)
		list_SP.append(SP)

	list_SP_T = np.array(list_SP).T
	df = pd.DataFrame(list_SP_T, columns = list_dataset)
	sim_calc(df)

def sim_calc(df):
	corr = df.astype(float).corr()
	same = []
	cross = []

	best = 999
	best_threshold = -1
	thresholds = [i * 0.01 for i in range(100)]
	for threshold in thresholds:
		same_pos = 0
		same_neg = 0
		cross_pos = 0
		cross_neg = 0
		for i in range(0, len(corr)):
			for j in range(i+1, len(corr)):
				value = corr.iloc[i, j]
				if domain[i] == domain[j]:
					if value > threshold:
						same_pos += 1
					else:
						same_neg += 1
				else:
					if value < threshold:
						cross_pos += 1
					else:
						cross_neg += 1
		if best > same_neg + cross_neg:
			best = same_neg + cross_neg
			best_threshold = threshold
	print(best_threshold, best)

def draw(df, name):
	
	plt.figure(figsize=(4, 2))
	colormap = sns.color_palette("YlOrBr", as_cmap=True)
	ax = sns.heatmap(df.astype(float).corr(), linewidths = 0,  square = True, cmap = colormap, xticklabels=[])
	ax.set_yticklabels(dataset_label)
	labels = ax.get_yticklabels()
	ticks = ax.get_yticks()
	
	for label, tick in zip(labels, ticks):
		label.set_fontsize(13)
		if tick == 0.5:
			label.set_color('red')
		elif tick == 1.5:
			label.set_color('red')
		elif tick == 2.5:
			label.set_color('red')
		elif tick == 2.5:
			label.set_color('blue')
		elif tick == 3.5:
			label.set_color('blue')
		elif tick == 4.5:
			label.set_color('blue')
		elif tick == 5.5:
			label.set_color('blue')
		else: label.set_color('green')
	plt.tight_layout()
	plt.savefig(name+'.png', dpi=500)
	plt.show()
	
	
	plt.figure(figsize=(6, 2))
	
	X = np.array(df.index.values.tolist())
	header = ['enron', 'email-eu', 'college_msg', 'mathoverflow', 'askubuntu', 'stackoverflow', 'hepph', 'hepth', 'patent']
	color = []
	for name in header:
		if name == "hepth":
			color.append("#53aae5")
		elif name == "hepph":
			color.append("#D53515")
		elif name == "patent":
			color.append("#65b473")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	plt.tight_layout()
	plt.show()

	plt.figure(figsize=(6, 2))
	X = np.array(df.index.values.tolist())
	header = ['mathoverflow', 'askubuntu', 'stackoverflow', 'hepph', 'hepth', 'patent', 'enron', 'email-eu', 'college_msg']
	color = []
	for name in header:
		if name == "enron":
			color.append("#53aae5")
		elif name == "email-eu":
			color.append("#D53515")
		elif name == "college_msg":
			color.append("#65b473")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	plt.tight_layout()
	plt.show()
	
	plt.figure(figsize=(6, 2))
	X = np.array(df.index.values.tolist())
	header = ['hepph', 'hepth', 'patent', 'enron', 'email-eu', 'college_msg', 'mathoverflow', 'askubuntu', 'stackoverflow']
	color = []
	for name in header:
		print(name)
		if name == "askubuntu":
			color.append("#53aae5")
		elif name == "mathoverflow":
			color.append("#D53515")
		elif name == "stackoverflow":
			color.append("#65b473")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	
	plt.tight_layout()
	plt.show()
	
	
def draw_small(df, name):
	
	plt.figure(figsize=(4, 2))
	colormap = sns.color_palette("YlOrBr", as_cmap=True)
	ax = sns.heatmap(df.astype(float).corr(), linewidths = 0,  square = True, cmap = colormap, xticklabels=[])
	ax.set_yticklabels(small_label)
	labels = ax.get_yticklabels()
	ticks = ax.get_yticks()
	
	for label, tick in zip(labels, ticks):
		label.set_fontsize(13)
		if tick == 0.5:
			label.set_color('red')
		elif tick == 1.5:
			label.set_color('red')
		elif tick == 2.5:
			label.set_color('blue')
		elif tick == 3.5:
			label.set_color('blue')
		elif tick == 4.5:
			label.set_color('blue')
		else: label.set_color('green')
	plt.tight_layout()
	plt.savefig(name+'.png', dpi=500)
	plt.show()
	
	
	plt.figure(figsize=(6, 2))
	
	X = np.array(df.index.values.tolist())
	header = ['enron', 'email-eu', 'college_msg', 'mathoverflow', 'askubuntu', 'hepph', 'hepth']
	color = []
	for name in header:
		if name == "hepth":
			color.append("#53aae5")
		elif name == "hepph":
			color.append("#D53515")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	plt.tight_layout()
	plt.show()

	plt.figure(figsize=(6, 2))
	X = np.array(df.index.values.tolist())
	header = ['mathoverflow', 'askubuntu', 'hepph', 'hepth',  'enron', 'email-eu', 'college_msg']
	color = []
	for name in header:
		if name == "enron":
			color.append("#53aae5")
		elif name == "email-eu":
			color.append("#D53515")
		elif name == "college_msg":
			color.append("#65b473")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	plt.tight_layout()
	plt.show()
	
	plt.figure(figsize=(6, 2))
	X = np.array(df.index.values.tolist())
	header = ['hepph', 'hepth', 'enron', 'email-eu', 'college_msg', 'mathoverflow', 'askubuntu']
	color = []
	for name in header:
		print(name)
		if name == "askubuntu":
			color.append("#53aae5")
		elif name == "mathoverflow":
			color.append("#D53515")
		else:
			color.append("#e5e5e5")
	for i, column in enumerate(header):
		plt.plot(X, df[column], marker = 'o', color=color[i], linewidth=1.8, linestyle='--')
	plt.gca().spines['right'].set_visible(False) 
	plt.gca().spines['top'].set_visible(False) 
	
	plt.ylim(-1.2, 1.2)
	plt.ylabel("Normalized\nSignificance", fontsize=15)
	plt.xlabel("Graphlet Transition Index", fontsize=15)
	plt.xticks(fontsize=15)
	plt.yticks(fontsize=15)
	
	plt.tight_layout()
	plt.show()

if __name__ == '__main__':
	import sys
	if len(sys.argv) == 2:
		if sys.argv[1] =='--a':
			transition_diagram()
		else:
			print("second argument must be '--a'")
	else:
		transition_diagram_small()
	#subgraph_ratio_profile()