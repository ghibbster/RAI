package RAI.transition_clustering;


public class TransitionMerge implements Comparable<TransitionMerge>{


    public TransitionMerge(ClusteredTransition f, ClusteredTransition s){
        first = f;
        second = s;
        score = f.getAvgEuclideanDistance(s);
    }

    public ClusteredTransition getFirst() {
        return first;
    }

    public ClusteredTransition getSecond() {
        return second;
    }

    public double getScore() {
        return score;
    }

    public void updateScore(){
        first.getAvgEuclideanDistance(second);
    }

    @Override
    public int compareTo(TransitionMerge o) {
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

    public void setFirst(ClusteredTransition first) {
        this.first = first;
    }

    public void setSecond(ClusteredTransition second) {
        this.second = second;
    }

    private ClusteredTransition first;
    private ClusteredTransition second;
    private double score;


}
