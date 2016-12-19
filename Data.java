/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;


public interface Data <T extends Data> {


    public void add(String[] values);

    public void updateWith(T b);

    public  Double rankWith(T b);

    public boolean isCompatibleWith(T b);

    public void dispose();


}
