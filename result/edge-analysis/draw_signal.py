import matplotlib.pyplot as plt
import os.path as osp
import numpy as np
import pandas as pd
import csv
import os
import seaborn as sns
from sklearn.linear_model import LinearRegression
from scipy.stats import spearmanr
import math
pd.set_option("display.max_rows", None, "display.max_columns", None)

edge_role = ["m1_1", "m1_2",
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
   for centrality in centrality_list:
      list_slope = []
      sum_abs_coeff = 0
      num_valid_coeff = 0
      for dataset in dataset_list:
         df = pd.DataFrame(columns=edge_role, dtype=float)
         directory = osp.join(osp.abspath(""), dataset, centrality)
         files = os.listdir(directory)
         for csvfile in files:
            try:
               df_ = pd.read_csv(directory + "/" + csvfile, encoding='euc-kr')
               df = df.append(df_.loc[2, :], ignore_index=True) # degree 2 : df_.loc[0, :], degree 4 : df_.loc[1, :], degree 8 : df_.loc[2, :]
            except:
               pass
         if df.empty:
            continue
         
         rank = [i for i in range(len(df))]
         rank_correlation = []
         for column in df.columns:
            coef, p = spearmanr(rank, df[column])
            rank_correlation.append(coef)
            if not math.isnan(coef):
               sum_abs_coeff += abs(coef)
               num_valid_coeff += 1
         dataset_slope = rank_correlation
         list_slope.append(dataset_slope)
      print(centrality, num_valid_coeff, sum_abs_coeff / num_valid_coeff)
      np_slope = np.array(list_slope)
      np_slope[np.isnan(np_slope)]=0

      fig, ax = plt.subplots(figsize=(6, 2))
      p = ax.pcolor(np_slope, vmin = -1, vmax=1, cmap= sns.color_palette("vlag", as_cmap=True), edgecolors='black')
      cbar = plt.colorbar(p, ticks=[-1, -0.5, 0.0, 0.5, 1])

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
      plt.savefig('edge-betweenness.pdf')
      #plt.show()

draw(["hepph", "hepth", "email-eu", "enron", "college_msg", "mathoverflow", "askubuntu"], ["edge-betweenness"])
