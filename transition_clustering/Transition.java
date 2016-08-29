package RAI.transition_clustering;

import RAI.State;


public interface Transition extends Iterable<Double>, Comparable<Transition>, Cloneable{


    void setSource(State newDest);

    double getMu();

    public double getStd();

    State getSource();

    State getDestination();

    void addAll(Transition t);

    double getAvgEuclideanDistance(Transition t);

    void setDestination(State newShape);

    double getLeftGuard();

    double getRightGuard();

    void setLeftGuard(double v);

    void setRightGuard(double v);

    void add(Double v);

    public Transition clone() throws CloneNotSupportedException;


}