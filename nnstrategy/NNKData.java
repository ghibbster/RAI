/*
 * Copyright (c) 2017, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.nnstrategy;

import RAI.Data;
import RAI.Future;
import java.util.*;


// Generalization to k of NNData.java
// based on "Multivariate Two-Sample Tests Based on Nearest Neighbors" by Mark F. Schilling.
// https://www.jstor.org/stable/2289012?seq=1#page_scan_tab_contents


public class NNKData implements Data<NNKData> {

    // INTERFACE ALL CLIENTS SHOULD IMPLEMENT

    @Override
    public void add(String[] values){
        // invoked when adding a new tail to the tailset
        Future f = Future.parse(values);
        // updating local distances
        PriorityQueue<Double> kClosest = new PriorityQueue<>(k, Collections.reverseOrder());
        for (Future rF : localDistances.keySet()){
            Double nD = closeness(rF, f);
            PriorityQueue<Double> rFNeighbors = localDistances.get(rF);
            Double farest = rFNeighbors.peek();
            if (farest == null || nD < farest){
                rFNeighbors.poll();
                rFNeighbors.add(nD);
            }
            if (kClosest.size() < k || kClosest.peek() > nD) {
                kClosest.add(nD);
                if (kClosest.size() > k)
                    kClosest.poll();
            }
        }
        localDistances.put(f, kClosest);
        // updating futures
        tails.add(f);
    }

    @Override
    public void updateWith(NNKData b){
        // updating local distances
        for (Future newF : b.tails){
            PriorityQueue<Double> kClosest = new PriorityQueue<>(k, Collections.reverseOrder());
            for (Future localF : localDistances.keySet()){
                PriorityQueue<Double> localNeighbors = localDistances.get(localF);
                Double newD = closeness(localF, newF);
                Double farest = localNeighbors.peek();
                if (farest == null || newD < farest){
                    localNeighbors.poll();
                    localNeighbors.add(newD);
                }
                if (kClosest.size() < k || kClosest.peek() > newD){
                    kClosest.add(newD);
                    if (kClosest.size() > k)
                        kClosest.poll();
                }
            }
            localDistances.put(newF, kClosest);
        }
        // updating futures
        tails.addAll(b.tails);
    }

    @Override
    public Double rankWith(NNKData b){
        if (tails.isEmpty() || b.tails.isEmpty())
            return 0.;
        int unChanged = getUnchanged(b) + b.getUnchanged(this);
        int nr = tails.size();
        int nb = b.tails.size();
        int n = nr + nb;
        double score = unChanged / (double) (n * k);
        //System.out.println("Score: " + score + ", n: " + n + ", changes: " + unChanged);
        double lamr = nr / (double) n;
        double lamb = nb / (double) n;
        // double mu = (lamr * (nr - 1) + lamb * (nb - 1)) / n;
        double mu = lamr * lamr + lamb * lamb;
        double sigma = lamr * lamb + 4 * lamr * lamr * lamb * lamb;
        double z = Math.sqrt(n * k) * (score - mu) / Math.sqrt(sigma);
        // return pnorm(z) and not 1 - pnorm(z) because we know that RAI has a minimization function
        // by minimizing pnorm(z) we maximize 1 - pnorm(z)
        return pnorm(z);
    }

    @Override
    public boolean isCompatibleWith(NNKData b){
        if (tails.isEmpty() || b.tails.isEmpty())
            return true;
        return 1. - rankWith(b) > alpha;
    }

    @Override
    public void dispose(){
        localDistances.clear();
        tails.clear();
    }

    // PUBLIC METHODS SPECIFIC FOR THIS CLASS

    public NNKData(double alpha, int k){
        // alpha is the confidence value, k is the order for a K-Nearest-Neighbor algorithm
        localDistances = new HashMap<>();
        tails = new HashSet<>();
        this.alpha = alpha;
        this.k = k;
    }

    public Double closeness(Future f1, Future f2){
        return prefixAbsolute(f1, f2);
    }

    private static Double prefixAbsolute(Future f1, Future f2){
        if (f1 == null || f2 == null)
            return 0.;
        // now bot f1 and f2 are not null
        Iterator<Double> vs2 = f2.iterator();
        Double result = 0.;
        int n = 0;
        for (Double v1 : f1){
            if (! vs2.hasNext())
                break;
            Double v2 = vs2.next();
            //result += (v1 - v2) * (v1 - v2);
            result += Math.abs(v1 - v2);
            n += 1;
        }
        //return result / ((double) n);
        return result;
    }

    private static Double dynamicTimeWarping(Future f1, Future f2){
        if (f1 == null && f2 == null)
            return 0.;
        if (f1 == null || f2 == null)
            return Double.POSITIVE_INFINITY;
        // now f1 and f2 are bot not null
        // ------------------------------------
        //initialization
        double[][] lattice = new double[f1.size() + 1][f2.size() + 1];
        for (int i = 1; i < f1.size() + 1; i ++)
            lattice[i][0] = Double.POSITIVE_INFINITY;
        for (int j = 1; j < f2.size() + 1; j ++)
            lattice[0][j] = Double.POSITIVE_INFINITY;
        lattice[0][0] = 0.;
        // cost computations
        Iterator<Double> iIter = f1.iterator();
        for (int i = 1; i < f1.size() + 1; i ++) {
            Iterator<Double> jIter = f2.iterator();
            Double iVal = (iIter.hasNext())?(iIter.next()):(Double.POSITIVE_INFINITY);
            for (int j = 1; j < f2.size() + 1; j++) {
                Double jVal = (jIter.hasNext())?(jIter.next()):(Double.POSITIVE_INFINITY);
                double cost = Math.abs(iVal - jVal);
                // matching
                double bestOption = lattice[i - 1][j];
                // deletion
                if (lattice[i][j - 1] < bestOption)
                    bestOption = lattice[i][j - 1];
                // match
                if (lattice[i - 1][j - 1] < bestOption)
                    bestOption = lattice[i - 1][j - 1];
                // updating lattice
                lattice[i][j] = cost + bestOption;
            }
        }
//        for (int k = 0; k < f1.size() + 1; k ++)
//            System.out.println(Arrays.toString(lattice[k]));
        return lattice[f1.size()][f2.size()];
    }


    // PRIVATE STUFF

    private int getUnchanged(NNKData b){
        // gets the number of tails that are still in the same sample
        // after merging futures of this with futures in b (by simulation, we don't do the actual merge).
        int res = 0;
        // updating flags
        // updated[i][j] è TRUE se è già stata trovata una distanza inferiore dopo aver simulato il merge
        boolean[][] updated = new boolean[localDistances.size()][k];
        for (int i = 0; i < localDistances.size(); i ++)
            for (int j = 0; j < k; j ++)
                updated[i][j] = false;
        // cominciamo a contare gli update
        int i = 0;
        for (Future rF : localDistances.keySet()){
            PriorityQueue<Double> rNeighbors = localDistances.get(rF);
            int j = 0;
            for (Double localD : rNeighbors){
                for (Future bF : b.tails){
                    Double newD = closeness(rF, bF);
                    if (! updated[i][j] && localD > newD){
                        updated[i][j] = true;
                        res += 1;
                    }
                }
                j += 1;
            }
            i += 1;
        }
        return (localDistances.size() * k) - res;
    }

    private static double dnorm(double x){
        // standard normal (N(0,1)) distribution function
        return 1 / Math.sqrt(2 * Math.PI) * Math.exp(- 0.5 * x * x);
    }

    private static double pnorm(double z){
        // standard normal (N(0,1)) cumulative distribution function.
        // integral from -inf to z of dnorm()
        // NOTE: it uses Taylor approximation as in Massaglia reference
        // (Evaluating the Normal Distribution)
        if (z < -8.)
            return 0.;
        if (z > 8.)
            return 1.;
        double s = 0.;
        double t = z;
        int i = 3;
        while (s + t != s){
            s += t;
            t *= z * z / i;
            i += 2;
        }
        return 0.5 + s * dnorm(z);
    }

//    public static void main(String[] args){
//        Future f1 = Future.parse(new String[]{"0.1", "0.2", "44."});
//        Future f2 = Future.parse(new String[]{"0.2", "0.3"});
//        System.out.println(dynamicTimeWarping(f1, f2));
//        System.out.println(prefixAbsolute(f1, f2));
//    }


    // neighborhood size
    private int k;
    // for each future in tails, the distances to the closest k futures (neighbor) sorted in ascending order
    private Map<Future, PriorityQueue<Double>> localDistances;
    // specific data for a given state, in this case a set of future continuations aka tails.
    private Set<Future> tails;
    // confidence level for Schilling's statistical test
    private Double alpha;

}
