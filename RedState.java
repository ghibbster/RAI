package RAI;

import RAI.transition_clustering.ClusteredTransition;
import RAI.transition_clustering.Transition;
import RAI.transition_clustering.TransitionMerge;
import com.google.common.collect.Multiset;
import java.util.Iterator;
import java.util.PriorityQueue;


public class RedState implements State {


    public RedState(BlueState innerState){
        this.innerState = innerState;
    }

    @Override
    public int getId() {
        return innerState.getId();
    }

    @Override
    public Double getMu(){
        return innerState.getMu();
    }

    @Override
    public Transition getOutgoing(double value) {
        return innerState.getOutgoing(value);
    }

    @Override
    public Transition getIngoing(double value) {
        return innerState.getIngoing(value);
    }

    @Override
    public Transition getClosestOutgoing(double value) {
        return innerState.getClosestOutgoing(value);
    }

    @Override
    public Transition getClosestIngoing(double value) {
        return innerState.getClosestIngoing(value);
    }

    @Override
    public Multiset<Transition> getClosestOutgoing(Transition t) {
        return innerState.getClosestOutgoing(t);
    }

    @Override
    public Multiset<Transition> getClosestIngoing(Transition t) {
        return innerState.getClosestIngoing(t);
    }

    @Override
    public Multiset<Transition> getOutgoing(Transition t) {
        return innerState.getOutgoing(t);
    }

    @Override
    public Multiset<Transition> getIngoing(Transition t) {
        return innerState.getIngoing(t);
    }

    @Override
    public void addOutgoing(Transition t) {
        innerState.addOutgoing(t);
    }

    @Override
    public void addIngoing(Transition t) {
        innerState.addIngoing(t);
    }

    @Override
    public void removeOutgoing(Transition t) {
        innerState.removeOutgoing(t);
    }

    @Override
    public void removeIngoing(Transition t) {
        innerState.removeIngoing(t);
    }

    @Override
    public int getFutures() {
        return innerState.getFutures();
    }

    @Override
    public Iterator<Future> getFuturesIterator() {
        return innerState.getFuturesIterator();
    }

    @Override
    public Future getClosestFuture(Future f) {
        return innerState.getClosestFuture(f);
    }

    @Override
    public void addFuture(Future f) {
        innerState.addFuture(f);
    }

    @Override
    public void removeFuture(Future f) {
        innerState.removeFuture(f);
    }

    @Override
    public String toString() {
        String res = "<RED " + innerState.getId() + " [";
        Iterator<Transition> iterator = innerState.getOutgoingIterator();
        while (iterator.hasNext()) {
            Transition t = iterator.next();
            res += " " + t.getDestination().getId();
        }
        return res + " ]>";
    }

    @Override
    public boolean isLeaf(){
        return innerState.isLeaf();
    }

    @Override
    public Iterator<Transition> getIngoingIterator() {
        return innerState.getIngoingIterator();
    }

    @Override
    public Iterator<Transition> getOutgoingIterator() {
        return innerState.getOutgoingIterator();
    }

    @Override
    public int compareTo(State o) {
        return innerState.compareTo(o);
    }

    @Override
    public boolean equals(Object o){
        return innerState.equals(o);
    }

    @Override
    public void dispose() {
        innerState.dispose();
    }

    @Override
    public int hashCode(){
        return innerState.hashCode();
    }

    @Override
    public String toDot(){
        return innerState.toDot();
    }

    public void cluster(double significance){
        PriorityQueue<TransitionMerge> q = new PriorityQueue<>();
        // INIZIALIZATION
        inizializeClustering(q);
        // CLUSTERING
        performClustering(q, significance);
        // EXPANDING TRANSITIONS
        expandTransitions();
    }

    private void inizializeClustering(PriorityQueue<TransitionMerge> q){
        Iterator<Transition> fanout = innerState.getOutgoingIterator();
        ClusteredTransition previous = null;
        while (fanout.hasNext()){
            ClusteredTransition current = new ClusteredTransition(fanout.next());
            if (previous == null)
                previous = current;
            else{
                TransitionMerge m = new TransitionMerge(previous, current);
                previous.setNextMerge(m);
                current.setPreviousMerge(m);
                q.add(m);
                previous = current;
            }
        }
    }

    private void performClustering(PriorityQueue<TransitionMerge> q, double significance){
        //double RSSDelta = 0.;
        //while (n > MIN_TRANSITIONS && RSSDelta < significance && !q.isEmpty()){
        System.out.println("Clustering transitions of " + this);
        //while (RSSDelta < significance && ! q.isEmpty()){
        while (q.size() >= MIN_TRANSITIONS){
            TransitionMerge m = q.poll();
            ClusteredTransition f = m.getFirst();
            ClusteredTransition s = m.getSecond();
            if (addToCluster(f, s)) {
                //double fRSS = f.getRSS();
                //double sRSS = s.getRSS();
                //double fsRSS = f.getRSS();
                //RSSDelta = Math.abs(fsRSS - fRSS - sRSS);
                //System.out.println(fRSS + " " + sRSS + " " + fsRSS + " " + RSSDelta);
                // cleaning the queue
                //next
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
        Iterator<Transition> fanout = innerState.getOutgoingIterator();
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


    private BlueState innerState;
    public static final int MIN_TRANSITIONS = 4;


}
