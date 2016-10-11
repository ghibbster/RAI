/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;// WHITE STATE


import RAI.transition_clustering.Transition;
import RAI.transition_clustering.UnclusteredTransition;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.*;
import static java.lang.Math.abs;


public class WhiteState implements State{


    public WhiteState(){
        id = idGenerator ++;
        outgoing = TreeMultiset.create();
        ingoing = TreeMultiset.create();
        futures = new HashSet<>();
        mu = null;
    }

    public WhiteState(int id){
        this.id = id;
        outgoing = TreeMultiset.create();
        ingoing = TreeMultiset.create();
        mu = null;
    }

    @Override
    public int getId() {
        return id;
    }

//    @Override
//    public Double getMu(){
//        if (mu != null)
//            return mu;
//        if (futures == null)
//            return null;
//        if (outgoing.isEmpty())
//            return 0.;
//        // at training time we need to update mu over time, so we don't set the field mu.
//        double mu = 0.;
//        for (Transition t : outgoing)
//            mu += t.getMu();
//        return mu / (double) outgoing.size();
//
//        double mu = 0.;
//        int size = 0;
//        for (Transition t : outgoing)
//            for (Double v : t) {
//                mu += v;
//                size += 1;
//            }
//        return mu / (double) size;
//    }

    @Override
    public Double getMu() {
        if (mu != null)
            return mu;
        else{
            mu = 0.;
            int size = 0;
            for (Transition t : outgoing)
                for (Double v : t) {
                    mu += v;
                    size += 1;
                }
            if (size == 0)
                mu = 0.;
            else
                mu /= (double) size;
            return mu;
        }
    }

    public void setMu(double mu){
        this.mu = mu;
    }

    @Override
    public Transition getClosestOutgoing(double value) {
        Transition previousCandidate = null;
        for (Transition t : outgoing){
            //System.out.println(">> " + t.getMu() + " " + t);
            if (value >= t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard()) {
                if (previousCandidate == null)
                    return t;
                double dPrevious = abs(value - previousCandidate.getRightGuard());
                double dCurrent = abs(value - t.getLeftGuard());
                return (dPrevious <= dCurrent)?(previousCandidate):(t);
            }
            previousCandidate = t;
        }
        return previousCandidate;
    }

    @Override
    public Transition getClosestIngoing(double value) {
        Transition previousCandidate = null;
        for (Transition t : ingoing){
            if (value >= t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard()) {
                if (previousCandidate == null)
                    return t;
                double dPrevious = abs(value - previousCandidate.getRightGuard());
                double dCurrent = abs(value - t.getLeftGuard());
                return (dPrevious <= dCurrent)?(previousCandidate):(t);
            }
            previousCandidate = t;
        }
        return previousCandidate;
    }

