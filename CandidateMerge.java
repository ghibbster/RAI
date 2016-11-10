/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;

import java.util.Iterator;


public class CandidateMerge implements Comparable<CandidateMerge>{


    public CandidateMerge(State rs, State bs){
        redState = rs;
        blueState = bs;
        computeScore(rs, bs);
    }

    // SCORING STUFF

    public double getScore(){
        return score;
    }

    private void computeScore(State rs, State bs){
        System.out.println("Scoring couple (" + rs.getId() + ", " + bs.getId() + ")");
        score = 0.;
        int n = 0;
        //System.out.println(">>>");
        Iterator<Future> blueFutures = bs.getFuturesIterator();
        while (blueFutures.hasNext()){
            Future blueFuture = blueFutures.next();
            Future redFuture = rs.getClosestFuture(blueFuture);
            score += redFuture.getCloseness(blueFuture);
            //System.out.println(blueFuture + " " + redFuture + " " + redFuture.getCloseness(blueFuture));
            n += 1;
        }
        //System.out.println("<<<");
        Iterator<Future> redFutures = rs.getFuturesIterator();
        while (redFutures.hasNext()){
            Future redFuture = redFutures.next();
            Future blueFuture = bs.getClosestFuture(redFuture);
            score += blueFuture.getCloseness(redFuture);
            //System.out.println(redFuture + " " + blueFuture + " " + blueFuture.getCloseness(redFuture));
            n += 1;
        }
        if (n == 0)
            score = Double.POSITIVE_INFINITY;
        else {
            System.out.println(score + " " + n + " " + (score / (double) n));
            score /= (double) n;
        }
    }

    public boolean isCompatible(double alpha) {
        return (redState.isLeaf() && blueState.isLeaf()) || score < alpha;
    }

    //UTILITY

    @Override
    public int compareTo(CandidateMerge o){
        if (score != o.getScore())
            return (score > o.getScore())?(1):(-1);
        return 0;
    }

    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof CandidateMerge))
            return false;
        CandidateMerge s = (CandidateMerge) o;
        return redState.equals(s.redState) && blueState.equals(s.blueState);
    }

    @Override
    public int hashCode(){
        return redState.hashCode() + blueState.hashCode();
    }

    @Override
    public String toString(){
        return "{" + redState.getId() + ":" + blueState.getId() + ":" + score + "}";
    }

    public State getRedState() {
        return redState;
    }

    public State getBlueState() {
        return blueState;
    }


    private State redState;
    private State blueState;
    private double score;


}
