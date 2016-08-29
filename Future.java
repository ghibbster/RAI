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


public class Future implements Iterable<Double>, Comparable<Future>{


    public Future(){
        values = new LinkedList<>();
    }

    public static Future parse(String[] values){
        Future f = new Future();
        for (String v : values)
            f.add(new Double(v));
        return f;
    }

    public Future getSuffix(int i){
        Future f = new Future();
        int j = 0;
        for (Double v : values) {
            if (j >= i)
                f.add(v);
            j += 1;
        }
        return f;
    }

    // DYNAMIC TIME WARPING DISTANCE IMPLEMENTATION (not used at the moment)
//    public double getDTWScore(RAI.Future f){
//        // naive implementation, could get improved in space complexity.
//        // turning values in arrays
//        int sSize = values.size();
//        int tSize = f.values.size();
//        Double[] s = values.toArray(new Double[sSize]);
//        Double[] t = f.values.toArray(new Double[tSize]);
//        double[][] dtw = new double[sSize + 1][tSize + 1];
//        // initialization
//        for (int i = 0; i < sSize + 1; i++)
//            dtw[i][0] = Double.POSITIVE_INFINITY;
//        for (int j = 0; j < tSize + 1; j++)
//            dtw[0][j] = Double.POSITIVE_INFINITY;
//        dtw[0][0] = 0.;
//        // computing every subproblem
//        for (int i = 1; i < sSize + 1; i++)
//            for (int j = 1; j < tSize + 1; j++){
//                dtw[i][j] = Math.abs(s[i - 1] - t[j - 1]);
//                if (dtw[i - 1][j] < dtw[i][j - 1] && dtw[i - 1][j] < dtw[i - 1][j - 1])
//                    dtw[i][j] += dtw[i - 1][j];
//                else if (dtw[i][j - 1] < dtw[i - 1][j] && dtw[i][j - 1] < dtw[i - 1][j - 1])
//                    dtw[i][j] += dtw[i][j - 1];
//                else
//                    //dtw[i - 1][j - 1] < dtw[i - 1][j] && dtw[i - 1][j - 1] < dtw[i][j - 1]
//                    dtw[i][j] += dtw[i - 1][j - 1];
//            }
//        // ready to return
//        return dtw[sSize][tSize];
//    }

    public double getAvgPrefixEuclideanScore(Future f){
        double result = 0.;
        if (f == null || f.size() == 0){
            for (Double v : values)
                result += Math.pow(v, 2.);
            if (values.size() == 0)
                return Double.POSITIVE_INFINITY;
            return Math.sqrt(result) / (double) values.size();
        }
        int n = 0;
        Iterator<Double> i = f.iterator();
        for (Double v : values){
            if (! i.hasNext())
                break;
            result += Math.pow(v - i.next(), 2.);
            n += 1;
        }
        if (n == 0)
            return Double.POSITIVE_INFINITY;
        return Math.sqrt(result) / (double) n;
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


    //UNIT TEST
    public static void main(String[] args){
        Future f1 = Future.parse(new String[]{"2.0", "5.2", "7.2"});
        Future f2 = Future.parse(new String[]{"5.0", "8.8", "12.1"});
        System.out.println(f1);
        System.out.println(f2);
        System.out.println(f1.getSuffix(1));
        System.out.println(f2.getAvgPrefixEuclideanScore(null));
    }


}
