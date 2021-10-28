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


def prediction(dataset, centrality_measure, target_feature_list):
    current_path = osp.abspath("")
    result_folder = osp.join(current_path, dataset, "prediction", "result") 
    if (osp.isdir(result_folder) == False):
        os.makedirs(result_folder)

    for measure in centrality_measure:
        importance_path = osp.join(current_path, "..", "..", "data", "centrality", dataset + "-" + measure+".csv")
        assert osp.isfile(importance_path)

        # Make Result Folder & File
        result_path = osp.join(result_folder,  "feature_result-" + measure +".csv")

        # Find importance_threshold
        importance_value = list(csv.reader(open(importance_path)))
        importance_value = sorted(importance_value, key=lambda x : float(x[1]), reverse=True)
        importance_threshold = float(importance_value[int(len(importance_value) * 0.2)][2])
        dict_value = {}
        for (src, dst, value) in importance_value:
            dict_value[(float(src), float(dst))] = float(value)

        for target_feature in target_feature_list:
            writerow = [target_feature]
            path = osp.join(current_path, dataset, 'edge-indegree' + str(target_feature) + '.csv')
            reader = csv.reader(open(path))
            data = list(reader)
            feature_name = data[0]
            value = data[1:]
            y = []
            for i, line in enumerate(value):
                line = list(map(float, line))
                if dict_value[(line[0], line[1])] > importance_threshold:
                    y.append(1)
                else :
                    y.append(0)
            X = pd.DataFrame(value)
            y = pd.DataFrame(y)
            y = y.iloc[:, -1]
            
            # Local-Edge Role
            arr = []
            feature = X.iloc[:, 4:34]
            for i in range(0, 10):
                arr.append(test(feature, y, i))
            print(round(np.mean(arr), 2), round(np.std(arr), 3))
            
            # Global-Edge Role
            feature = X.iloc[:, 4:]
            for i in range(0, 10):
                arr.append(test(feature, y, i))
            print(round(np.mean(arr), 2), round(np.std(arr), 3))

            # Global-Basic
            feature = X.iloc[:, 2:3]
            for i in range(0, 10):
                arr.append(test(feature, y, i))
            print(round(np.mean(arr), 2), round(np.std(arr), 3))

            # ALL
            feature = X.iloc[:, 2:]
            for i in range(0, 10):
                arr.append(test(feature, y, i))
            print("************ALL***************")
            print(round(np.mean(arr), 2), round(np.std(arr), 3))
            print("******************************")


if __name__ == '__main__':
    if len(sys.argv) > 1:
        dataset = sys.argv[1]
        target_degree = [sys.argv[2]]
        #prediction(dataset, ["degree", "between", "closeness", "pagerank"], [2])
        prediction(dataset, ["edge-betweenness"], target_degree)
    else:
        prediction("hepph", ["edge-betweenness"], [2, 4, 8])
        prediction("hepth", ["edge-betweenness"], [2, 4, 8])
        prediction("enron", ["edge-betweenness"], [2, 4, 8])
        prediction("email-eu", ["edge-betweenness"], [2, 4, 8])
        prediction("college_msg", ["edge-betweenness"],[2, 4, 8])
        prediction("mathoverflow", ["edge-betweenness"], [2, 4, 8])
        prediction("askubuntu", ["edge-betweenness"],[2, 4, 8])
