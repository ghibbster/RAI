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
import java.util.Iterator;


public class VotingWithPrefixes implements Strategy{


    public VotingWithPrefixes(Double valueThreshold, Double votingThreshold){
        this.valueThreshold = valueThreshold;
        this.votingThreshold = votingThreshold;
    }

    @Override
    public Double rank(State rs, State bs) {
        System.out.println("Scoring couple (" + rs.getId() + ", " + bs.getId() + ")");
        // base cases
        if (rs.isLeaf() && bs.isLeaf())
            return 0.;
        // general case
        Double score = 0.;
        //System.out.println(">>>");
        Iterator<Future> blueFutures = bs.getFuturesIterator();
        while (blueFutures.hasNext()){
            Future blueFuture = blueFutures.next();
            Future redFuture = rs.getClosestFuture(blueFuture);
            Double diffs = assess(redFuture, blueFuture);
            int prefixsize = (redFuture.size() < blueFuture.size())?(redFuture.size()):(blueFuture.size());
            if (diffs > votingThreshold * prefixsize)
                // this couple is far
                score += 1.;
            //System.out.println(blueFuture + " " + redFuture + " " + redFuture.getCloseness(blueFuture));
        }
        //System.out.println("<<<");
        Iterator<Future> redFutures = rs.getFuturesIterator();
        while (redFutures.hasNext()){
            Future redFuture = redFutures.next();
            Future blueFuture = bs.getClosestFuture(redFuture);
            Double diffs = assess(blueFuture, redFuture);
            int prefixsize = (redFuture.size() < blueFuture.size())?(redFuture.size()):(blueFuture.size());
            if (diffs > votingThreshold * prefixsize)
                // this couple is far
                score += 1.;
            //System.out.println(redFuture + " " + blueFuture + " " + blueFuture.getCloseness(redFuture));
        }
        System.out.println(score);
        return score;
    }

    @Override
    public boolean assess(State rs, State bs) {
        System.out.println("CHECK: " + rank(rs, bs) + ", " + votingThreshold * (rs.getFutures() + bs.getFutures()));
        return rank(rs, bs) <= votingThreshold * (rs.getFutures() + bs.getFutures());
    }

    @Override
    public Double assess(Future f1, Future f2) {
        // It counts how meny significatly different values are in the
        // longest aligned prefixes of f1 and f2
        if (f1 == null && f2 == null)
            return 0.;
        if (f1 == null){
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
        Iterator<Double> vs2 = f2.iterator();
        Double result = 0.;
        for (Double v1 : f1){
            if (! vs2.hasNext())
                break;
            Double v2 = vs2.next();
            if (Math.abs(v1 - v2) >= valueThreshold)
                result += 1.;
        }
        return result;
    }

    // two values are far if their (absolute) difference exceeds this threshold
    private Double valueThreshold;
    // proportion of far votes we need to exceed to mark a couple as unmergeable
    private Double votingThreshold;

}
