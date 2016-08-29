/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;


public class StatesCouple {


    public StatesCouple(State first, State second){
        this.first = first;
        this.second = second;
    }

    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (! (o instanceof StatesCouple))
            return false;
        StatesCouple sc = (StatesCouple) o;
        return first.equals(sc.first) && second.equals(sc.second);
    }

    public String toString(){
        return "{ " + first + " - " + second + " }";
    }

    public int hashCode(){
        int res = 0;
        if (first != null)
            res += first.hashCode();
        if (second != null)
            res += second.hashCode();
        return res;
    }


    public final State first;
    public final State second;

}
