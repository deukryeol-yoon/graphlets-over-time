from __future__ import division
#from matplotlib import pyplot as plt
import numpy as np
import sys, getopt
import random
import os.path as osp
import operator
import csv
import math
from os import listdir
import os
import pandas as pd
import torch
import torch.optim as optim
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import confusion_matrix, classification_report
import matplotlib.pyplot as plt
import seaborn as sns
from itertools import groupby
from sklearn import svm
from sklearn.metrics import average_precision_score
from sklearn.metrics import f1_score
from sklearn.metrics import recall_score
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics
from sklearn.feature_selection import RFE
from sklearn.decomposition import PCA
from sklearn.metrics import roc_auc_score
from tqdm import tqdm

def test(X, Y, seed):
    X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size = 0.2, random_state=seed)

    scaler = StandardScaler()
    X_train = scaler.fit(X_train).transform(X_train)
    X_test = scaler.transform(X_test)

    clf = RandomForestClassifier(max_depth=10, n_estimators = 30, random_state=seed)
    clf.fit(X_train, Y_train)
    auroc_rf = roc_auc_score(Y_test, clf.predict_proba(X_test)[:,1])
    return auroc_rf

def prediction(dataset, centrality_measure, target_degree_list):
    print("dataset : " + dataset)
    current_path = osp.abspath("")
    result_folder = osp.join(current_path, dataset, "prediction", "result") 
    if (osp.isdir(result_folder) == False):
        os.makedirs(result_folder)

    for measure in centrality_measure:
        print("centrality : " + measure)
        importance_path = osp.join(current_path, '..',"..", "..", 'data', 'centrality', dataset + "-" + measure+".csv")
        assert osp.isfile(importance_path)

        # Find importance_threshold
        importance_value = list(csv.reader(open(importance_path)))
        importance_value = sorted(importance_value, key=lambda x : float(x[1]), reverse=True)
        importance_threshold = float(importance_value[int(len(importance_value) * 0.2)][1])
        dict_value = {}
        for (node, value) in importance_value:
            dict_value[int(node)] = float(value)

        for target_degree in target_degree_list:
            print("target_degree :" +str(target_degree))
            writerow = [target_degree]
            path = osp.join(current_path, dataset, '{0:04d}.csv'.format(target_degree))
            reader = csv.reader(open(osp.join(current_path, dataset, '{0:04d}.csv'.format(target_degree))))
            data = list(reader)
            feature_name = data[0]
            value = data[1:]
            y = []
            for line in value:
                l = list(map(float, line))
                if dict_value[l[0]] > importance_threshold:
                    y.append(1)
                else :
                    y.append(0)
            X = pd.DataFrame(value)
            y = pd.DataFrame(y)
            y = y.iloc[:, -1]

            try:
                # 1. node role features
                arr = []
                feature = X.iloc[:, 3:33]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("Local-NR: ",round(np.mean(arr), 2), round(np.std(arr), 3))

                # 2. Local Features of Node Prominence Profile
                arr = []
                feature = X.iloc[:, 63:66]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("Local-NPP: ",round(np.mean(arr), 2), round(np.std(arr), 3))

                # 3. node role features with global information
                arr = []
                feature = X.iloc[:, 3:63]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("Global-NR: ",round(np.mean(arr), 2), round(np.std(arr), 3))

                # 4. Node Prominence Profile
                arr = []
                feature = X.iloc[:, 63:68]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("Global-NPP: ",round(np.mean(arr), 2), round(np.std(arr), 3))

                # 4. Global Features
                arr = []
                feature = X.iloc[:, 1:3]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("Global-Simple: ",round(np.mean(arr), 2), round(np.std(arr), 3))
                
                # 6. all features
                arr = []
                feature = X.iloc[:, 1:-1]
                for i in range(0, 10):
                    arr.append(test(feature, y, i))
                print("ALL: ",round(np.mean(arr), 2), round(np.std(arr), 3))

            except:
                print("Exception Occur\n")
                continue


if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        dataset = sys.argv[1]
        centrality = [sys.argv[2]]
        target_degree = [int(sys.argv[3])]
        prediction(dataset, centrality, target_degree)
    else:
        prediction("hepph", ["degree", "between", "closeness", "pagerank"], [2, 4, 8])
        prediction("hepth", ["degree", "between", "closeness", "pagerank"], [2, 4, 8])
        prediction("enron", ["degree", "between", "closeness", "pagerank"],  [2, 4, 8])
        prediction("email-eu", ["degree", "between", "closeness", "pagerank"],  [2, 4, 8])
        prediction("college_msg", ["degree", "between", "closeness", "pagerank"], [2, 4, 8])
        prediction("mathoverflow", ["degree", "between", "closeness", "pagerank"],  [2, 4, 8])
        prediction("askubuntu", ["degree", "between", "closeness", "pagerank"],  [2, 4, 8])