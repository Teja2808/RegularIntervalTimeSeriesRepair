import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static List<Double> time2ts(List<Double> originalSeq, double timeScale) {
        List<Double> tsList = new ArrayList<>();
        for (Double t : originalSeq) {
            long timeMillis = (long) ((t) * 1000);
            tsList.add((double) timeMillis * timeScale);
        }
        return tsList;
    }

    public static List<Double> equalSeriesGenerate(double epsT, double s0, int m) {
        List<Double> series = new ArrayList<>();
        List<Double> series1 = new ArrayList<>();
        List<Double> series2 = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            series.add(s0 + i * epsT);
        }
        return series;
    }

    public static double metricRes(List<Double> repair, List<Double> truth, List<Double> fault, String metricName) {
        if (metricName.equals("cost")) {
            double lmdA = 5 * (truth.get(1) - truth.get(0));
            double lmdD = 5 * (truth.get(1) - truth.get(0));
            return metrics.calCost(truth, repair, lmdA, lmdD);
        } 
        else if (metricName.equals("dtw")) 
        {
            return metrics.calDTW(truth, repair);
        }
        else if (metricName.equals("accuracy")) 
        { 
            return metrics.calAccuracy(truth, fault, repair);
        } 
        else
        {
            return metrics.calRMSE(truth, repair);
        }
    }
  

    public static void main(String args[]) {
        try{
            Map<String, Map<String, Object>> parameters = new HashMap<>();
            Map<String, Object> energyParams = new HashMap<>();
            energyParams.put("fileCounts", 5);
            energyParams.put("truthCol", 1);
            energyParams.put("truthDir", "../data/energy");
            energyParams.put("originalCol", 1);
            energyParams.put("originalDir", "../data/dirty_energy");
            energyParams.put("startPointGranularity", 1);
            energyParams.put("intervalGranularity", 1);
            energyParams.put("lmdA", 100.0);
            energyParams.put("lmdD", 100.0);
            parameters.put("energy", energyParams);

        String version = "-test";
        String[] datasets = {"energy"};
        String[] metrics = {"rmse","accuracy"};
        String[] methods = {"exact", "approximate"};
        boolean dataCharacteristic = false;
        Map<String, Map<String, Double>> resultDfs = new HashMap<>();
        for (String m : metrics) {
            Map<String, Double> methodMap = new HashMap<>();
            for (String method : methods) {
                methodMap.put(method, 0.0);
            }
            resultDfs.put(m, methodMap);
        }
        Map<String, Double> timeMap = new HashMap<>();
        for (String method : methods) {
            timeMap.put(method, 0.0);
        }
        resultDfs.put("time", timeMap);

        for (String dataset : datasets) {
            Map<String, Object> param = parameters.get(dataset);
            int fileCounts = (int) param.get("fileCounts");
            Map<String, List<Double>> resultMap = new HashMap<>();
            for (String method : methods) {
                for (String metric : metrics) {
                    resultMap.put(method + "-" + metric, new ArrayList<>());
                }
                resultMap.put(method + "-time", new ArrayList<>());
            }

            String datasetPath = "../result/" + dataset;
            System.out.println(new java.io.File(".").getAbsolutePath());
            if (!(new java.io.File(datasetPath).exists())) {
                new java.io.File(datasetPath).mkdir();
            }
            for (int ts = 0; ts < fileCounts; ts++) {
                System.out.println(ts);
                String originalDir = (String) param.get("originalDir");
                String fileName = originalDir + "/series_" + ts + ".csv";
                List<Double> originalSeq = readCSV(fileName, (int) param.get("originalCol"));
                List<Double> sourceValues = null;

                String truthDir = (String) param.get("truthDir");
                List<Double> groundTruthSeq = readCSV(truthDir + "/series_" + ts + ".csv", (int) param.get("truthCol"));
                List<Double> originalTime;
                List<Double> truthTime;
                if (param.containsKey("timeScale")) {
                    originalTime = time2ts(originalSeq, (double) param.get("timeScale"));
                    truthTime = time2ts(groundTruthSeq, (double) param.get("timeScale"));
                } else {
                    originalTime = originalSeq;
                    truthTime = groundTruthSeq;
                }

                double lmdA = (double) param.get("lmdA");
                double lmdD = (double) param.get("lmdD");
                int intervalGranularity = (int) param.get("intervalGranularity");
                int startPointGranularity = (int) param.get("startPointGranularity");

               double start = System.currentTimeMillis();
              //  double start = 1701631992.375159;
                
                
                double epsTE=0,s0E=0;
                int mE =0;
                    //System.out.println(exact.exact_repair(originalTime, lmdA, lmdD, intervalGranularity, startPointGranularity,start,mE));
                    double[] result = exact.exact_repair(originalTime, lmdA,lmdD,intervalGranularity,startPointGranularity,1.0,3.0); 
                    
                   epsTE = result[0];
                    s0E = result[1];
                    mE = (int)result[2];
                
                System.out.println(epsTE+" "+s0E+" "+mE);
                System.out.println("exact end");
                double end = System.currentTimeMillis();
                //double end = 1701632081.774293;
                double exactTime = (end - start) / 1000.0;

                  start = System.currentTimeMillis();
                //start = 1701631992.375159;
                //List<Double> epsTA = new ArrayList<>(), s0A = new ArrayList<>();
                double epsTA =0, s0A =0;
                int mA =0;
              //  List<Double> result = approximation.medianApproximationAll(originalTime, lmdA, lmdD, intervalGranularity);
                List<Double> result1 = approximation.medianApproximationAll(originalTime, lmdA, lmdD, intervalGranularity);
                epsTA = result1.get(1);
                s0A = result1.get(2);
                mA = result1.get(3).intValue();
                
                System.out.println(epsTA+" "+s0A+" "+mA);
                System.out.println("approximate end");
                end = System.currentTimeMillis();
                // end = 1701632081.774293;
                double approTime = (end - start) / 1000.0;

                List<Double> exactRes = equalSeriesGenerate(epsTE, s0E, mE);
                List<Double> approRes = equalSeriesGenerate(epsTA, s0A, mA);
                

                for (String metric : metrics) {
                    //System.out.println(metric + " exact");
                    resultMap.get("exact-" + metric).add(metricRes(exactRes, truthTime, originalTime, metric));
                    //System.out.println(metric + " approximate");
                    resultMap.get("approximate-" + metric).add(metricRes(approRes, truthTime, originalTime, metric));
                }
                resultMap.get("exact-time").add(exactTime);
               // System.out.println(resultMap.get("exact-time")+" exact-time");
                resultMap.get("approximate-time").add(approTime);
            }
            String[] metrics1 = {"rmse","accuracy","time"};
            for (String metric : metrics1) {
                resultDfs.get(metric).put("exact", mean(resultMap.get("exact-" + metric)));
                List<Double> list = resultMap.get("exact-" + metric);
              //  System.out.println(list+" list");
                String filePath = Paths.get(datasetPath, "exact-" + metric + version + ".txt").toString();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    for (Double number : list) {
                        writer.write(number.toString());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                resultDfs.get(metric).put("approximate", mean(resultMap.get("approximate-" + metric)));
                List<Double> list1 = resultMap.get("approximate-" + metric);
                String filePath1 = Paths.get(datasetPath, "approximate-" + metric + version + ".txt").toString();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath1))) {
                    for (Double number : list1) {
                        writer.write(number.toString());
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
            for(String metric: metrics1){
                saveToCSV( datasetPath ,"exp1-"+ metric + version+".csv", resultDfs.get(metric));
            }
            saveToCSV(datasetPath,"exp1-time"+ version +".csv",resultDfs.get("time"));
        }}
        catch (Exception e) {
             e.printStackTrace();
         }
        }

    public static List<Double> readCSV(String fileName, int col) {
        List<Double> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(values[col].equals("Error") || values[col].equals("date")){
                    continue;
                }
               // System.out.println(values[col]);
                result.add(Double.parseDouble(values[col]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double mean(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static void saveToCSV(String resultPath, String fileName, Map<String, Double> data) throws IOException {
        try (FileWriter csvWriter = new FileWriter(resultPath + File.separator + fileName)) {
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                csvWriter.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }
}
