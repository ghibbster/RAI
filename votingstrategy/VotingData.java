/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.votingstrategy;

import RAI.Data;
import RAI.Future;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class VotingData implements Data<VotingData> {


    @Override
    public void add(String[] values) {
        Future f = Future.parse(values);
        tails.add(f);
    }

    @Override
    public void updateWith(VotingData b) {
        tails.addAll(b.tails);
    }

    @Override
    public Double rankWith(VotingData b) {
        if (tails.isEmpty() && b.tails.isEmpty())
            return 0.;
        Double score = 0.;
        for (Future blueFuture : b.tails){
            Future redFuture = closestFuture(blueFuture);
            if (redFuture == null)
                redFuture = Future.parse(new String[]{"0.0"});
            Double diffs = closeness(redFuture, blueFuture);
            int prefixsize = (redFuture.size() < blueFuture.size())?(redFuture.size()):(blueFuture.size());
            if (diffs > votingThreshold * prefixsize)
                // this couple is far
                score += 1.;
        }
        for (Future redFuture : tails){
            Future blueFuture = b.closestFuture(redFuture);
            if (blueFuture == null)
                blueFuture = Future.parse(new String[]{"0.0"});
            Double diffs = closeness(blueFuture, redFuture);
            int prefixsize = (redFuture.size() < blueFuture.size())?(redFuture.size()):(blueFuture.size());
            if (diffs > votingThreshold * prefixsize)
                score += 1.;
        }
        return score;
    }

    @Override
    public boolean isCompatibleWith(VotingData b) {
        System.out.println(rankWith(b) + " " + votingThreshold * (tails.size() + b.tails.size()));
        return rankWith(b) <= votingThreshold * (tails.size() + b.tails.size());
    }

    @Override
    public void dispose() {
        tails.clear();
    }

    public VotingData(double valueThreshold, double votingThreshold){
        this.valueThreshold = valueThreshold;
        this.votingThreshold = votingThreshold;
        tails = new HashSet<>();
    }

    public Double closeness(Future f1, Future f2){
        if (f1 == null && f2 == null)
            return 0.;
        if (f1 == null) {
            String[] values = new String[f2.size()];
            for (int i = 0; i < f2.size(); i ++)
                values[i] = "0.0";
            f1 = Future.parse(values);
        }
        if (f2 == null) {
            String[] values = new String[f1.size()];
            for (int i = 0; i < f1.size(); i ++)
                values[i] = "0.0";
            f2 = Future.parse(values);
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

    public Future closestFuture(Future f){
        Future closest = null;
        Double minDist = Double.POSITIVE_INFINITY;
        for (Future r : tails){
            Double curDist = closeness(r, f);
            if (curDist < minDist){
                closest = r;
                minDist = curDist;
            }
        }
        return closest;
    }


    private Set<Future> tails;
    private double valueThreshold;
    private double votingThreshold;

}
