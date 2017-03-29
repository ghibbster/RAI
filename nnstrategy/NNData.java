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
        if (tails.isEmpty() && b.tails.isEmpty())
            return 0.;
        if (tails.isEmpty() || b.tails.isEmpty())
            //return Double.POSITIVE_INFINITY;
            return 1.;
        //double unChangedB = getUnchanged(b);
        //double unchangedT = b.getUnchanged(this);
        //double unChanged = unChangedB + unchangedT;
        double unChanged = getUnchanged(b) + b.getUnchanged(this);
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
        //double pvalue = 1. - pnorm(z);
        //return pnorm(z);
        return 1 - pnorm(z);
    }

    @Override
    public boolean isCompatibleWith(NNData b){
//        if (tails.isEmpty() && b.tails.isEmpty())
//            return true;
//        if (tails.isEmpty() || b.tails.isEmpty())
//            return false;
        //return 1. - rankWith(b) > alpha;
        return rankWith(b) > alpha;
    }

    @Override
    public void dispose(){
        localDistances.clear();
        tails.clear();
    }

    // PUBLIC METHODS SPECIFIC FOR THIS CLASS

    public NNData(double alpha){
        localDistances = new HashMap<>();
        tails = new LinkedList<>();
        this.alpha = alpha;
    }

    public Double closeness(Future f1, Future f2){
        // prefix absolute distance
        if (f1 == null && f2 == null)
            return 0.;
        if (f1 == null || f2 == null)
            return Double.POSITIVE_INFINITY;
        // now bot f1 and f2 are not null
        Iterator<Double> vs2 = f2.iterator();
        Double result = 0.;
        int n = 0;
        for (Double v1 : f1){
            // rimuovi la seconda clausola di questo check, è stata messa solo per debug
            if (! vs2.hasNext() || n == 5)
            //if (! vs2.hasNext())
                break;
            Double v2 = vs2.next();
            //result += (v1 - v2) * (v1 - v2);
            result += Math.abs(v1 - v2);
            n += 1;
        }
//        if (n == 0)
//            return Double.POSITIVE_INFINITY;
//        return result / ((double) n);
        return result;
    }


    // PRIVATE STUFF

    private double getUnchanged(NNData b){
        // gets the number of tails that are still in the same sample
        double res = 0.;
        for (Future rF : tails) {
            Double lD = localDistances.get(rF);
            double contribution = 1.;
            for (Future bF : b.tails) {
                double close = closeness(rF, bF);
                if (close == 0.) {
                    // in questo caso finiamo perché la distanza non può scendere sotto lo zero
                    contribution = .5;
                    break;
                }
                if (close < lD)
                    // close è più bassa strettamente di lD, ma è diversa da zero.
                    // in questo caso close potrebbe diventare zero successivamente, e quindi contribution passare
                    // da 0 a 0.5, ergo non possiamo finirla quà
                    contribution = .0;
                else if (close == lD && contribution != 0.)
                    // questo caso è quando non abbiamo ancora trovato un close minore strettamente, ma almeno uno
                    // che è pari alla distanza locale. A questo punto non possiamo finire perché potremmo
                    // trovare successivi candidati che hanno distanze strettamente inferiori. Una volta che li trovia
                    // mo non possiamo più considerare questo branch
                    contribution = .5;
            }
            res += contribution;
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
    private List<Future> tails;
    private Double alpha;

}
