/*
 * Copyright (c) 2017, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;


import RAI.nnstrategy.NNData;
import RAI.transition_clustering.Transition;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ReachabilityCountsDecorator<T extends Data<T>> {

    public ReachabilityCountsDecorator(Hypothesis<T> h){
        this.h = h;
        this.counts = new HashMap<>();
    }

    public ReachabilityCountsDecorator(Hypothesis<T> h, String aPath){
        this(h);
        addCountsFrom(aPath);
    }

    public void addCountsFrom(String aPath) {
        // it collects reachability counts for each transition in this'h,
        // given a test file located in aPath.
        try (BufferedReader br = new BufferedReader(new FileReader(aPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                State<T> state = h.getRoot();
                for (String cvalue : values) {
                    double value = new Double(cvalue);
                    if (state.isLeaf())
                        // then we restart the computation from the start state
                        state = h.getRoot();
                    // we are sure, by construction, that there will exist at least one transition covering value.
                    Transition<T> fired = state.getOutgoing(value);
                    // we update the counts
                    if (!counts.containsKey(fired))
                        counts.put(fired, 1);
                    else counts.put(fired, counts.get(fired) + 1);
                    state = fired.getDestination();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String decoratedStateDot(State<T> s){
        int lId = (s.isRoot() && s.getId() != 0)?(0):(s.getId());
        String rep = lId + " [shape=circle, label=\"" + lId + "\"];";
        Iterator<Transition<T>> outIter = s.getOutgoingIterator();
        while (outIter.hasNext()) {
            Transition<T> t = outIter.next();
            if (t.getDestination() != null) {
                String rightBra = (t.getRightGuard() == Double.POSITIVE_INFINITY) ? ("[") : ("]");
                // we relabel the start state to 0 by convention
                int lDestId = (t.getDestination().isRoot() && t.getDestination().getId() != 0) ? (0) : (t.getDestination().getId());
                int cs = (counts.get(t) == null)?(0):(counts.get(t));
                rep += "\n\t" + lId + " -> " + lDestId +
                        " [label=\"]" + String.format(Locale.ENGLISH, "%.10f", t.getLeftGuard()) +
                        ", " + String.format(Locale.ENGLISH, "%.10f", t.getRightGuard()) + rightBra +
                        " " + String.format(Locale.ENGLISH, "%.10f", t.getMu()) +
                        " " + cs + "\"];";
            }
        }
        return rep;
    }

    public void toDot(String outPath){
        // it serializes h in dot format, including reachability counts for transitions.
        // Stores everything in outPath.
        try {
            Set<State<T>> visited = new HashSet<>();
            LinkedList<State<T>> toVisit = new LinkedList<>();
            toVisit.addFirst(h.getRoot());
            FileWriter writer = new FileWriter(outPath, false);
            writer.write("digraph DFA {");
            while (! toVisit.isEmpty()) {
                State<T> s = toVisit.removeFirst();
                if (! visited.contains(s)) {
                    visited.add(s);
                    // now we print the decorated transitions
                    writer.write("\n" + decoratedStateDot(s));
                    Iterator<Transition<T>> iterator = s.getOutgoingIterator();
                    while (iterator.hasNext()) {
                        Transition<T> t = iterator.next();
                        State<T> next = t.getDestination();
                        if ((next != null) && (!visited.contains(next)))
                            toVisit.addFirst(next);
                    }
                }
            }
            writer.write("\n}");
            writer.close();
        } catch (IOException o){
            o.printStackTrace();
        }
    }

    private Hypothesis<T> h;
    private Map<Transition<T>, Integer> counts;


    public static void main(String[] args){
        String modelPath = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/sinus/sinus10.slided.DOT";
        String checkPath = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/sinus/sinus10.slided";
        String outPath = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/sinus/sinus10.slided.reach.DOT";
        Hypothesis<NNData> h = new Hypothesis<>();
        h.fromDOT(modelPath);
        System.out.println("model loaded");
        ReachabilityCountsDecorator<NNData> r = new ReachabilityCountsDecorator<>(h, checkPath);
        r.toDot(outPath);
    }

}
