/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.strategies;


import RAI.CandidateMerge;
import RAI.Future;
import RAI.State;
import RAI.Strategy;
import java.util.Iterator;


public class AvgPrefixEuclidean implements Strategy{


    public AvgPrefixEuclidean(Double threshold){
        this.threshold = threshold;
    }


    @Override
    public Double rank(CandidateMerge m) {
        State rs = m.getRedState();
        State bs = m.getBlueState();
        System.out.println("Scoring couple (" + rs.getId() + ", " + bs.getId() + ")");
        // base cases
        if (rs.isLeaf() || bs.isLeaf())
            return 0.;
        // general case
        Double score = 0.;
        int n = 0;
        //System.out.println(">>>");
        Iterator<Future> blueFutures = bs.getFuturesIterator();
        while (blueFutures.hasNext()){
            Future blueFuture = blueFutures.next();
            Future redFuture = rs.getClosestFuture(blueFuture);
            score += assess(redFuture, blueFuture);
            //System.out.println(blueFuture + " " + redFuture + " " + redFuture.getCloseness(blueFuture));
            n += 1;
        }
        //System.out.println("<<<");
        Iterator<Future> redFutures = rs.getFuturesIterator();
        while (redFutures.hasNext()){
            Future redFuture = redFutures.next();
            Future blueFuture = bs.getClosestFuture(redFuture);
            score += assess(blueFuture, redFuture);
            //System.out.println(redFuture + " " + blueFuture + " " + blueFuture.getCloseness(redFuture));
            n += 1;
        }
        if (n == 0)
            return Double.POSITIVE_INFINITY;
        System.out.println(score + " " + n + " " + (score / (double) n));
        return score / (double) n;
    }

    @Override
    public boolean assess(CandidateMerge m) {
        return m.getScore() < threshold;
    }

    @Override
    public Double assess(Future f1, Future f2) {
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
            result += (v1 - v2) * (v1 - v2);
            n += 1;
        }
        if (n == 0)
            return Double.POSITIVE_INFINITY;
        return Math.sqrt(result) / ((double) n);
    }


    private Double threshold;


}
