/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.distance_measures;

/**
 * Given 2 sequences:
 * -) 2, 3, 4, 5
 * -) 1, 7
 * the result is 4 (3 of first sequence minus 7 of the second).
 */
public class MaxPrefixDifference implements DistanceMeasure{


    public MaxPrefixDifference(){
        maxDifference = 0.;
    }


    @Override
    public void push(Double value1, Double value2) {
        if (value1 != null && value2 != null){
            Double diff = Math.abs(value1 - value2);
            if (diff > maxDifference)
                maxDifference = diff;
        }
    }

    @Override
    public Double compute() {
        return maxDifference;
    }


    private Double maxDifference;


}
