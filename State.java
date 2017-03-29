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


import RAI.transition_clustering.Transition;
import RAI.transition_clustering.TransitionMerge;
import com.google.common.collect.TreeMultiset;
import java.util.*;


public class State <T extends Data<T>>{


    public State(Hypothesis<T> h, T d){
        this(h, d, idGenerator ++);
    }

    public State(Hypothesis<T> h, T d, int id){
        this.id = id;
        hypothesis = h;
        outgoing = TreeMultiset.create();
        ingoing = TreeMultiset.create();
        mu = null;
        data = d;
        color = Color.WHITE;
        pairs = new HashSet<>();
        root = false;
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
            for (Transition<T> t : outgoing)
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

    public void setRoot(){
        root = true;
    }

    public Transition<T> getOutgoing(double value) {
        for (Transition<T> t : outgoing){
            // special case: singleton transitions with matching value
            if (t.getLeftGuard() == t.getRightGuard() && value == t.getMu())
                return t;
            // special case: singleton transitions with mismatching value
            if (t.getLeftGuard() == t.getRightGuard() && value < t.getMu())
                return null;
            if (t.getLeftGuard() != t.getRightGuard() && value > t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (t.getLeftGuard() != t.getRightGuard() && value < t.getLeftGuard())
                return null;
            // value > LG --> keep iterating
        }
        return null;
    }

    public Transition<T> getIngoing(double value){
        for (Transition<T> t : ingoing){
            // special case: singleton transitions with matching value
            if (t.getLeftGuard() == t.getRightGuard() && value == t.getMu())
                return t;
            // special case: singleton transitions with mismatching value
            if (t.getLeftGuard() == t.getRightGuard() && value < t.getMu())
                return null;
            if (t.getLeftGuard() != t.getRightGuard() && value > t.getLeftGuard() && value <= t.getRightGuard())
                return t;
            // value < LG || value > RG
            if (t.getLeftGuard() != t.getRightGuard() && value < t.getLeftGuard())
                return null;
            // value > LG --> keep iterating
        }
        return null;
    }

//    public void addOutgoing(Transition<T> t){
//        t.setSource(this);
//        if (! outgoing.contains(t)) {
//            outgoing.add(t);
//            t.getDestination().addIngoing(t);
//            if (isRed())
//                fixAdiacents();
////            boolean updated = false;
////            for (Transition<T> c : outgoing)
////                if (c.isAdiacenTo(t)) {
////                    c.addAll(t);
////                    if (c.getLeftGuard() >= t.getLeftGuard())
////                        c.setLeftGuard(t.getLeftGuard());
////                    if (c.getRightGuard() <= t.getRightGuard())
////                        c.setRightGuard(t.getRightGuard());
////                    updated = true;
////                    break;
////                }
////            if (! updated){
////                outgoing.add(t);
////                t.getDestination().addIngoing(t);
////            }
//        }
//    }


    public void addOutgoing(Transition<T> t){
        t.setSource(this);
        if (! outgoing.contains(t)) {
            outgoing.add(t);
            t.getDestination().addIngoing(t);
            if (isRed())
                fixAdiacents();
        }
    }


    public void addIngoing(Transition<T> t){
        t.setDestination(this);
        if (! ingoing.contains(t)) {
            ingoing.add(t);
            t.getSource().addOutgoing(t);
//            boolean updated = false;
//            for (Transition<T> c : ingoing)
//                if (c.isAdiacenTo(t)){
//                    c.addAll(t);
//                    if (c.getLeftGuard() >= t.getLeftGuard())
//                        c.setLeftGuard(t.getLeftGuard());
//                    if (c.getRightGuard() <= t.getRightGuard())
//                        c.setRightGuard(t.getRightGuard());
//                    updated = true;
//                    break;
//                }
//            if (! updated) {
//                ingoing.add(t);
//                t.getSource().addOutgoing(t);
//            }
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

    public void mergeWith(State<T> s){
        // It merges s (blu) to this (red)
        //--------------------------
        System.out.println("going to merge " + this + " with " + s);
        // updating futures
        data.updateWith(s.getData());
        s.getData().dispose();
        // updating ingoing transitions
        Iterator<Transition<T>> inIterator = s.getIngoingIterator();
        while (inIterator.hasNext()) {
            Transition<T> t = inIterator.next();
            s.removeIngoing(t);
            addIngoing(t);
        }
        // updating outgoing transitions
        // note: it is important that firs it updates outgoing transitions and then the ingoing ones
        // if we change the order we could have adiacent transitions in a still correct result.
        Iterator<Transition<T>> outIterator = s.getOutgoingIterator();
        while (outIterator.hasNext()){
            // Transition<T> t = outIterator.next();
            //s.removeOutgoing(t);
            // fold(t);
            fold(outIterator.next());
        }
        // eventual promotions
        if (isRed()) {
            for (Transition<T> t : outgoing) {
                State son = t.getDestination();
                if (son.isWhite())
                    son.promote();
            }
        }
        // disposing s
        s.dispose();
    }

    private void fixAdiacents(){
        Transition<T> prev = null;
        for (Transition<T> t : outgoing){
            if (prev == null)
                prev = t;
            else if (prev.isAdiacenTo(t)) {
                prev.addAll(t);
                if (prev.getLeftGuard() >= t.getLeftGuard())
                    prev.setLeftGuard(t.getLeftGuard());
                if (prev.getRightGuard() <= t.getRightGuard())
                    prev.setRightGuard(t.getRightGuard());
                removeOutgoing(t);
            }else
                prev = t;
        }
    }

//    private void fixAndExpandOutgoings(){
//        Transition<T> prev = null;
//        for (Transition<T> t : outgoing){
//            if (prev == null) {
//                t.setLeftGuard(Double.NEGATIVE_INFINITY);
//                prev = t;
//            }else if (prev.isAdiacenTo(t)) {
//                prev.addAll(t);
//                if (prev.getLeftGuard() >= t.getLeftGuard())
//                    prev.setLeftGuard(t.getLeftGuard());
//                if (prev.getRightGuard() <= t.getRightGuard())
//                    prev.setRightGuard(t.getRightGuard());
//                removeOutgoing(t);
//            }else{
//                double l = t.getLeftGuard();
//                double r = prev.getRightGuard();
//                double d = (l - r) / 2.;
//                t.setLeftGuard(l - d);
//                prev.setRightGuard(l - d);
//                prev = t;
//            }
//        }
//        if (prev != null)
//            prev.setRightGuard(Double.POSITIVE_INFINITY);
//    }

//    private void fold(Transition<T> t) {
//        //System.out.println("Folding " + t + " in " + this);
//        // CASE 1: non red state
//        State<T> dest = t.getDestination();
//        if (! isRed()) {
//            addOutgoing(t);
//        } else if (isLeaf()) {
//            // CASE 2: red leaf
//            addOutgoing(t);
//            // updating guards (this become a sink state)
//            t.setLeftGuard(Double.NEGATIVE_INFINITY);
//            t.setRightGuard(Double.POSITIVE_INFINITY);
//        } else {
//            // CASE 3: red non leaf
//            // find the overlapping transition.
//            // Please note: t is a singleton transition, hence it represents just one value (mu)
//            Transition<T> overlapped = getOutgoing(t.getMu());
//            overlapped.addAll(t);
//            // updating futures in the new son
//            T destData = dest.getData();
//            overlapped.getDestination().getData().updateWith(destData);
//            destData.dispose();
//            // fixing adiacent transitions
//            fixAndExpandOutgoings();
//            // recursive calls to handle the subtrees rooted in t's destination
//            Iterator<Transition<T>> outIter = dest.getOutgoingIterator();
//            State<T> ovDest = overlapped.getDestination();
//            while (outIter.hasNext())
//                ovDest.fold(outIter.next());
//            dest.dispose();
//        }
//    }

        private void fold(Transition<T> t) {
        //System.out.println("Folding " + t + " in " + this);
        // CASE 1: non red state
        State<T> source = t.getSource();
        State<T> dest = t.getDestination();
        if (equals(source))
            return;
        if (! isRed()) {
            source.removeOutgoing(t);
            addOutgoing(t);
        } else if (isLeaf()) {
            // CASE 2: red leaf
            source.removeOutgoing(t);
            addOutgoing(t);
            // updating guards (this become a sink state)
            t.setLeftGuard(Double.NEGATIVE_INFINITY);
            t.setRightGuard(Double.POSITIVE_INFINITY);
        } else {
            // CASE 3: red non leaf
            // find the overlapping transition.
            // Please note: t is a singleton transition, hence it represents just one value (mu)
            source.removeOutgoing(t);
            Transition<T> overlapped = getOutgoing(t.getMu());
            overlapped.addAll(t);
            // updating futures in the new son
            T destData = dest.getData();
            overlapped.getDestination().getData().updateWith(destData);
            destData.dispose();
            // recursive calls to handle the subtrees rooted in t's destination
            Iterator<Transition<T>> outIter = dest.getOutgoingIterator();
            State<T> ovDest = overlapped.getDestination();
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

    public T getData(){
        return data;
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

    public Iterator<Transition<T>> getOutgoingIterator(){
        return outgoing.iterator();
    }

    public Iterator<Transition<T>> getIngoingIterator(){
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
        // we relabel the start state to 0 by convention
        // NOTA BENE:
        // siccome l'assegnazione per id avviene tramite variabile statica (idGenerator),
        // quando learniamo più modelli con lo stesso run possiamo violare questa convenzione
        int lId = (isRoot() && id != 0)?(0):(id);
        String rep = lId + " [shape=circle, label=\"" + lId + "\"];";
        for (Transition t : outgoing)
            if (t.getDestination() != null) {
                String rightBra = (t.getRightGuard() == Double.POSITIVE_INFINITY)?("["):("]");
                // we relabel the start state to 0 by convention
                int lDestId = (t.getDestination().isRoot() && t.getDestination().getId() != 0)?(0):(t.getDestination().getId());
                // LOCCHIO ALLA PRECISIONE !!!
                rep += "\n\t" + lId + " -> " + lDestId +
                        " [label=\"]" + String.format(Locale.ENGLISH, "%.10f", t.getLeftGuard()) +
                        ", " + String.format(Locale.ENGLISH, "%.10f", t.getRightGuard()) + rightBra +
                        " " + String.format(Locale.ENGLISH, "%.10f", t.getMu()) + "\"];";
            }
        return rep;
    }

    public void dispose(){
        outgoing.clear();
        pairs.clear();
        data.dispose();
        // if this is blue, and it has been merged,
        // other possible optional merges must be discarded
        hypothesis.notifyDisposal(this);
    }

    // COLOR AND ROOT CHECKS

    public boolean isWhite(){
        return color == Color.WHITE;
    }

    public boolean isBlue(){
        return color == Color.BLUE;
    }

    public boolean isRed(){
        return color == Color.RED;
    }

    public boolean isRoot() { return root; }

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
        PriorityQueue<TransitionMerge<T>> q = new PriorityQueue<>(Collections.reverseOrder());
        //PriorityQueue<TransitionMerge<T>> q = new PriorityQueue<>();
        // INIZIALIZATION
        inizializeClustering(q);
        // CLUSTERING
        performClustering(q);
        // EXTENDING TRANSITIONS
        expandTransitions();
    }

    private void inizializeClustering(PriorityQueue<TransitionMerge<T>> q){
        // it adds all couple of candidate joins to the queue
        Iterator<Transition<T>> fanout = outgoing.iterator();
        Transition<T> prevT = null;
        TransitionMerge<T> prevM = null;
        while (fanout.hasNext()) {
            Transition<T> currT = fanout.next();
            if (prevT == null)
                prevT = currT;
            else if (prevT.isOverlappedBy(currT) || prevT.isAdiacenTo(currT))
                addToCluster(prevT, currT);
            else if (prevM == null){
                    TransitionMerge<T> currM = new TransitionMerge<>(prevT, currT);
                    q.add(currM);
                    prevM = currM;
                    prevT = currT;
            } else {
                    TransitionMerge<T> currM = new TransitionMerge<>(prevM.getSecond(), currT);
                    currM.setPrevious(prevM);
                    prevM.setNext(currM);
                    q.add(currM);
                    prevM = currM;
                    prevT = currT;
            }
        }
    }

    private void performClustering(PriorityQueue<TransitionMerge<T>> q){
        while (q.size() >= MAX_TRANSITIONS){
            TransitionMerge<T> m = q.poll();
            Transition<T> f = m.getFirst();
            Transition<T> s = m.getSecond();
            if (addToCluster(f, s)) {
                TransitionMerge<T> p = m.getPrevious();
                TransitionMerge<T> n = m.getNext();
                if (p != null){
                    q.remove(p);
                    p.setNext(n);
                    p.setSecond(f);
                    p.updateScore();
                    q.add(p);
                }
                if (n != null){
                    q.remove(n);
                    n.setPrevious(p);
                    n.setFirst(f);
                    n.updateScore();
                    q.add(n);
                }
            }
        }
        q.clear();
    }

    public void expand(){
        expandTransitions();
        for (Transition<T> t : outgoing){
            State<T> s = t.getDestination();
            s.expand();
        }
    }

    private void expandTransitions(){
        Iterator<Transition<T>> fanout = outgoing.iterator();
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

    public boolean addToCluster(Transition<T> cluster, Transition<T> t){
        // nota: questo metodo viene chiamato quando un blu state viene promosso a red,
        // ergo tutte le sue transizioni uscenti non sono loop !! (è importante perché siamo certi, in questo caso,
        // che la destinazione di qualsiasi transizione uscente dal novello red state sarà non red (e quindi potremo
        // aggiungere transizioni come se non ci fosse un domani).
        if (cluster == t)
            return false;
        State<T> newDest = cluster.getDestination();
        State<T> oldDest = t.getDestination();
        // let's update futures
        T oldData = oldDest.getData();
        newDest.getData().updateWith(oldData);
        oldData.dispose();
        //let's update cluster
        oldDest.removeIngoing(t);
        cluster.addAll(t);
        // let's update outgoing transitions (and all paths)
        Iterator<Transition<T>> oi = oldDest.getOutgoingIterator();
        while (oi.hasNext()) {
            Transition<T> out = oi.next();
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
    private TreeMultiset<Transition<T>> outgoing;
    private TreeMultiset<Transition<T>> ingoing;
    private Color color;
    private T data;
    private Collection<CandidateMerge> pairs;
    private Hypothesis<T> hypothesis;
    private boolean root;
    public static int MAX_TRANSITIONS;

}
