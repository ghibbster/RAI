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
 * Generic distance measure
 */
public interface DistanceMeasure {


    public void push(Double value1, Double value2);

    public Double compute();


}
