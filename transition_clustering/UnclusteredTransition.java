package RAI.transition_clustering;

import RAI.Future;
import RAI.State;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class UnclusteredTransition implements Transition{


    public UnclusteredTransition(State source, State destination) {
        this.source = source;
        this.destination = destination;
        this.values = new LinkedList<>();
        this.mu = 0.;
        this.oldmu = 0.;
        this.sigmasquared = 0.;
        this.oldsigmasquared = 0.;
        this.leftguard = Double.POSITIVE_INFINITY;
        this.rightguard = Double.NEGATIVE_INFINITY;
    }

    public UnclusteredTransition(State source, State destination, Double leftguard, Double rightguard){
        this.source = source;
        this.destination = destination;
        this.leftguard = leftguard;
        this.rightguard = rightguard;
        this.mu = 0.;
        this.oldmu = 0.;
        this.sigmasquared = 0.;
        this.oldsigmasquared = 0.;
    }

    public UnclusteredTransition(State source, State destination, double value) {
        this(source, destination);
        this.add(value);
    }

    public State getSource() {
        return source;
    }

    public void setSource(State source) {
        this.source = source;
    }

    public State getDestination() {
        return destination;
    }

    public void setDestination(State destination) {
        this.destination = destination;
    }

    public double getLeftGuard() {
        return leftguard;
    }

    public double getRightGuard() {
        return rightguard;
    }

    public void setLeftGuard(double v){
        leftguard = v;
    }

    public void setRightGuard(double v){
        rightguard = v;
    }

    public double getMu(){
        return mu;
    }

    public double getStd(){
        return Math.sqrt(sigmasquared);
    }

    public void add(Double value){
        if (values.isEmpty()){
            leftguard = value;
            rightguard = value;
            mu = value;
            oldmu = value;
        } else {
            if (value < leftguard)
                leftguard = value;
            if (value > rightguard)
                rightguard = value;
            mu = oldmu + (value - oldmu) / ((double) values.size() + 1);
            sigmasquared = oldsigmasquared + (value - oldmu) * (value - mu);
            oldmu = mu;
            oldsigmasquared = sigmasquared;
        }
        values.add(value);
    }

    public void addAll(Transition t){
        for (Double v : t)
            add(v);
    }

    public double getAvgEuclideanDistance(Transition t){
        // future based distance used for clustering purposes
        // quadratic in the number of futures in this.destination and t.destination
        State tDest = t.getDestination();
        if (destination.isLeaf() && tDest.isLeaf())
            // if both the destinations have no futures ... then ok, zero distance
            return 0.;
        if (destination.isLeaf()){
            double sum = 0.;
            int n = 0;
            Iterator<Future> fs = tDest.getFuturesIterator();
            while (fs.hasNext()){
                Future tf = fs.next();
                n += 1;
                sum += tf.getAvgPrefixEuclideanScore(null);
            }
            return sum / ((double) n);
        }
        if (tDest.isLeaf()){
            double sum = 0.;
            int n = 0;
            Iterator<Future> fs = destination.getFuturesIterator();
            while (fs.hasNext()){
                Future tf = fs.next();
                n += 1;
                sum += tf.getAvgPrefixEuclideanScore(null);
            }
            return sum / ((double) n);
        }
        // both destination and tDest are not null
        double sum = 0.;
        Iterator<Future> thisfs = tDest.getFuturesIterator();
        int n = 0;
        //System.out.println(">>>");
        while (thisfs.hasNext()){
            Future thisf = thisfs.next();
            Future tf = destination.getClosestFuture(thisf);
            //System.out.println("F " + thisf + " " + tf + " " + thisf.getAvgPrefixEuclideanScore(tf));
            sum += thisf.getAvgPrefixEuclideanScore(tf);
            n += 1;
        }
        //System.out.println("<<<");
        Iterator<Future> fs = destination.getFuturesIterator();
        while (fs.hasNext()){
            Future tf = fs.next();
            Future thisf = tDest.getClosestFuture(tf);
            //System.out.println("F " + tf + " " + thisf + " " + thisf.getAvgPrefixEuclideanScore(tf));
            sum += tf.getAvgPrefixEuclideanScore(thisf);
            n += 1;
        }
        //System.out.println("---");
        return sum / ((double) n);
    }

    @Override
    public Iterator<Double> iterator() {
        return values.iterator();
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (! (o instanceof Transition))
            return false;
        Transition t = (Transition) o;
        return (source.equals(t.getSource()) && destination.equals(t.getDestination()) &&
                leftguard == t.getLeftGuard() && rightguard == t.getRightGuard());
    }

    public int hashCode(){
        return source.hashCode() + destination.hashCode() +
                new Double(leftguard).hashCode() + new Double(rightguard).hashCode();
    }

    @Override
    public int compareTo(Transition o) {
        if (leftguard < o.getLeftGuard())
            return -1;
        if (leftguard > o.getLeftGuard())
            return 1;
        // leftguards are equal at this point
        if (rightguard < o.getRightGuard())
            return -1;
        if (rightguard > o.getRightGuard())
            return 1;
        // both guards are equal at this point
        if (source.getId() != o.getSource().getId())
            return source.getId() - o.getSource().getId();
        // sources are equals at this point
        return destination.getId() - o.getDestination().getId();
    }

    @Override
    public Transition clone() throws CloneNotSupportedException{
        UnclusteredTransition cloned = (UnclusteredTransition) super.clone();
        cloned.values = new LinkedList<>();
        for (Double v : values)
            cloned.values.add(v);
        return cloned;
    }

    @Override
    public String toString(){
        return "( " + source.getId() + " [ " + leftguard + ", " + rightguard + " ] " + destination.getId() + " )";
    }



    private State source;
    private State destination;
    private double leftguard;
    private double rightguard;
    private double mu;
    private double sigmasquared;
    private double oldmu;
    private double oldsigmasquared;
    private Collection<Double> values;


}
