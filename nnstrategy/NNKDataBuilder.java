/*
 * Copyright (c) 2017, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.nnstrategy;

import RAI.DataBuilder;


public class NNKDataBuilder implements DataBuilder<NNKData> {

    public NNKDataBuilder(double alpha, int k){
        this.alpha = alpha;
        this.k = k;
    }

    @Override
    public NNKData createInstance() {
        return new NNKData(alpha, k);
    }


    private double alpha;
    private int k;

}
