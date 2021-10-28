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
import networkx as nx
import itertools
import math
title = {"hepph": "HepPh", "hepth" : "HepTh", "patent" : "Patent", "email-eu" : "EU", "enron" : "Enron", "college_msg" : "College", "askubuntu" : "Ask", "mathoverflow" : "Math", "stackoverflow" : "Stack"}

import numpy as np
import matplotlib
import matplotlib.pyplot as plt
from mpl_toolkits.axes_grid1 import AxesGrid

def shiftedColorMap(cmap, start=0, midpoint=0.5, stop=1.0, name='shiftedcmap'):
    '''
    Function to offset the "center" of a colormap. Useful for
    data with a negative min and positive max and you want the
    middle of the colormap's dynamic range to be at zero.

    Input
    -----
      cmap : The matplotlib colormap to be altered
      start : Offset from lowest point in the colormap's range.
          Defaults to 0.0 (no lower offset). Should be between
          0.0 and `midpoint`.
      midpoint : The new center of the colormap. Defaults to 
          0.5 (no shift). Should be between 0.0 and 1.0. In
          general, this should be  1 - vmax / (vmax + abs(vmin))
          For example if your data range from -15.0 to +5.0 and
          you want the center of the colormap at 0.0, `midpoint`
          should be set to  1 - 5/(5 + 15)) or 0.75
      stop : Offset from highest point in the colormap's range.
          Defaults to 1.0 (no upper offset). Should be between
          `midpoint` and 1.0.
    '''
    cdict = {
        'red': [],
        'green': [],
        'blue': [],
        'alpha': []
    }

    # regular index to compute the colors
    reg_index = np.linspace(start, stop, 257)

    # shifted index to match the data
    shift_index = np.hstack([
        np.linspace(0.0, midpoint, 128, endpoint=False), 
        np.linspace(midpoint, 1.0, 129, endpoint=True)
    ])
    for ri, si in zip(reg_index, shift_index):
        r, g, b, a = cmap(ri)

        cdict['red'].append((si, r, r))
        cdict['green'].append((si, g, g))
        cdict['blue'].append((si, b, b))
        cdict['alpha'].append((si, a, a))

    newcmap = matplotlib.colors.LinearSegmentedColormap(name, cdict)
    plt.register_cmap(cmap=newcmap)

    return newcmap

def draw_GTG(dataset):
	plt.figure(figsize=(12, 8))
	motifs = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"]

	edges = ["3-10", "2-9", "2-7", "2-5", "8-12", "4-7", "new-3", "new-2", "new-1", 
		"new-7", "new-4", "9-10", "10-12", "4-5", "2-3", "11-12", "3-8", "6-12", 
		"5-10", "1-5", "3-6", "5-11", "12-13", "7-10", "5-6", "7-11", "7-8", "1-3"]

	G = nx.DiGraph()

	layers = {"new" :0,  
		"1": 1, "4": 1, "2": 1, 
		"3": 2, "5": 2, "7": 2, "9": 2,
		"6": 3, "10": 3, "8": 3, "11": 3,
		"12": 4,
		"13": 5}

	for edge in edges:
		src, dst = edge.rstrip().split("-")
		G.add_edge(src, dst)
	for node in G.nodes:
		node_attribute = layers[node]
		G.nodes[node]["layer"] = node_attribute
	pos = nx.multipartite_layout(G, subset_key="layer")

	def swap(a, b):
		temp = pos[a]
		pos[a] = pos[b]
		pos[b] = temp

	swap('1', '2')
	swap('5', '9')
	swap('6', '10')
	swap('10', '8')
	swap('8', '11')


	path = osp.join(osp.abspath(""),  dataset, "change.csv")
	real_items = list(csv.reader(open(path, newline="")))
	header = real_items[0]
	real_motif = np.array(list(map(int, real_items[-1])))
	motif_ratio = [math.log((x/np.sum(real_motif) )+1)  for x in real_motif]

	edge_width = {}
	for i, edge in enumerate(header):
		v1, v2 = edge.rstrip().replace('m', '').split("-")
		edge_width[(v1, v2)] = motif_ratio[i]

	weight = []
	for edge in G.edges:
		weight.append(edge_width[edge])

	from matplotlib.colors import ListedColormap, BoundaryNorm
	from matplotlib.collections import LineCollection

	colormap = matplotlib.cm.Reds
	shifted_cmap = shiftedColorMap(colormap, midpoint=0.05, name='shifted')

	ax = plt.gca()
	vmin = 0.0
	vmax = 0.6
	nx.draw(G, pos, with_labels=False, node_size=300, node_color=["#000000" for i in range(len(G.nodes))], edge_color=weight, edge_cmap=shifted_cmap, width=3, edge_vmin = vmin, edge_vmax = vmax)
	sm = plt.cm.ScalarMappable(cmap=shifted_cmap)
	sm.set_clim(vmin=vmin, vmax=vmax)
	cbar = plt.colorbar(sm, ticks=[0.0, 0.3, 0.6])
	cbar.ax.set_yticklabels([0.0, 0.3, 0.6])
	cbar.ax.tick_params(labelsize=30)

	for node in G.nodes:
		pos[node][0] -= 0.02
		pos[node][1] += 0.08

	nx.draw_networkx_labels(G, pos=pos, font_size=30, horizontalalignment='left') 
	ax.set_title(title[dataset], size=40)
	plt.tight_layout()
	plt.show()

if __name__ == '__main__':
	import sys
	if len(sys.argv) > 1:
		dataset = sys.argv[1]
		draw_GTG(dataset)
	else :
		draw_GTG('hepph')
	#dict_dataset = {'citation' : ['hepph', 'hepth', 'patent'],
	#		'email' : ['enron', 'email-eu', 'college_msg'],
	#		'interaction' : ['askubuntu', 'mathoverflow', 'stackoverflow']}
	#draw_GTG(dict_dataset['citation'])
	#draw_GTG(dict_dataset['email'])
	#draw_GTG(dict_dataset['interaction'])