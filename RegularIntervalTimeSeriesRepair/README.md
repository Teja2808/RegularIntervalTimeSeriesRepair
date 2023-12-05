
## README
#### File Sturcture

- `code/` provides the codes for the experiments
  - `code/exact.py`: the exact method implementation.
  - `code/approximate.py`: the median approximation algorithm implementation.
  - `code/metrics.py`: metrics for evaluation
  - `code/main.py`: the main evaluation entry
- `data/` provides the datasets used in the experiments
  - `energy, pm, air_quality` are the ground truth datasets
  - `dirty_xxx` are the datasets with errors
- `result/`provides the results for specific datasets


#### Prerequisites

- Install java in the system.
- Install VSCode.
- Clone the git respository and open it in VSCode.


#### Example Invocation


```powershell
javac Main.java
java Main
```


#### Run the Code for specific datasets

Initially, the Main.java is set for energy dataset.

To run the code for other datasets follow the below steps:

- Open the Main.java file

- go to line 60 and change the "energyParams.put("truthDir", "../data/energy")" to "energyParams.put("truthDir", "../data/`datasetname`")"

- go to line 62 and change the "energyParams.put("originalDir", "../data/dirty_energy")" to "energyParams.put("originalDir", "../data/`dirty_datasetname`")"

- go to line 67 and change the "parameters.put("energy", energyParams)" to "parameters.put(`datasetname`, energyParams)"

- go to line 70 and change the "String[] datasets = {"energy"}" to "String[] datasets = {"`datasetname`"}"

The results of the ran datasets will automatically moved to the results folder.

#### Result folder

- The result folder shows the results for three datasets energy, pm, air_quality.
- Open the specific datasets to check the result of dataset.
- exp1-accuracy-test.csv - shows the accuracy of particular dataset for both exact and approximation method.
- exp1-rmse-test.csv - shows the rmse of particular dataset for both exact and approximation method.
- exp1-time-test.csv - shows the time of particular dataset for both exact and approximation method.


