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
 * Mean Squared Error measure
 */
public class MeanSquaredError implements DistanceMeasure{


    public MeanSquaredError(){
        sum = 0.;
        n = 0;
    }

    @Override
    public void push(Double value1, Double value2) {
        if (value1 == null)
            value1 = 0.;
        if (value2 == null)
            value2 = 0.;
        sum += (value1 - value2) * (value1 - value2);
        n += 1;
    }

    @Override
    public Double compute() {
        if (n == 0)
            return Double.POSITIVE_INFINITY;
        return Math.sqrt(sum) / (double) n;
    }

    private Double sum;
    private int n;

}
