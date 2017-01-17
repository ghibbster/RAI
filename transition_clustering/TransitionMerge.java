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

public class TransitionMerge<T extends Data<T>> implements Comparable<TransitionMerge<T>>{


    public TransitionMerge(Transition<T> f, Transition<T> s){
        first = f;
        second = s;
        score = f.getCloseness(s);
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
        return score;
    }

    public void updateScore(){
        score = first.getCloseness(second);
    }

    @Override
    public int compareTo(TransitionMerge<T> o) {
        if ((first.equals(o.getFirst()) && second.equals(o.getSecond())) ||
        (first.equals(o.getSecond())) && second.equals(o.getFirst()))
            return 0;
        return (score > o.getScore())?(1):(-1);
    }

    @Override
    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (! (o instanceof TransitionMerge))
            return false;
        TransitionMerge t = (TransitionMerge) o;
        return first.equals(t.first) && second.equals(t.second);
    }

    @Override
    public int hashCode(){
        return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString(){
        return first + " --- " + second + " --- " + score;
    }

    public void setFirst(Transition<T> first) {
        this.first = first;
    }

    public void setSecond(Transition<T> second) {
        this.second = second;
    }

    public TransitionMerge<T> getNext() {
        return next;
    }

    public TransitionMerge<T> getPrevious() {
        return previous;
    }

    public void setNext(TransitionMerge<T> next) {
        this.next = next;
    }

    public void setPrevious(TransitionMerge<T> previous) {
        this.previous = previous;
    }


    private Transition<T> first;
    private Transition<T> second;
    private TransitionMerge<T> previous;
    private TransitionMerge<T> next;
    private double score;


}
