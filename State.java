package RAI;

import RAI.transition_clustering.Transition;
import com.google.common.collect.Multiset;
import java.util.Iterator;


public interface State extends Comparable<State>{


    public int getId();

    public Transition getOutgoing(double value);

    public Transition getIngoing(double value);

    public Transition getClosestOutgoing(double value);

    public Transition getClosestIngoing(double value);

    public Double getMu();

    public Multiset<Transition> getClosestOutgoing(Transition t);

    public Multiset<Transition> getClosestIngoing(Transition t);

    public Multiset<Transition> getOutgoing(Transition t);

    public Multiset<Transition> getIngoing(Transition t);

    public void addOutgoing(Transition t);

    public void addIngoing(Transition t);

    public void removeOutgoing(Transition t);

    public void removeIngoing(Transition t);

    public int getFutures();

    public Iterator<Future> getFuturesIterator();

    public Future getClosestFuture(Future f);

    public void addFuture(Future f);

    public void removeFuture(Future f);

    public Iterator<Transition> getIngoingIterator();

    public Iterator<Transition> getOutgoingIterator();

    public String toDot();

    public boolean isLeaf();

    public int hashCode();

    public boolean equals(Object o);

    public void dispose();


}
