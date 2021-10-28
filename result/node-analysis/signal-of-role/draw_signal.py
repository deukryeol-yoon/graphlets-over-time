import matplotlib.pyplot as plt
import os.path as osp
import numpy as np
import pandas as pd
import csv
import os
import seaborn as sns
from sklearn.linear_model import LinearRegression
pd.set_option("display.max_rows", None, "display.max_columns", None)

node_role = ["m1_1", "m1_2",
   "m2_1", "m2_2", "m2_3",
   "m3_1", "m3_2", "m3_3",
   "m4_1", "m4_2",
   "m5_1", "m5_2", "m5_3",
   "m6_1", "m6_2",
   "m7_1", "m7_2", "m7_3",
   "m8_1", "m8_2",
   "m9_1",
   "m10_1", "m10_2", "m10_3",
   "m11_1", "m11_2",
   "m12_1", "m12_2", "m12_3",
   "m13_1"]


title = {"between" : "Betweenness", "closeness" : "Closeness", "degree" : "In-degree", "pagerank" : "Pagerank", "edge-betweenness" : "Edge\nBetweenness"}

def draw(dataset_list, centrality_list):
   colors = sns.color_palette("bright", 30)
   total_one = 0
   total_minus = 0
   for centrality in centrality_list:
      list_slope = []
      for dataset in dataset_list:
         df = pd.DataFrame(columns=node_role, dtype=float)
         directory = osp.join(osp.abspath(""), dataset, centrality)
         files = os.listdir(directory)
         for csvfile in files:
            try:
               df_ = pd.read_csv(directory + "/" + csvfile, encoding='euc-kr')
               df = df.append(df_.loc[1, :], ignore_index=True) # degree 2 : df_.loc[0, :], degree 4 : df_.loc[1, :], degree 8 : df_.loc[2, :]
            except:
            	print("")
         if df.empty:
            continue
         
         ascending = []
         descending = []
         for column in df.columns:
            if all(earlier > later for earlier, later in zip(df[column], df[column][1:])):
               descending.append(column)
            elif all(earlier < later for earlier, later in zip(df[column], df[column][1:])):
               ascending.append(column)
      
         dataset_slope = []
         for column in df.columns:
                
            if column in ascending:
               dataset_slope.append(1)
            elif column in descending:
               dataset_slope.append(-1)
            else :
               dataset_slope.append(0)

         list_slope.append(dataset_slope)
      np_slope = np.array(list_slope)
      max_abs = max(abs(np_slope.min()), abs(np_slope.max()))
      fig, ax = plt.subplots(figsize=(6, 2))
      zero = 0
      one = 0
      minus_one = 0
      for i in range(7):
         for j in range(30):
            if list_slope[i][j] == 0:
               zero += 1
            elif list_slope[i][j] == 1:
               one += 1
            else:
               minus_one += 1
      print(centrality, one, minus_one)
      total_one += one
      total_minus += minus_one
      p = ax.pcolor(np_slope, vmin = -max_abs, vmax=max_abs, cmap= sns.color_palette("vlag", as_cmap=True), edgecolors='black')
      dataset_label=["HepPh", "HepTh", "Enron", "EU", "College", "Math", "Ask"]
      ax.set_yticks([0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5])
      ax.set_yticklabels(dataset_label)
      ax.set_xticks([4.5, 9.5, 14.5, 19.5, 24.5, 29.5])
      ax.set_xticklabels(["5", "10", "15", "20", "25", "30"])
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
      if centrality != "edge-betweenness":
         plt.xlabel("Node Role", fontsize=15)
      else:
         plt.xlabel("Edge Role", fontsize=15)
      plt.ylabel(title[centrality], fontsize=15)
      plt.tight_layout()
      plt.show()
   print(total_one, total_minus, total_one + total_minus)

draw(["hepph", "hepth", "email-eu", "enron", "college_msg", "mathoverflow", "askubuntu"], ["degree", "between", "closeness", "pagerank"])

