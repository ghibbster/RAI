package RAI.transition_clustering;


import RAI.State;
import java.util.Iterator;


public class ClusteredTransition implements Transition{


    public ClusteredTransition(Transition t){
        transition = t;
        previous = null;
        next = null;
    }


    @Override
    public void setSource(State newDest) {
        transition.setSource(newDest);
    }

    @Override
    public double getMu() {
        return transition.getMu();
    }

    @Override
    public double getStd() {
        return transition.getStd();
    }

    @Override
    public State getSource() {
        return transition.getSource();
    }

    @Override
    public State getDestination() {
        return transition.getDestination();
    }

    @Override
    public void addAll(Transition t) {
        transition.addAll(t);
    }

    @Override
    public double getAvgEuclideanDistance(Transition t) {
        return transition.getAvgEuclideanDistance(t);
    }

    @Override
    public void setDestination(State newShape) {
        transition.setDestination(newShape);
    }

    @Override
    public double getLeftGuard() {
        return transition.getLeftGuard();
    }

    @Override
    public double getRightGuard() {
        return transition.getRightGuard();
    }

    @Override
    public void setLeftGuard(double v) {
        transition.setLeftGuard(v);
    }

    @Override
    public void setRightGuard(double v) {
        transition.setRightGuard(v);
    }

    @Override
    public void add(Double v) {
        transition.add(v);
    }

    @Override
    public Transition clone() throws CloneNotSupportedException {
        return (ClusteredTransition) super.clone();
    }

    @Override
    public int compareTo(Transition o) {
        return transition.compareTo(o);
    }

    @Override
    public Iterator<Double> iterator() {
        return transition.iterator();
    }

    @Override
    public boolean equals(Object o){
        return transition.equals(o);
    }

    @Override
    public int hashCode(){
        return transition.hashCode();
    }

    @Override
    public String toString(){
        return transition.toString();
    }

    public void setPreviousMerge(TransitionMerge m){
        previous = m;
    }

    public void setNextMerge(TransitionMerge m){
        next = m;
    }

    public TransitionMerge getPreviousMerge(){
        return previous;
    }

    public TransitionMerge getNextMerge(){
        return next;
    }

    public double getRSS(){
        double mu = transition.getMu();
        double rss = 0.;
        for (Double v : transition)
            rss += Math.pow(Math.abs(v - mu), 2.);
        return rss;
    }


    private Transition transition;
    private TransitionMerge previous;
    private TransitionMerge next;


}
