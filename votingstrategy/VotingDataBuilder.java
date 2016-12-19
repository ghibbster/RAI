/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI.votingstrategy;

import RAI.DataBuilder;


public class VotingDataBuilder implements DataBuilder<VotingData> {

    public VotingDataBuilder(double valueThreshold, double votingThreshold){
        this.valueThreshold = valueThreshold;
        this.votingThreshold = votingThreshold;
    }

    @Override
    public VotingData createInstance() {
        return new VotingData(valueThreshold, votingThreshold);
    }

    private double votingThreshold;
    private double valueThreshold;

}
