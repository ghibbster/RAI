/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;


public class CandidateMerge{


    public CandidateMerge(State rs, State bs){
        redState = rs;
        blueState = bs;
    }

    //UTILITY

    public boolean equals(Object o){
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof CandidateMerge))
            return false;
        CandidateMerge s = (CandidateMerge) o;
        return redState.equals(s.redState) && blueState.equals(s.blueState);
    }

    @Override
    public int hashCode(){
        return redState.hashCode() + blueState.hashCode();
    }

    @Override
    public String toString(){
        return "{" + redState.getId() + ":" + blueState.getId() + "}";
    }

    public State getRedState() {
        return redState;
    }

    public State getBlueState() {
        return blueState;
    }


    private State redState;
    private State blueState;


}
