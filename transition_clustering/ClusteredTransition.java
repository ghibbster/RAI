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
import java.util.Iterator;


public class ClusteredTransition<T extends Data<T>> implements Transition<T>{


    public ClusteredTransition(Transition<T> t){
        transition = t;
        previous = null;
        next = null;
    }


    @Override
    public void setSource(State<T> newDest) {
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
    public State<T> getSource() {
        return transition.getSource();
    }

    @Override
    public State<T> getDestination() {
        return transition.getDestination();
    }

    @Override
    public void addAll(Transition<T> t) {
        transition.addAll(t);
    }

    @Override
    public double getCloseness(Transition<T> t) {
        return transition.getCloseness(t);
    }

    @Override
    public void setDestination(State<T> newShape) {
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
    public boolean isAdiacenTo(Transition<T> t){
        return transition.isAdiacenTo(t);
    }

    @Override
    public boolean isOverlappedBy(Transition<T> t){
        return transition.isOverlappedBy(t);
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

    public void setPreviousMerge(TransitionMerge<T> m){
        previous = m;
    }

    public void setNextMerge(TransitionMerge<T> m){
        next = m;
    }

    public TransitionMerge<T> getPreviousMerge(){
        return previous;
    }

    public TransitionMerge<T> getNextMerge(){
        return next;
    }



    private Transition<T> transition;
    private TransitionMerge<T> previous;
    private TransitionMerge<T> next;


}
