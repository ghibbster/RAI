/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.nnstrategy;

import RAI.DataBuilder;


public class NNDataBuilder implements DataBuilder<NNData> {

    public NNDataBuilder(double alpha){
        this.alpha = alpha;
    }

    @Override
    public NNData createInstance() {
        return new NNData(alpha);
    }


    private double alpha;

}
