/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.strategies;


import RAI.Future;
import RAI.State;
import RAI.Strategy;
import RAI.transition_clustering.ClusteredTransition;
import RAI.transition_clustering.Transition;

import java.util.HashMap;
import java.util.Iterator;
import java.lang.Math;
import java.util.Map;


public class NNStrategy implements Strategy {


    public NNStrategy(double alpha){
        this.alpha = alpha;
    }

    public Double rank(State r, State b){
        // per ogni futuro red r
        // per ogni futuro blu b
        // se b è più vicino ad r di r' allora interrompi e setta changed to true
        // fine loop interno
        // se changed is false, count += 1
        // fine loop esterno
        // return count / n
        if (r.isLeaf() || b.isLeaf())
            return 0.;
        // now we look for changes
        int changes = getChanges(r, b) + getChanges(b, r);
        //System.out.println("Score: " + score + ", n: " + (r.getFutures() + b.getFutures()) + ", changes: " + changes);
        double score = changes / (double) (r.getFutures() + b.getFutures());
        int nr = r.getFutures();
        int nb = b.getFutures();
        int n = nr + nb;
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

    private int getChanges(State r, State b){
        // note: order in parameters is fundamental here
        int i = 0;
        Map<Future, Future> locals = getLocalNeighbors(r);
        Iterator<Future> rIter = r.getFuturesIterator();
        while (rIter.hasNext()){
            Future rf = rIter.next();
//            Future bestOpponent = null;
            Double bestOpponentDist = Double.POSITIVE_INFINITY;
            Iterator<Future> bIter = b.getFuturesIterator();
            boolean changed = false;
            while (bIter.hasNext()){
                Future bf = bIter.next();
                double dg = assess(rf, bf);
                double dl = assess(rf, locals.get(rf));
                if (dg < dl) {
                    changed = true;
                    //break;
                } else if (dg < bestOpponentDist){
                    bestOpponentDist = dg;
//                    bestOpponent = bf;
                }
            }
//            System.out.println("CHANGES " + r.getId() + " " + b.getId());
//            System.out.println("RF: " + rf);
//            System.out.println("RL: " + locals.get(rf));
//            System.out.println("BF: " + bestOpponent);
//            System.out.println("d(RF, RL): " + assess(rf, locals.get(rf)));
//            System.out.println("d(RF, BF): " + bestOpponentDist);
//            System.out.println("--------------------");
            if (! changed)
                i += 1;
        }
        return i;
    }

    private Map<Future, Future> getLocalNeighbors(State s){
        // for each future in s, it finds the closest (different) future in s itself
        Map<Future, Future> localPairs = new HashMap<>();
        Iterator<Future> outer = s.getFuturesIterator();
        while (outer.hasNext()){
            Future o = outer.next();
            Future bestF = null;
            double bestD = Double.POSITIVE_INFINITY;
            Iterator<Future> inner = s.getFuturesIterator();
            while (inner.hasNext()){
                Future i = inner.next();
                double d = assess(o, i);
                if ((!o.equals(i)) && d < bestD){
                    bestF = i;
                    bestD = d;
                }
            }
            // note: it is impossible that bestF is null by construction
            localPairs.put(o, bestF);
        }
        return localPairs;
    }

    @Override
    public boolean assess(State r, State b){
        if (r.isLeaf() || b.isLeaf())
            return true;
        // now n is bigger than 0
//        double score = rank(r, b);
//        int nr = r.getFutures();
//        int nb = b.getFutures();
//        int n = nr + nb;
//        double lamr = nr / (double) n;
//        double lamb = nb / (double) n;
//        // double mu = (lamr * (nr - 1) + lamb * (nb - 1)) / n;
//        double mu = lamr * lamr + lamb * lamb;
//        double sigma = lamr * lamb + 4 * lamr * lamr * lamb * lamb;
//        double z = Math.sqrt(n) * (score - mu) / Math.sqrt(sigma);
//        System.out.println("ZSCORE " + z + ", PNORM " + pnorm(z) + ", PROP " + score + ", MU " + mu + ", SIGMA " + sigma + ", N " + n + ", NR " + nr + ", NB " + nb);
//        return (1. - pnorm(z)) > alpha;
        return 1. - rank(r, b) > alpha;
    }

    @Override
    public Double assess(Future f1, Future f2) {
        // prefix euclidean distance
        if (f1 == null && f2 == null)
            return 0.;
        if (f1 == null) {
            String[] values = new String[f2.size()];
            for (int i = 0; i < f2.size(); i ++)
                values[i] = "0.0";
            f1 = Future.parse(values, this);
        }
        if (f2 == null) {
            String[] values = new String[f1.size()];
            for (int i = 0; i < f1.size(); i ++)
                values[i] = "0.0";
            f2 = Future.parse(values, this);
        }
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
        if (n == 0)
            return Double.POSITIVE_INFINITY;
        //double res = Math.sqrt(result) / ((double) n);
        //System.out.println("DISTANCE:\n" + f1 + "\n" + f2 + "\n" + res + "\n---------------");
        //return Math.sqrt(result) / ((double) n);
        return result / ((double) n);
    }

//    private Map<Transition, Double> getLocals(Transition t){
//        Map<Transition, Double> locals = new HashMap<>();
//        for (Double v1 : t)
//            for (Double v2 : t){
//                if (! v1.equals(v2))
//
//            }
//    }
//
//    public boolean assess(Transition t1, Transition t2){
//
//    }

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

    private double alpha;

    //UNIT TEST
    public static void main(String[] args){
        System.out.println(dnorm(3.));
        System.out.println(2 *  pnorm( - Math.abs(4.472136)));
        System.out.println("---");
        System.out.println(pnorm(-1.));
        System.out.println(pnorm(1.));
        System.out.println(1. - pnorm(-1.));
    }


}
