/*
 * Copyright (c) 2017, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.transition_clustering;



import RAI.Data;

public class TransitionMergeNew<T extends Data<T>> implements Comparable<TransitionMergeNew<T>>{


    public TransitionMergeNew(Transition<T> f, Transition<T> s){
        first = f;
        second = s;
        // score = f.getCloseness(s);
        previous = null;
        next = null;
    }

    public Transition<T> getFirst() {
        return first;
    }

    public Transition<T> getSecond() {
        return second;
    }

    public double getScore() {
        return first.getCloseness(second);
    }

//    public void updateScore(){
//        score = first.getCloseness(second);
//    }

    @Override
    public int compareTo(TransitionMergeNew<T> o) {
        if ((first.equals(o.getFirst()) && second.equals(o.getSecond())) ||
        (first.equals(o.getSecond())) && second.equals(o.getFirst()))
            return 0;
        return (getScore() > o.getScore())?(1):(-1);
    }

    @Override
    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (! (o instanceof TransitionMergeNew))
            return false;
        TransitionMergeNew t = (TransitionMergeNew) o;
        return first.equals(t.first) && second.equals(t.second);
    }

    @Override
    public int hashCode(){
        return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString(){
        return first + " --- " + second + " --- " + getScore();
    }

    public void setFirst(Transition<T> first) {
        this.first = first;
    }

    public void setSecond(Transition<T> second) {
        this.second = second;
    }

    public TransitionMergeNew<T> getNext() {
        return next;
    }

    public TransitionMergeNew<T> getPrevious() {
        return previous;
    }

    public void setNext(TransitionMergeNew<T> next) {
        this.next = next;
    }

    public void setPrevious(TransitionMergeNew<T> previous) {
        this.previous = previous;
    }


    private Transition<T> first;
    private Transition<T> second;
    private TransitionMergeNew<T> previous;
    private TransitionMergeNew<T> next;
    //private double score;


}
