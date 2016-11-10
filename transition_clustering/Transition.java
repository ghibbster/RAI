/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.transition_clustering;

import RAI.State;


public interface Transition extends Iterable<Double>, Comparable<Transition>, Cloneable{


    void setSource(State newDest);

    double getMu();

    public double getStd();

    State getSource();

    State getDestination();

    void addAll(Transition t);

    double getCloseness(Transition t);

    void setDestination(State newShape);

    double getLeftGuard();

    double getRightGuard();

    void setLeftGuard(double v);

    void setRightGuard(double v);

    void add(Double v);

    public Transition clone() throws CloneNotSupportedException;


}
