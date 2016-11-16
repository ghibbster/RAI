/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;// WHITE STATE


import RAI.transition_clustering.ClusteredTransition;
import RAI.transition_clustering.Transition;
import RAI.transition_clustering.TransitionMerge;
import RAI.transition_clustering.UnclusteredTransition;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.*;
import static java.lang.Math.abs;


public class State{


    public State(){
        this(idGenerator ++ );
    }

    public State(int id){
        this.id = id;
        outgoing = TreeMultiset.create();
        ingoing = TreeMultiset.create();
        mu = null;
        futures = new HashSet<>();
        color = Color.WHITE;
        pairs = new HashSet<>();
    }

    public int getId() {
        return id;
    }

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

    public Multiset<Transition> getClosestOutgoing(Transition t) {
        Transition bestLeft = null;
        Transition bestRight = null;
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : outgoing){
            // NON OVERLAP CASES and stop condition of the loop
            if (o.getRightGuard() > t.getRightGuard() && o.getLeftGuard() > t.getRightGuard()) {
                bestRight = o;
                break;
            }
            if (o.getRightGuard() < t.getLeftGuard() && o.getLeftGuard() < t.getLeftGuard())
                bestLeft = o;
            // OVERLAP CASES: here we always take those candidates as the best ones
            // totally included
            if (t.isOverlappedBy(o))
                overlapping.add(o);
//            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
//                overlapping.add(o);
//            // overlap on the left
//            if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
//                overlapping.add(o);
//            //overlap on the right
//            if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
//                overlapping.add(o);
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
//            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
//                overlapping.add(o);
//            // overlap on the left
//            if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
//                overlapping.add(o);
//            //overlap on the right
//            if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
//                overlapping.add(o);
            if (t.isOverlappedBy(o))
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

    public Multiset<Transition> getOutgoing(Transition t) {
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : outgoing){
            // OVERLAP CASES: here we always take those candidates as the best ones
            if (t.isOverlappedBy(o))
                overlapping.add(o);
//             totally included
//            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
//                overlapping.add(o);
//            // overlap on the left
//            else if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
//                overlapping.add(o);
//            //overlap on the right
//            else if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
//                overlapping.add(o);
        }
        return overlapping;
    }

    public Multiset<Transition> getIngoing(Transition t) {
        Multiset<Transition> overlapping = TreeMultiset.create();
        for (Transition o : ingoing){
            // OVERLAP CASES: here we always take those candidates as the best ones
            if (t.isOverlappedBy(o))
                overlapping.add(o);
//            // totally included
//            if (o.getLeftGuard() <= t.getLeftGuard() && t.getRightGuard() <= o.getRightGuard())
//                overlapping.add(o);
//            // overlap on the left
//            else if (o.getRightGuard() >= t.getLeftGuard() && o.getLeftGuard() <= t.getLeftGuard())
//                overlapping.add(o);
//            //overlap on the right
//            else if (o.getLeftGuard() <= t.getRightGuard() && o.getRightGuard() >= t.getRightGuard())
//                overlapping.add(o);
        }
        return overlapping;
    }

