import java.util.ArrayList;
import java.util.List;

public class metrics {

    public static double calRMSE(List<Double> truth, List<Double> repair) {
        int minLength = Math.min(truth.size(), repair.size());
        truth = truth.subList(0, minLength);
        repair = repair.subList(0, minLength);

        double sum = 0.0;
        for (int i = 0; i < minLength; i++) {
            double diff = Math.pow(Math.abs(truth.get(i) - repair.get(i)), 2);
            sum += diff;
        }

        return Math.sqrt(sum / minLength);
    }

    public static double calAccuracy(List<Double> truth, List<Double> fault, List<Double> repair) {
        int minLength = Math.min(Math.min(truth.size(), fault.size()), repair.size());
        truth = truth.subList(0, minLength);
        fault = fault.subList(0, minLength);
        repair = repair.subList(0, minLength);

        double error = 0.0;
        double cost = 0.0;
        double inject = 0.0;

        for (int i = 0; i < minLength; i++) {
            error += Math.abs(truth.get(i) - repair.get(i));
            cost += Math.abs(fault.get(i) - repair.get(i));
            inject += Math.abs(truth.get(i) - fault.get(i));
        }

        if (error == 0) {
            return 100.0;
        }

        return 100*(1.0 - (error / (cost + inject))) ;
    }

    public static double calDTW(List<Double> truth, List<Double> repair) {
        double[][] d = calculateDTW(truth, repair);
        return d[truth.size() - 1][repair.size() - 1];
        //check
    }

    public static double calCost(List<Double> truth, List<Double> repair, double lmdA, double lmdD) {
        int n = repair.size();
        int m = truth.size();
        List<List<Double>> dp = new ArrayList<>();

        lmdA = lmdA * (truth.get(1) - truth.get(0));
        lmdD = lmdD * (truth.get(1) - truth.get(0));

        for (int i = 0; i <= n; i++) {
            dp.add(new ArrayList<>());
        }

        dp.get(0).add(0.0);
        for (int i = 1; i <= n; i++) {
            dp.get(i).add(i * lmdD);
        }

        for (int j = 1; j <= m; j++) {
            dp.get(0).add(j * lmdA);
            for (int i = 1; i <= n; i++) {
                double sM = truth.get(j - 1);
                double moveRes = dp.get(i - 1).get(j - 1) + Math.abs(repair.get(i - 1) - sM);
                double addRes = dp.get(i).get(j - 1) + lmdA;
                double delRes = dp.get(i - 1).get(j) + lmdD;

                if (moveRes <= addRes && moveRes <= delRes) {
                    dp.get(i).add(moveRes);
                } else if (addRes <= moveRes && addRes <= delRes) {
                    dp.get(i).add(addRes);
                } else {
                    dp.get(i).add(delRes);
                }
            }
        }

        return dp.get(n).get(m);
    }

    public static double[][] calculateDTW(List<Double> s1, List<Double> s2) {
        int m = s1.size();
        int n = s2.size();
        double[][] dp = new double[m][n];

        for (int i = 0; i < m; i++) {
            dp[i][0] = Math.abs(s1.get(i) - s2.get(0));
        }
        for (int j = 0; j < n; j++) {
            dp[0][j] = Math.abs(s1.get(0) - s2.get(j));
        }

        for (int j = 1; j < n; j++) {
            for (int i = 1; i < m; i++) {
                dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1], dp[i - 1][j]), dp[i][j - 1])
                        + Math.abs(s1.get(i) - s2.get(j));
            }
        }

        return dp;
    }
}