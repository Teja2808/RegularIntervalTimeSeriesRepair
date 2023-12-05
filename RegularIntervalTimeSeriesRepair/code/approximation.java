import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class approximation {

    public static double determineInterval(List<Double> t, double intervalGranularity) {
        List<Double> epsList = new ArrayList<>();
        for (int i = 1; i < t.size(); i++) {
            epsList.add(t.get(i) - t.get(i - 1));
        }
        double[] epsArray = epsList.stream().mapToDouble(Double::doubleValue).toArray();
        double epsMd = median(epsArray);
        double eps = Math.round(epsMd / intervalGranularity) * intervalGranularity;
        return eps;
    }

    public static List<Double> startPointApproximation(List<Double> t, double lmdA, double lmdD, double intervalGranularity) {
        int n = t.size();
        double epsT = determineInterval(t, intervalGranularity);
        double s0 = t.get(0);

        List<List<Double>> dp = new ArrayList<>();
        List<List<Integer>> op = new ArrayList<>();

        for (int i = 0; i <= n; i++) {
            dp.add(new ArrayList<>());
            op.add(new ArrayList<>());
        }

        dp.get(0).add(0.0);
        op.get(0).add(0);

        for (int i = 1; i <= n; i++) {
            dp.get(i).add(i * lmdD);
            op.get(i).add(2);
        }

        double mBest = 10e8;
        double mUb = 10e8;
        double minCost = 10e8;
        int m = 1;

        while (m <= mUb) {
            dp.get(0).add(m * lmdA);
            op.get(0).add(1);

            for (int i = 1; i <= n; i++) {
                double sM = s0 + (m - 1) * epsT;
                double moveRes = dp.get(i - 1).get(m - 1) + Math.abs(t.get(i - 1) - sM);
                double addRes = dp.get(i).get(m - 1) + lmdA;
                double delRes = dp.get(i - 1).get(m) + lmdD;

                if (moveRes <= addRes && moveRes <= delRes) {
                    dp.get(i).add(moveRes);
                    op.get(i).add(0);
                } else if (addRes <= moveRes && addRes <= delRes) {
                    dp.get(i).add(addRes);
                    op.get(i).add(1);
                } else {
                    dp.get(i).add(delRes);
                    op.get(i).add(2);
                }
            }

            if (dp.get(n).get(m) < minCost) {
                minCost = dp.get(n).get(m);
                mBest = m;
                mUb = Math.floor(minCost / lmdA) + n;
            }

            m++;
        }

        List<Double> result = new ArrayList<>();
        result.add(minCost);
        result.add(epsT);
        result.add(s0);
        result.add((double) mBest);

        return result;
    }

    public static List<Double> medianApproximation(List<Double> t, double lmdA, double lmdD, double intervalGranularity) {
        int n = t.size();
        double epsT = determineInterval(t, intervalGranularity);
        //System.out.println(epsT);
        int nMd = (int) Math.floor(n / 2.0);
        double sMd = median(t.stream().mapToDouble(Double::doubleValue).toArray());

        List<List<Double>> dpL = new ArrayList<>();
        List<List<Double>> dpR = new ArrayList<>();
        List<List<Integer>> opL = new ArrayList<>();
        List<List<Integer>> opR = new ArrayList<>();

        for (int i = 0; i <= nMd; i++) {
            dpL.add(new ArrayList<>());
            dpR.add(new ArrayList<>());
            opL.add(new ArrayList<>());
            opR.add(new ArrayList<>());
        }

        dpL.get(0).add(0.0);
        opL.get(0).add(0);
        dpR.get(0).add(0.0);
        opR.get(0).add(0);

        for (int i = 1; i <= nMd; i++) {
            dpL.get(i).add(i * lmdD);
            opL.get(i).add(2);
            dpR.get(i).add(i * lmdD);
            opR.get(i).add(2);
        }

        double mBest = 10e8;
        double mUb = 10e8;
        double minCost = 10e8;
        int m = 1;

        while (m <= mUb) {
            //System.out.println("approximate"+ m);
            dpL.get(0).add(m * lmdA);
            opL.get(0).add(1);
            dpR.get(0).add(m * lmdA);
            opR.get(0).add(1);

            for (int i = 1; i <= nMd; i++) {
                double sML = sMd - m * epsT;
                double sMR = sMd + m * epsT;
                double tIL, tIR;

                if (n % 2 == 1) {
                    sML = sMd - m * epsT;
                    sMR = sMd + m * epsT;
                    tIL = t.get((n - 1) / 2 - i);
                    tIR = t.get((n + 1) / 2 + (i - 1));
                } else {
                    sML = sMd - (m-0.5) * epsT;
                    sMR = sMd + (m-0.5) * epsT;
                    tIL = t.get(n / 2 - i);
                    tIR = t.get(n / 2 + i - 1);
                }

                double moveResL = dpL.get(i - 1).get(m - 1) + Math.abs(tIL - sML);
                double moveResR = dpR.get(i - 1).get(m - 1) + Math.abs(tIR - sMR);
                double addResL = dpL.get(i).get(m - 1) + lmdA;
                double addResR = dpR.get(i).get(m - 1) + lmdA;
                double delResL = dpL.get(i - 1).get(m) + lmdD;
                double delResR = dpR.get(i - 1).get(m) + lmdD;

                double minResL = Math.min(moveResL, Math.min(addResL, delResL));
                if (moveResL == minResL) {
                    dpL.get(i).add(moveResL);
                    opL.get(i).add(0);
                } else if (addResL == minResL) {
                    dpL.get(i).add(addResL);
                    opL.get(i).add(1);
                } else {
                    dpL.get(i).add(delResL);
                    opL.get(i).add(2);
                }

                double minResR = Math.min(moveResR, Math.min(addResR, delResR));
                if (moveResR == minResR) {
                    dpR.get(i).add(moveResR);
                    opR.get(i).add(0);
                } else if (addResR == minResR) {
                    dpR.get(i).add(addResR);
                    opR.get(i).add(1);
                } else {
                    dpR.get(i).add(delResR);
                    opR.get(i).add(2);
                }
            }

            if (dpR.get(nMd).get(m) + dpL.get(nMd).get(m) < minCost) {
                minCost = dpR.get(nMd).get(m) + dpL.get(nMd).get(m);
                mBest = m;
                if (n % 2 == 1) {
                    mUb = (int) (Math.floor(minCost / lmdA + n) - 1) / 2;
                } else {
                    mUb = (int) (Math.floor(minCost / lmdA + n)) / 2;
                }
            }
            m++;
        }

        List<Double> result = new ArrayList<>();
        if (n % 2 == 1) {
            double s0 = sMd - mBest * epsT;
            result.add(minCost);
            result.add(epsT);
            result.add(s0);
            result.add(mBest * 2 + 1.0);
        } else {
            double s0 = sMd - (mBest - 0.5) * epsT;
            result.add(minCost);
            result.add(epsT);
            result.add(s0);
            result.add(mBest * 2.0);
        }

        return result;
    }

    public static List<Integer> traceBack(List<List<Integer>> op, List<Double> t, List<Double> s) {
        //double s0 = s.get(0);
        //double epsT = s.get(1);
        int mBest = s.get(3).intValue();
        int n = t.size();
        List<Integer> M = new ArrayList<>();

        int i = n - 1;
        int j = mBest - 1;

        while (i >= 0 && j >= 0) {
            if (op.get(i).get(j) == 0) {
                M.add(i,j);
                i = i - 1;
                j = j - 1;
            } else if (op.get(i).get(j) == 1) {
                M.add(-1,j);
                j = j - 1;
            } else {
                M.add(i,-1);
                i = i - 1;
            }
        }
        M.sort((a, b) -> a - b);
        return M;
    }

    public static List<Double> medianApproximationAll(List<Double> t, double lmdA, double lmdD, double intervalGranularity) {
        System.out.println("approximate:median");
        List<Double> medianResult = medianApproximation(t, lmdA, lmdD, intervalGranularity);

        System.out.println("approximate:start point");
        List<Double> startPointResult = startPointApproximation(t, lmdA, lmdD, intervalGranularity);

        if (medianResult.get(0) <= startPointResult.get(0)) {
            return medianResult;
        } else {
            return startPointResult;
        }
    }


    public static double median(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Input array is empty or null");
        }

        int length = array.length;
        double[] sortedArray = Arrays.copyOf(array, length);
        Arrays.sort(sortedArray);

        int middle = length / 2;
        return (length % 2 == 0) ? (sortedArray[middle - 1] + sortedArray[middle]) / 2.0 : sortedArray[middle];
    }

}