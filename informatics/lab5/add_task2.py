import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


df = pd.read_csv("data.csv")
fig, ax = plt.subplots(1, 3,figsize=(16, 4))
df.groupby(['<DATE>']).boxplot(column="<OPEN>,<HIGH>,<LOW>,<CLOSE>".split(","), ax=ax)
plt.savefig("add_task2.png")