    public Transition getOutgoing(double value) {
        for (Transition t : outgoing){
            if (value >= t.getLeftGuard() && value < t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    public Transition getIngoing(double value) {
        for (Transition t : ingoing){
            if (value >= t.getLeftGuard() && value < t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    public void addOutgoing(Transition t) {
        if (! equals(t.getSource()))
            throw new IllegalArgumentException("Source state should be this");
        if (! outgoing.contains(t)){
            // fix per correggere multitransizioni che partono da uno stesso stato,
            // finiscono in uno stesso stato, ed hanno transizioni consecurive.
            boolean added = false;
            Multiset<Transition> closeset = getClosestOutgoing(t);
            for (Transition close : closeset){
                State tdest = t.getDestination();
                State cdest = close.getDestination();
                if (tdest.equals(cdest) && close.isAdiacenTo(t)){
//                        (t.getLeftGuard() == close.getRightGuard() || t.getRightGuard() == close.getLeftGuard())){
                    close.addAll(t);
                    if (close.getLeftGuard() >= t.getLeftGuard())
                        close.setLeftGuard(t.getLeftGuard());
                    if (close.getRightGuard() <= t.getRightGuard())
                        close.setRightGuard(t.getRightGuard());
                    added = true;
                    break;
                }
            }
            if (! added) {
                outgoing.add(t);
                t.getDestination().addIngoing(t);
            }
        }
    }

    public void addIngoing(Transition t) {
        if (! equals(t.getDestination()))
            throw new IllegalArgumentException("Destination state should be this");
        if (! ingoing.contains(t)) {
            // fix per correggere multitransizioni che partono da uno stesso stato,
            // finiscono in uno stesso stato, ed hanno transizioni consecurive.
            boolean added = false;
            Multiset<Transition> closeset = getClosestIngoing(t);
            for (Transition close : closeset){
                State tsource = t.getSource();
                State csource = close.getSource();
                if (tsource.equals(csource) && close.isAdiacenTo(t)){
//                        (t.getLeftGuard() == close.getRightGuard() || t.getRightGuard() == close.getLeftGuard())){
                    close.addAll(t);
                    if (close.getLeftGuard() >= t.getLeftGuard())
                        close.setLeftGuard(t.getLeftGuard());
                    if (close.getRightGuard() <= t.getRightGuard())
                        close.setRightGuard(t.getRightGuard());
                    added = true;
                    break;
                }
            }
            if (! added) {
                ingoing.add(t);
                t.getSource().addOutgoing(t);
            }
        }
    }

    public void removeOutgoing(Transition t) {
        if (outgoing.contains(t)) {
            outgoing.remove(t);
        }
    }

    public void removeIngoing(Transition t) {
        if (ingoing.contains(t)) {
            ingoing.remove(t);
            t.getSource().removeOutgoing(t);
        }
    }

    public String toString(){
        String res = "<" + color + " " + id + " [";
        for (Transition t : outgoing)
            res += " " + t.getDestination().getId();
        return res + " ]>";
    }

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

    public Iterator<Transition> getIngoingIterator(){
        return ingoing.iterator();
    }

    public Iterator<Transition> getOutgoingIterator(){
        return outgoing.iterator();
    }

    public void promote(){
        System.out.println("going to promote " + this);
        if (color == Color.WHITE)
            color = Color.BLUE;
        else if (color == Color.BLUE){
            color = Color.RED;
            cluster();
        }
    }

    public boolean isLeaf(){
        return outgoing.size() == 0;
    }

    public String toDot(){
        //String rep = id + " [shape=circle, label=\"" + id + "\\n" + String.format(Locale.ENGLISH, "%.2f", getMu()) + "\"];";
        String rep = id + " [shape=circle, label=\"" + id + "\"];";
        for (Transition t : outgoing)
            if (t.getDestination() != null) {
                String rightBra = (t.getRightGuard() == Double.POSITIVE_INFINITY)?("["):("]");
                rep += "\n\t" + id + " -> " + t.getDestination().getId() +
                        " [label=\"]" + String.format(Locale.ENGLISH, "%.2f", t.getLeftGuard()) +
                        ", " + String.format(Locale.ENGLISH, "%.2f", t.getRightGuard()) + rightBra +
                        " " + String.format(Locale.ENGLISH, "%.2f", t.getMu()) + "\"];";
            }
        return rep;
    }

    public void dispose(){
        outgoing.clear();
        ingoing.clear();
        futures.clear();
        pairs.clear();
    }

    // FUTURES STUFF (new similarity criterium)

    public void addFuture(Future f){
        futures.add(f);
    }

    public void removeFuture(Future f){
        futures.remove(f);
    }

    public Iterator<Future> getFuturesIterator(){
        return futures.iterator();
    }

    public int getFutures(){
        return futures.size();
    }

//    public Future getClosestFuture(Future f){
//        Future r = new Future();
//        State s = this;
//        for (Double value : f){
//            if (s == null)
//                break;
//            Transition t = s.getClosestOutgoing(value);
//            if (t == null)
//                break;
//            r.add(t.getMu());
//            s = t.getDestination();
//        }
//        return r;
//    }

    public Future getClosestFuture(Future f){
        // nuova versione che non genera il futuro più vicino,
        // bensi cerca quello più vicino tra i vari candidati
        // PRO: piu robusto
        // CONTRO: forse più costoso (O(|futurset(this)|) invece di O(|f|)
        Future closest = null;
        Double closestScore = Double.POSITIVE_INFINITY;
        for (Future candidate : futures){
            Double score = f.getCloseness(candidate);
            if (score < closestScore || closest == null){
                closestScore = score;
                closest = candidate;
            }
        }
        return closest;
    }

    // END OF FUTURE STUFF

    // COLOR CHECKS

    public boolean isWhite(){
        return color == Color.WHITE;
    }

    public boolean isBlue(){
        return color == Color.BLUE;
    }

    public boolean isRed(){
        return color == Color.RED;
    }

    // END OF COLORS STUFF

    // BLUE STATE SPECIFIC STUFF

    public boolean hasMerges(){
        return color == Color.BLUE && ! pairs.isEmpty();
    }

    public void addMerge(CandidateMerge pair){
        if (color == Color.BLUE)
            pairs.add(pair);
    }

    public void removeMerge(CandidateMerge pair){
        if (color == Color.BLUE)
            pairs.remove(pair);
    }

    public Iterator<CandidateMerge> getMergesIterator(){
        return pairs.iterator();
    }

    // END OF BLUE STATE SPECIFIC STUFF

    // RED STATE SPECIFIC STUFF

    public void cluster(){
        if (color == Color.RED) {
            PriorityQueue<TransitionMerge> q = new PriorityQueue<>();
            // INIZIALIZATION
            inizializeClustering(q);
            // CLUSTERING
            performClustering(q);
            // EXPANDING TRANSITIONS
            expandTransitions();
        }
    }

    private void inizializeClustering(PriorityQueue<TransitionMerge> q){
        Iterator<Transition> fanout = outgoing.iterator();
        ClusteredTransition previous = null;
        while (fanout.hasNext()) {
            ClusteredTransition current = new ClusteredTransition(fanout.next());
            if (previous == null)
                previous = current;
            else if (previous.getRightGuard() == current.getLeftGuard()) {
                // quando ci sono transizioni singleton con guardie in comune,
                // si ha un nondeterminismo, quindi a prescindere dallo score
                // queste vanno unite.
                addToCluster(previous, current);
            } else {
                TransitionMerge m = new TransitionMerge(previous, current);
                previous.setNextMerge(m);
                current.setPreviousMerge(m);
                q.add(m);
                previous = current;
            }
        }
    }

    private void performClustering(PriorityQueue<TransitionMerge> q){
        System.out.println("Clustering transitions of " + this);
        while (q.size() >= MIN_TRANSITIONS){
            TransitionMerge m = q.poll();
            ClusteredTransition f = m.getFirst();
            ClusteredTransition s = m.getSecond();
            if (addToCluster(f, s)) {
                TransitionMerge tm = s.getNextMerge();
                if (tm != null) {
                    q.remove(tm);
                    tm.setFirst(f);
                    tm.updateScore();
                    q.add(tm);
                    f.setNextMerge(tm);
                }
            }
        }
    }

    private void expandTransitions(){
        Iterator<Transition> fanout = outgoing.iterator();
        Transition prev = null;
        while (fanout.hasNext()){
            Transition t = fanout.next();
            if (prev == null){
                t.setLeftGuard(Double.NEGATIVE_INFINITY);
                prev = t;
            } else {
                double l = t.getLeftGuard();
                double r = prev.getRightGuard();
                double d = (l - r) / 2.;
                t.setLeftGuard(l - d);
                prev.setRightGuard(r + d);
                prev = t;
            }
        }
        if (prev != null)
            prev.setRightGuard(Double.POSITIVE_INFINITY);
    }

    public static boolean addToCluster(Transition cluster, Transition t){
        // nota: questo metodo viene chiamato quando un blu satte viene promosso a red,
        // ergo tutte le sue transizioni uscenti non sono loop !! (è importante perché siamo certi, in questo caso,
        // che la destinazione di qualsiasi transizione uscente dal novello red state sarà non red (e quindi potremo
        // aggiungere transizioni come se non ci fosse un domani).
        if (cluster == t)
            return false;
        State newDest = cluster.getDestination();
        State oldDest = t.getDestination();
        // let's update futures
        Iterator<Future> fi = oldDest.getFuturesIterator();
        while (fi.hasNext()) {
            Future f = fi.next();
            fi.remove();
            newDest.addFuture(f);
        }
        //let's update cluster
        oldDest.removeIngoing(t);
        cluster.addAll(t);
        // let's update outgoing transitions (and all paths)
        Iterator<Transition> oi = oldDest.getOutgoingIterator();
        while (oi.hasNext()) {
            Transition out = oi.next();
            out.setSource(newDest);
            newDest.addOutgoing(out);
        }
        // now we can dispose the old-destination state
        oldDest.dispose();
        return true;
    }

    // END OF RED STATE SPECIFIC STUFF


    private static int idGenerator = 0;
    private int id;
    private Double mu;
    private TreeMultiset<Transition> outgoing;
    private TreeMultiset<Transition> ingoing;
    private Set<Future> futures;
    private Color color;
    private Collection<CandidateMerge> pairs;
    public static final int MIN_TRANSITIONS = 4;

    //UNIT TEST
    public static void main(String[] args){
        State s1 = new State();
        State s2 = new State();

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
