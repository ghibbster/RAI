package RAI;

import RAI.transition_clustering.Transition;
import com.google.common.collect.Multiset;
import java.util.*;


public class BlueState implements State {


    public BlueState(WhiteState innerState){
        this.innerState = innerState;
        pairs = new HashSet<>();
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
    public String toString() {
        String res = "<BLUE " + innerState.getId() + " [";
        Iterator<Transition> iterator = innerState.getOutgoingIterator();
        while (iterator.hasNext()){
            Transition t = iterator.next();
            res += " " + t.getDestination().getId();
        }
        return res + " ]>";
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
    public int hashCode(){
        return innerState.hashCode();
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
    public String toDot(){
        return innerState.toDot();
    }

    public boolean hasMerges(){
        return ! pairs.isEmpty();
    }

    public void addMerge(Hypothesis.CandidateMerge pair){
        pairs.add(pair);
    }

    public void removeMerge(Hypothesis.CandidateMerge pair){pairs.remove(pair);}

    public void dispose(){
        //rimuovo tutte le coppie con this come bluestate !
        Iterator<Hypothesis.CandidateMerge> pairIterator = pairs.iterator();
        while (pairIterator.hasNext()){
            Hypothesis.CandidateMerge p = pairIterator.next();
            pairIterator.remove();
            p.dispose();
        }
        innerState.dispose();
    }

    public RedState promote(double significance){
        RedState newShape = new RedState(this);
        Iterator<Transition> iterator = innerState.getIngoingIterator();
        while (iterator.hasNext()){
            Transition t = iterator.next();
            t.setDestination(newShape);
        }
        iterator = innerState.getOutgoingIterator();
        while (iterator.hasNext()){
            Transition t = iterator.next();
            t.setSource(newShape);
        }
        newShape.cluster(significance);
        return newShape;
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
    public boolean isLeaf(){
        return innerState.isLeaf();
    }




    private WhiteState innerState;
    private Collection<Hypothesis.CandidateMerge> pairs;


}
