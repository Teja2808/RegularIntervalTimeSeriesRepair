import java.util.*;

public class exact {
    static double g_cost,g_m;
    public static double determineInterval(List<Double> t) {
        List<Double> eps = new ArrayList<>();
        for (int i = 1; i < t.size() - 1; i++) {
            eps.add(t.get(i) - t.get(i - 1));
        }
        return median(eps);
    }

    public static List<List<Integer>> matchSearching(List<Double> t, double eps_t, double s_0, double lmd_a, double lmd_d) {
        int n = t.size();
        
        
        // System.out.println(eps_t);
        // System.out.println(s_0);
        // System.out.println(lmd_a);
        // System.out.println(lmd_d);
        
        
        
        
        List<List<Double>> dp = new ArrayList<>();
        List<List<Integer>> op = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            dp.add(new ArrayList<>());
            op.add(new ArrayList<>());
        }
        dp.get(0).add(0.0);
        
    //why double
        op.get(0).add(0);
        for (int i = 1; i <= n; i++) {
            dp.get(i).add((double) (i * lmd_d));
            op.get(i).add(2);
        }
        double mBest = 10e8;
        double mUb = 10e8;
        double minCost = 10e8;
        int m = 1;
        
        while (m <= mUb) {
            dp.get(0).add(m * lmd_a);
            op.get(0).add(1);
            for (int i = 1; i <= n; i++) {
                double sM = s_0 + (m - 1) * eps_t;
                double moveRes = dp.get(i - 1).get(m - 1) + Math.abs(t.get(i - 1) - sM);
                double addRes = dp.get(i).get(m - 1) + lmd_a;
                double delRes = dp.get(i - 1).get(m) + lmd_d;
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
                mUb = Math.floor(minCost / lmd_a) + n;
            }
            
            m++;
        }
        g_cost = minCost;
        g_m = mBest;
        
        return traceBack(op, t, s_0, eps_t, mBest);
    }

    public static List<List<Integer>> traceBack(List<List<Integer>> op, List<Double> t, double s_0, double eps_t, double mBest) {
        int n = t.size();
        List<List<Integer>> M = new ArrayList<>();
        int i = n;
        int j = (int) mBest;
        while (i > 0 && j > 0) {
            if (op.get(i).get(j) == 0) {
                List<Integer> pair = new ArrayList<>();
                pair.add(i - 1);
                pair.add(j - 1);
                M.add(pair);
                i = i - 1;
                j = j - 1;
            } else if (op.get(i).get(j) == 1) {
                List<Integer> pair = new ArrayList<>();
                pair.add(-1);
                pair.add(j - 1);
                M.add(pair);
                j = j - 1;
            } else {
                List<Integer> pair = new ArrayList<>();
                pair.add(i - 1);
                pair.add(-1);
                M.add(pair);
                i = i - 1;
            }
        }
        Collections.reverse(M);
        return M;
    }

    public static double roundToGranularity(double value, double granularity) {
       // System.out.println(value+"in method"+granularity +" "+Math.round(value / granularity) * granularity);
        return Math.round(value / granularity) * granularity;
    }

    public static double[] exact_repair(List<Double> t, double lmd_a, double lmd_d, double intervalGranularity,
            double startPointGranularity, double biasD, double biasS) {
        List<Double> epsList = new ArrayList<>();
        for (int i = 1; i < t.size() - 1; i++) {
            epsList.add(t.get(i) - t.get(i - 1));
        }
        double epsMd = median(epsList);
       // System.out.println(epsMd+" gran "+intervalGranularity);
        double epsT = roundToGranularity(epsMd, intervalGranularity);
        //System.out.println("final1 "+epsT);
        Set<Double> epsTTraverseRange = new HashSet<>();
        Set<Double> epsTTraversed = new HashSet<>(); 
        double minCost = 1e8;
        boolean minCostChange = false;
        double cost=0.0,m=0.0,m_best=0.0,min_eps_t=0,min_s_0=0.0;
        while (true) {
            int d = 0;
            while ((d == 0 || checkStLb(d, epsList, minCost, lmd_d, epsT)) && d < epsList.size() && d < biasD) {
                double s_0 = t.get(d);
                boolean flagIncrease = false;
                boolean flagDecrease = false;
                 
                while (s_0 <= t.get(d) + biasS) {
                    
                    List<List<Integer>> M = matchSearching(t, epsT, s_0, lmd_a, lmd_d);
                    cost = g_cost;
                    m = g_m;
                    if (cost < minCost) {
                        minCost = cost;
                        m_best = m;
                        min_eps_t = epsT;
                        min_s_0 = s_0;
                        minCostChange = true;
                        s_0 += startPointGranularity;
                    } else {
                        flagIncrease = true;
                        break;
                    }
                }
               
                s_0 = t.get(d) - 1;
                while (s_0 >= t.get(d) - biasS) {
                    s_0 -= startPointGranularity;
                    List<List<Integer>> M = matchSearching(t, epsT, s_0, lmd_a, lmd_d);
                    cost = g_cost;
                    m = g_m;
                    
                    if (cost < minCost) {
                        minCost = cost;
                        m_best = m;
                        min_eps_t = epsT;
                        min_s_0 = s_0;
                        
                        minCostChange = true;
                    } else {
                        flagDecrease = true;
                        break;
                    }
                }
                if (flagIncrease && flagDecrease) {
                    break;
                }
                d += 1;
            }

            if (!minCostChange || (!checkIntervalLb(epsT, minCost, epsList))) {
                break;
            }
            
            if (!epsTTraverseRange.contains(epsT + intervalGranularity)
                    && (epsT + intervalGranularity) <= roundToGranularity(epsMd, intervalGranularity)
                            + intervalGranularity) {
                epsTTraverseRange.add(epsT + intervalGranularity);
            }
            if (!epsTTraversed.contains(epsT - intervalGranularity)
                    && (epsT - intervalGranularity) >= roundToGranularity(epsMd, intervalGranularity)
                            + intervalGranularity) {
                epsTTraverseRange.add(epsT - intervalGranularity);
            }
            epsTTraversed.add(epsT);
            if (epsTTraverseRange.isEmpty()) {
                break;
            }
            epsT = epsTTraverseRange.iterator().next();
            epsTTraverseRange.remove(epsT);
        }
        //System.out.println(min_eps_t+" final check "+epsT);
        double[] rt = new double[3];
        rt[0] = min_eps_t;
        rt[1] = min_s_0;
        rt[2] =  m_best ;
        
        return rt;
    }

    public static boolean checkIntervalLb(double interval, double minCost, List<Double> epsList) {
        double c = 0;
        for (double eps : epsList) {
            c += Math.abs(interval - eps);
        }
        return (c <= minCost);
    }

    public static boolean checkStLb(int d, List<Double> epsList, double minCost, double lmdD, double epsT) {
        double c = d * lmdD;
        for (int i = d; i < epsList.size(); i++) {
            c += Math.abs(epsT - epsList.get(i));
        }
        return c < minCost;
    }

    

    private static double median(List<Double> eps) {
        Collections.sort(eps);
        int n = eps.size();
        if (n % 2 == 0) {
            return (eps.get(n / 2 - 1) + eps.get(n / 2)) / 2.0;
        } else {
            return eps.get(n / 2);
        }
    } 
    
}