/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.transition_clustering;

import RAI.Data;
import RAI.State;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class Transition<T extends Data<T>> implements Iterable<Double>, Comparable<Transition>, Cloneable{


    public Transition(State<T> source, State<T> destination) {
        this.source = source;
        this.destination = destination;
        this.values = new LinkedList<>();
        this.mu = 0.;
        this.oldmu = 0.;
        this.sigmasquared = 0.;
        this.oldsigmasquared = 0.;
        this.leftguard = Double.POSITIVE_INFINITY;
    }

    public Transition(State<T> source, State<T> destination, Double leftguard, Double rightguard){
        this.source = source;
        this.destination = destination;
        this.leftguard = leftguard;
        this.rightguard = rightguard;
        this.mu = 0.;
        this.oldmu = 0.;
        this.sigmasquared = 0.;
        this.oldsigmasquared = 0.;
    }

    public Transition(State<T> source, State<T> destination, double value) {
        this(source, destination);
        this.add(value);
    }

    public State<T> getSource() {
        return source;
    }

    public void setSource(State<T> source) {
        this.source = source;
    }

    public State<T> getDestination() {
        return destination;
    }

    public void setDestination(State<T> destination) {
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

    public void addAll(Transition<T> t){
        for (Double v : t)
            this.add(v);
    }

    public double getCloseness(Transition<T> t){
        // future based distance used for clustering purposes
        T thisData = destination.getData();
        T tData = t.getDestination().getData();
        double futureContribution = thisData.rankWith(tData);
        double localContribution = Math.abs(mu - t.getMu());
        return (futureContribution != 0.)?(localContribution):(localContribution * futureContribution);
    }

    public boolean isAdiacenTo(Transition t){
        return (t.getRightGuard() == leftguard || t.getLeftGuard() == rightguard) &&
                destination.equals(t.getDestination());
    }

    public boolean isOverlappedBy(Transition t){
        if (equals(t))
            return true;
        // this and t are the same singleton transitions
        if (leftguard == rightguard && t.getLeftGuard() == t.getRightGuard() && rightguard == t.getRightGuard())
            return true;
        // overlap on the right side of this
        if (t.getLeftGuard() < rightguard && t.getRightGuard() >= rightguard)
            return true;
        // overlap on the left side of this
        if (t.getRightGuard() > leftguard && t.getLeftGuard() <= leftguard)
            return true;
        // total inclusion of t in this
        if (t.getLeftGuard() > leftguard && t.getRightGuard() <= rightguard)
            return true;
        return false;
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
    public String toString(){
        return "( " + source.getId() + " [ " + leftguard + ", " + rightguard + " ] " + destination.getId() + " )";
    }


    private State<T> source;
    private State<T> destination;
    private double leftguard;
    private double rightguard;
    private double mu;
    private double sigmasquared;
    private double oldmu;
    private double oldsigmasquared;
    private Collection<Double> values;


}
