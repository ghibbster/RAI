/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */


// questo stato rappresenta uno stato dell'algoritho di merging.
// ci dovrà in futuro essere anche una classe State della quale questa è una decorazione.

package RAI;


import RAI.transition_clustering.ClusteredTransition;
import RAI.transition_clustering.Transition;
import RAI.transition_clustering.TransitionMerge;
import RAI.transition_clustering.UnclusteredTransition;
import com.google.common.collect.TreeMultiset;
import java.util.*;


public class State{


    public State(Hypothesis h){
        this(h, idGenerator ++);
    }

    public State(Hypothesis h, int id){
        this.id = id;
        hypothesis = h;
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

    public Transition getOutgoing(double value) {
        for (Transition t : outgoing){
            // special case: singleton transitions
            if (t.getLeftGuard() == t.getRightGuard() && value == t.getLeftGuard())
                return t;
            if (value > t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    public Transition getIngoing(double value){
        for (Transition t : ingoing){
            // special case: singleton transitions
            if (t.getLeftGuard() == t.getRightGuard() && value == t.getLeftGuard())
                return t;
            // general case
            if (value > t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (value < t.getLeftGuard())
                return null;
            // value < LG --> keep iterating
        }
        return null;
    }

    public void addOutgoing(Transition t){
        t.setSource(this);
        if (! outgoing.contains(t)) {
            outgoing.add(t);
            t.getDestination().addIngoing(t);
        }
    }

    public void addIngoing(Transition t){
        t.setDestination(this);
        if (! ingoing.contains(t)) {
            ingoing.add(t);
            t.getSource().addOutgoing(t);
        }
    }

    public void removeOutgoing(Transition t) {
        if (outgoing.contains(t)) {
            outgoing.remove(t);
            t.getDestination().removeIngoing(t);
        }
    }

    public void removeIngoing(Transition t){
        if (ingoing.contains(t)){
            ingoing.remove(t);
            t.getSource().removeOutgoing(t);
        }
    }

    public void mergeWith(State s){
        // It merges s to this
        //--------------------------
        System.out.println("going to merge " + this + " with " + s);
        // updating futures
        Iterator<Future> fIterator = s.getFuturesIterator();
        while (fIterator.hasNext()) {
            Future f = fIterator.next();
            fIterator.remove();
            addFuture(f);
        }
        // updating ingoing transitions
        Iterator<Transition> inIterator = s.getIngoingIterator();
        while (inIterator.hasNext()) {
            Transition t = inIterator.next();
            s.removeIngoing(t);
            addIngoing(t);
        }
        // updating outgoing transitions
        Iterator<Transition> outIterator = s.getOutgoingIterator();
        while (outIterator.hasNext()){
            Transition t = outIterator.next();
            s.removeOutgoing(t);
            fold(t);
        }
        // fixing incongruencies and nondeterminisms
        //sanitize();
        // promotions only if this is RED (during clustering it never happens)
        if (isRed()) {
            for (Transition t : outgoing) {
                State son = t.getDestination();
                if (son.isWhite())
                    son.promote();
            }
        }
        // disposing s
        s.dispose();
    }

    private void fold(Transition t) {
        System.out.println("Folding " + t + " in " + this);
        // CASE 1: non red state
        State dest = t.getDestination();
        if (! isRed()) {
            addOutgoing(t);
            // updating futures
            Double firstValue = t.getMu();
            Iterator<Future> fit = dest.getFuturesIterator();
            while (fit.hasNext()) {
                Future f = fit.next();
                try {
                    Future fatherFuture = (Future) f.clone();
                    f.addFirst(firstValue);
                    addFuture(fatherFuture);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        } else if (isLeaf()) {
            // CASE 2: red leaf
            addOutgoing(t);
            // updating guards (this become a sink state)
            t.setLeftGuard(Double.NEGATIVE_INFINITY);
            t.setRightGuard(Double.POSITIVE_INFINITY);
            // updating futures
            Double firstValue = t.getMu();
            Iterator<Future> fit = dest.getFuturesIterator();
            while (fit.hasNext()) {
                Future f = fit.next();
                try {
                    Future fatherFuture = (Future) f.clone();
                    f.addFirst(firstValue);
                    addFuture(fatherFuture);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // CASE 3: red non leaf
            // find the overlapping transition.
            // Please note: t is a singleton transition, hence it represents just one value (mu)
            Transition overlapped = getOutgoing(t.getMu());
            overlapped.addAll(t);
            Double firstValue = t.getMu();
            // updating futures in the father and in the new son
            Iterator<Future> fit = dest.getFuturesIterator();
            while (fit.hasNext()) {
                Future f = fit.next();
                fit.remove();
                try {
                    Future fatherFuture = (Future) f.clone();
                    f.addFirst(firstValue);
                    addFuture(fatherFuture);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
            // recursive calls to handle the subtrees rooted in t's destination
            Iterator<Transition> outIter = dest.getOutgoingIterator();
            State ovDest = overlapped.getDestination();
            while (outIter.hasNext())
                ovDest.fold(outIter.next());
            dest.dispose();
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

    public Iterator<Transition> getOutgoingIterator(){
        return outgoing.iterator();
    }

    public Iterator<Transition> getIngoingIterator(){
        return ingoing.iterator();
    }

    public State promote(){
        System.out.println("going to promote " + this);
        if (color == Color.WHITE) {
            hypothesis.notifyPromotion(this);
            color = Color.BLUE;
        } else if (color == Color.BLUE) {
            // calling the cluster for learning guards over transitions
            hypothesis.notifyPromotion(this);
            cluster();
            // updating pairs
            // NOTE: if I'm here it means that there are no merges anymore
            // where this is involved (we merge red - blue couples)
            // NOTE: since this is BLUE, all its sons are WHITE by definition
            for (Transition t : outgoing)
                t.getDestination().promote();
            color = Color.RED;
        }
        return this;
    }

    public boolean isLeaf(){
        return outgoing.isEmpty();
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
        futures.clear();
        // if this is blue, and it has been merged,
        // other possible optional merges must be discarded
        hypothesis.notifyDisposal(this);
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
        pairs.add(pair);
    }

    public void removeMerge(CandidateMerge pair){
        pairs.remove(pair);
    }

    public Iterator<CandidateMerge> getMergesIterator(){
        return pairs.iterator();
    }

    // END OF BLUE STATE SPECIFIC STUFF

    // RED STATE SPECIFIC STUFF

    public void cluster() {
        System.out.println("Clustering " + this);
        PriorityQueue<TransitionMerge> q = new PriorityQueue<>();
        // INIZIALIZATION
        inizializeClustering(q);
        // CLUSTERING
        performClustering(q);
        // EXPANDING TRANSITIONS
        expandTransitions();
    }

    private void inizializeClustering(PriorityQueue<TransitionMerge> q){
        Iterator<Transition> fanout = outgoing.iterator();
        ClusteredTransition prev = null;
        while (fanout.hasNext()) {
            ClusteredTransition current = new ClusteredTransition(fanout.next());
            if (prev == null)
                prev = current;
            else if (prev.isOverlappedBy(current) || prev.isAdiacenTo(current))
                addToCluster(prev, current);
            else {
                TransitionMerge m = new TransitionMerge(prev, current);
                prev.setNextMerge(m);
                current.setPreviousMerge(m);
                q.add(m);
                prev = current;
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
                prev.setRightGuard(l - d);
                prev = t;
            }
        }
        if (prev != null)
            prev.setRightGuard(Double.POSITIVE_INFINITY);
    }

    public static boolean addToCluster(Transition cluster, Transition t){
        // nota: questo metodo viene chiamato quando un blu state viene promosso a red,
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
    private Hypothesis hypothesis;
    public static final int MIN_TRANSITIONS = 4;

    //UNIT TEST
    public static void main(String[] args){
        Hypothesis h = new Hypothesis();
        State s1 = new State(h);
        State s2 = new State(h);

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