    @Override
    public Multiset<Transition> getClosestOutgoing(Transition t) {
        Transition bestLeft = null;
        Transition bestRight = null;
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : outgoing){
            // NON OVERLAP CASES
            if (o.getRightGuard() > t.getRightGuard() && o.getLeftGuard() > t.getRightGuard()) {
                bestRight = o;
                break;
            }
            if (o.getRightGuard() < t.getLeftGuard() && o.getLeftGuard() < t.getLeftGuard())
                bestLeft = o;
            // OVERLAP CASES: here we always take those candidates as the best ones
            // totally included
            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
                overlapping.add(o);
            // overlap on the left
            if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
                overlapping.add(o);
            //overlap on the right
            if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
                overlapping.add(o);
        }
        // se overlapping ha elementi al suo interno, ritorna overlapping. Altrimenti ritorna il migliore più prossimo
        // se esiste
        if (! overlapping.isEmpty())
            return overlapping;
        else {
            Multiset<Transition> result = TreeMultiset.create();
            if (bestLeft == null && bestRight == null)
                return result;
            if (bestLeft == null) {
                result.add(bestRight);
                return result;
            }
            if (bestRight == null){
                result.add(bestLeft);
                return result;
            }
            double dLeft = abs(t.getLeftGuard() - bestLeft.getRightGuard());
            double dRight = abs(bestRight.getLeftGuard() - t.getRightGuard());
            if (dLeft <= dRight) {
                result.add(bestLeft);
                return result;
            }
            result.add(bestRight);
            return result;
        }
    }

    @Override
    public Multiset<Transition> getClosestIngoing(Transition t) {
        Transition bestLeft = null;
        Transition bestRight = null;
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : ingoing){
            // NON OVERLAP CASES
            if (o.getRightGuard() > t.getRightGuard() && o.getLeftGuard() > t.getRightGuard()) {
                bestRight = o;
                break;
            }
            if (o.getRightGuard() < t.getLeftGuard() && o.getLeftGuard() < t.getLeftGuard())
                bestLeft = o;
            // OVERLAP CASES: here we always take those candidates as the best ones
            // totally included
            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
                overlapping.add(o);
            // overlap on the left
            if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
                overlapping.add(o);
            //overlap on the right
            if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
                overlapping.add(o);
        }
        // se overlapping ha elementi al suo interno, ritorna overlapping. Altrimenti ritorna il migliore più prossimo
        // se esiste
        if (! overlapping.isEmpty())
            return overlapping;
        else {
            Multiset<Transition> result = TreeMultiset.create();
            if (bestLeft == null && bestRight == null)
                return result;
            if (bestLeft == null) {
                result.add(bestRight);
                return result;
            }
            if (bestRight == null){
                result.add(bestLeft);
                return result;
            }
            double dLeft = abs(t.getLeftGuard() - bestLeft.getRightGuard());
            double dRight = abs(bestRight.getLeftGuard() - t.getRightGuard());
            if (dLeft <= dRight) {
                result.add(bestLeft);
                return result;
            }
            result.add(bestRight);
            return result;
        }
    }

    @Override
    public Multiset<Transition> getOutgoing(Transition t) {
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : outgoing){
            // OVERLAP CASES: here we always take those candidates as the best ones
            // totally included
            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
                overlapping.add(o);
            // overlap on the left
            else if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
                overlapping.add(o);
            //overlap on the right
            else if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
                overlapping.add(o);
        }
        return overlapping;
    }

    @Override
    public Multiset<Transition> getIngoing(Transition t) {
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : ingoing){
            // OVERLAP CASES: here we always take those candidates as the best ones
            // totally included
            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
                overlapping.add(o);
            // overlap on the left
            else if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
                overlapping.add(o);
            //overlap on the right
            else if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
                overlapping.add(o);
        }
        return overlapping;
    }

    @Override
    public Transition getOutgoing(double value) {
        for (Transition t : outgoing){
            if (value >= t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    @Override
    public Transition getIngoing(double value) {
        for (Transition t : ingoing){
            if (value >= t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    @Override
    public void addOutgoing(Transition t) {
        if (! equals(t.getSource()))
            throw new IllegalArgumentException("Source state should be this");
        if (! outgoing.contains(t)){
            outgoing.add(t);
            t.getDestination().addIngoing(t);
        }
    }

    @Override
    public void addIngoing(Transition t) {
        if (! equals(t.getDestination()))
            throw new IllegalArgumentException("Destination state should be this");
        if (! ingoing.contains(t)) {
            ingoing.add(t);
            t.getSource().addOutgoing(t);
        }
    }

    @Override
    public void removeOutgoing(Transition t) {
        if (outgoing.contains(t)) {
            outgoing.remove(t);
        }
    }

    @Override
    public void removeIngoing(Transition t) {
        if (ingoing.contains(t)) {
            ingoing.remove(t);
            t.getSource().removeOutgoing(t);
        }
    }

    @Override
    public String toString(){
        String res = "<" + id + " [";
        for (Transition t : outgoing)
            res += " " + t.getDestination().getId();
        return res + " ]>";
    }

    @Override
    public int compareTo(State o) {
        return id - o.getId();
    }

    @Override
    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (! (o instanceof State))
            return false;
        State s = (State) o;
        return s.getId() == id;
    }

    @Override
    public int hashCode(){
        return id;
    }

    @Override
    public Iterator<Transition> getIngoingIterator(){
        return ingoing.iterator();
    }

    @Override
    public Iterator<Transition> getOutgoingIterator(){
        return outgoing.iterator();
    }

    public BlueState promote(){
        BlueState newShape = new BlueState(this);
        for (Transition t : ingoing)
            t.setDestination(newShape);
        for (Transition t : outgoing)
            t.setSource(newShape);
        return newShape;
    }

    @Override
    public boolean isLeaf(){
        return outgoing.size() == 0;
    }

    @Override
    public String toDot(){
        String rep = id + " [shape=circle, label=\"" + id + "\\n" + String.format(Locale.ENGLISH, "%.2f", getMu()) + "\"];";
        for (Transition t : outgoing)
            if (t.getDestination() != null) {
                String rightBra = (t.getRightGuard() == Double.POSITIVE_INFINITY)?("["):("]");
                rep += "\n\t" + id + " -> " + t.getDestination().getId() +
                        " [label=\"]" + String.format(Locale.ENGLISH, "%.2f", t.getLeftGuard()) +
                        ", " + String.format(Locale.ENGLISH, "%.2f", t.getRightGuard()) + rightBra + "\"];";
            }
        return rep;
    }

    @Override
    public void dispose(){
        outgoing.clear();
        ingoing.clear();
        futures.clear();
    }

    // FUTURES STUFF (new similarity criterium)

    @Override
    public void addFuture(Future f){
        futures.add(f);
    }

    @Override
    public void removeFuture(Future f){
        futures.remove(f);
    }

    @Override
    public Iterator<Future> getFuturesIterator(){
        return futures.iterator();
    }

    public int getFutures(){
        return futures.size();
    }

    @Override
    public Future getClosestFuture(Future f){
        Future r = new Future();
        State s = this;
        for (Double value : f){
            if (s == null)
                break;
            Transition t = s.getClosestOutgoing(value);
            if (t == null)
                break;
            r.add(t.getMu());
            s = t.getDestination();
        }
        return r;
    }

    // END OF FUTURE STUFF


    private static int idGenerator = 0;
    private int id;
    private Double mu;
    private TreeMultiset<Transition> outgoing;
    private TreeMultiset<Transition> ingoing;
    private Set<Future> futures;

    //UNIT TEST
    public static void main(String[] args){
        WhiteState s1 = new WhiteState();
        WhiteState s2 = new WhiteState();

        Transition t1 = new UnclusteredTransition(s1, s2);
        t1.add(2.);
        t1.add(3.);
        t1.add(4.);

        Transition t2 = new UnclusteredTransition(s1, s2);
        t2.add(8.);
        t2.add(9.);

        Transition t3 = new UnclusteredTransition(s2, s1);
        t3.add(17.0);
        t3.add(20.0);

        Transition t4 = new UnclusteredTransition(s2, s1);
        t4.add(4.);

        System.out.println(t3.getMu());

//        for (Double v : t1)
//            System.out.println(v);

//        System.out.println();
//        System.out.println(t1.getStd());
//        System.out.println(t2.getStd());
//        System.out.println(t3.getStd());
//
//
//        //System.out.println();
//        //System.out.println(t1.compareTo(t2));


//        System.out.println();
//        s1.addOutgoing(t1);
//        s2.addOutgoing(t3);
//        s1.addOutgoing(t2);
//        s2.addOutgoing(t4);
//
//        RAI.Future f1 = RAI.Future.parse(new String[]{"2.0", "5.2", "7.2"});
//        RAI.Future f2 = RAI.Future.parse(new String[]{"5.0", "8.8", "12.1"});
//        RAI.Future f3 = RAI.Future.parse(new String[]{"5.1", "8.7", "12.3"});
//        s1.addFuture(f1);
//        s1.addFuture(f3);
//
//        System.out.println(f1);
//        System.out.println(f2);
//        System.out.println(f3);
//        System.out.println();
//
//        System.out.println(s1);
//        System.out.println(s1.getClosestFuture(f2));
    }


}
