/*
 * Copyright (c) 2016, Gaetano Pellegrino
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


public class NNData implements Data<NNData> {

    // INTERFACE ALL CLIENTS SHOULD IMPLEMENT

    @Override
    public void add(String[] values){
        Future f = Future.parse(values);
        // updating local distances
        Double bestD = Double.POSITIVE_INFINITY;
        for (Future rF : localDistances.keySet()){
            Double nD = closeness(rF, f);
            if (nD < localDistances.get(rF))
                localDistances.put(rF, nD);
            if (nD < bestD)
                bestD = nD;
        }
        localDistances.put(f, bestD);
        // updating futures
        tails.add(f);
    }

    @Override
    public void updateWith(NNData b){
        // updating local distances
        for (Future newF : b.tails){
            Double bestD = Double.POSITIVE_INFINITY;
            for (Future localF : localDistances.keySet()){
                Double localD = localDistances.get(localF);
                Double newD = closeness(localF, newF);
                if (newD < localD)
                    localDistances.put(localF, newD);
                if (newD < bestD)
                    bestD = newD;
            }
            localDistances.put(newF, bestD);
        }
        // updating futures
        tails.addAll(b.tails);
    }

    @Override
    public Double rankWith(NNData b){
        if (tails.isEmpty() || b.tails.isEmpty())
            return 0.;
        int unChanged = getUnchanged(b) + b.getUnchanged(this);
        int nr = tails.size();
        int nb = b.tails.size();
        int n = nr + nb;
        double score = unChanged / (double) (n);
        //System.out.println("Score: " + score + ", n: " + n + ", changes: " + unChanged);
        double lamr = nr / (double) n;
        double lamb = nb / (double) n;
        // double mu = (lamr * (nr - 1) + lamb * (nb - 1)) / n;
        double mu = lamr * lamr + lamb * lamb;
        double sigma = lamr * lamb + 4 * lamr * lamr * lamb * lamb;
        double z = Math.sqrt(n) * (score - mu) / Math.sqrt(sigma);
        // return pnorm(z) and not 1 - pnorm(z) because we know that RAI has a minimization function
        // by minimizing pnorm(z) we maximize 1 - pnorm(z)
        return pnorm(z);
    }

    @Override
    public boolean isCompatibleWith(NNData b){
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

    public NNData(double alpha){
        localDistances = new HashMap<>();
        tails = new HashSet<>();
        this.alpha = alpha;
    }

    public Double closeness(Future f1, Future f2){
        // prefix absolute distance
        if (f1 == null || f2 == null)
            return 0.;
//        if (f1 == null) {
//            String[] values = new String[f2.size()];
//            for (int i = 0; i < f2.size(); i ++)
//                values[i] = "0.0";
//            f1 = Future.parse(values);
//        }
//        if (f2 == null) {
//            String[] values = new String[f1.size()];
//            for (int i = 0; i < f1.size(); i ++)
//                values[i] = "0.0";
//            f2 = Future.parse(values);
//        }
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
//        if (n == 0)
//            return Double.POSITIVE_INFINITY;
        return result / ((double) n);
    }


    // PRIVATE STUFF

    private int getUnchanged(NNData b){
        // gets the number of tails that are still in the same sample
        int res = 0;
        for (Future rF : localDistances.keySet()){
            Double lD = localDistances.get(rF);
            boolean changed= false;
            for (Future bF : b.tails)
                if (closeness(rF, bF) < lD){
                    changed = true;
                    break;
                }
            if (! changed)
                res += 1;
        }
        return res;
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



    private Map<Future, Double> localDistances;
    private Set<Future> tails;
    private Double alpha;

}
