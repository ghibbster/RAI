/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;


public interface Strategy {

    // scoring function
    public Double rank(State r, State b);

    // compatibility check
    public boolean assess(State r, State b);

    // distance measure between future continuations
    public Double assess(Future f1, Future f2);


}
