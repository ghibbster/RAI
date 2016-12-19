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


public interface Transition<T extends Data<T>> extends Iterable<Double>, Comparable<Transition>, Cloneable{


    void setSource(State<T> newDest);

    double getMu();

    public double getStd();

    State<T> getSource();

    State<T> getDestination();

    void addAll(Transition<T> t);

    double getCloseness(Transition<T> t);

    void setDestination(State<T> newShape);

    double getLeftGuard();

    double getRightGuard();

    void setLeftGuard(double v);

    void setRightGuard(double v);

    void add(Double v);

    boolean isAdiacenTo(Transition<T> t);

    boolean isOverlappedBy(Transition<T> t);


}
