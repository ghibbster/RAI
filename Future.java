/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;

import java.util.Iterator;
import java.util.LinkedList;


public class Future implements Iterable<Double>, Comparable<Future>, Cloneable{


    private Future(){
        values = new LinkedList<>();
    }

    public static Future parse(String[] values){
        Future f = new Future();
        for (String v : values)
            f.add(new Double(v));
        return f;
    }

    public String toString(){
        String ret = "F[";
        for (Double value : values)
            ret += " " + value;
        return ret + " ]";
    }

    public Iterator<Double> iterator(){
        return values.iterator();
    }

    public void add(Double v){
        values.add(v);
    }

    public void addFirst(Double v) {
        values.addFirst(v);
    }

    public int compareTo(Future f){
        return values.getFirst().compareTo(f.getFirst());
    }

    public Double getFirst(){
        return values.getFirst();
    }

    public int size(){
        return values.size();
    }

    @Override
    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof Future))
            return false;
        Future f = (Future) o;
        if (values.size() != f.size())
            return false;
        Iterator<Double> fi = values.iterator();
        for (Double v : f)
            if (! v.equals(fi.next()))
                return false;
        return true;
    }

    @Override
    public int hashCode(){
        return values.hashCode();
    }


    private LinkedList<Double> values;


}
