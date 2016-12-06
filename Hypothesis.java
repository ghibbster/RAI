/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;

import RAI.strategies.AvgPrefixEuclidean;
import RAI.transition_clustering.Transition;
import RAI.transition_clustering.UnclusteredTransition;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// In questa versione sono apportati cambi NON ancora concettuali ma di riorganizzazione del codice.
// Principali cambiamenti:
// 1) Hypothesis ora contiene solo l'algoritmo di learning. Ciò vuol dire che promote, fold, ecc sono spostate in State
// 2) State ha maggior responsabilità, contiene cose che prima erano appannaggio di Hypothesis


public class Hypothesis {



    public Hypothesis(){
        root = new State(this);
        merges = new PriorityQueue<>();
        redStates = new HashSet<>();
    }

    private void prefixTree(String trainingPath){
        try (BufferedReader br = new BufferedReader(new FileReader(trainingPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                Future future = Future.parse(values, strategy);
                State state = root;
                for (int i = 0; i < values.length; i++){
                    double value = new Double(values[i]);
                    if (state.getOutgoing(value) == null) {
                        Transition newT = new UnclusteredTransition(state, new State(this), value);
                        state.addOutgoing(newT);
                        newT.getDestination().addIngoing(newT);
                    }
                    state.addFuture(future.getSuffix(i));
                    // a questo punto ci sarà sicuro la transizione uscente per value
                    state = state.getOutgoing(value).getDestination();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fromDOT(String modelPath){
        // READ A MODEL FROM A DOT FILE
        try (BufferedReader br = new BufferedReader(new FileReader(modelPath))) {
            String line;
            Map<Integer, State> states = new HashMap<>();
            State current;
            while ((line = br.readLine()) != null) {
                Matcher lMatcher = stateRE.matcher(line);
                if (lMatcher.matches()){
                    //state id and mu
                    Integer sid = Integer.parseInt(lMatcher.group("sid"));
                    Double mu = Double.parseDouble(lMatcher.group("mu"));
                    if (! states.containsKey(sid))
                        states.put(sid, new State(this, sid));
                    current = states.get(sid);
                    current.setMu(mu);
                    if (sid.equals(0))
                        root = current;
                } else {
                    lMatcher = transRE.matcher(line);
                    if (lMatcher.matches()){
                        Integer ssid = Integer.parseInt(lMatcher.group("ssid"));
                        Integer dsid = Integer.parseInt(lMatcher.group("dsid"));
                        Double lguard = Double.parseDouble(lMatcher.group("lguard"));
                        Double rguard = Double.parseDouble(lMatcher.group("rguard"));
                        //System.out.println("BUM " + ssid + " " + dsid + " " + lguard + " " + rguard);
                        if (! states.containsKey(dsid))
                            states.put(dsid, new State(this, dsid));
                        current = states.get(ssid);
                        Transition t = new UnclusteredTransition(current, states.get(dsid), lguard, rguard);
                        current.addOutgoing(t);
                        t.getDestination().addIngoing(t);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStrategy(Strategy strategy){
        this.strategy = strategy;
    }

    // CANDIDATE MERGES STUFF

    public void notifyPromotion(State s){
        // it gets called before updating s's fields as a consequence of the promotion
        if (s.isBlue()) {
            redStates.add(s);
            Iterator<CandidateMerge> pairs = s.getMergesIterator();
            while (pairs.hasNext()){
                CandidateMerge pair = pairs.next();
                merges.remove(pair);
                pairs.remove();
            }
        }else if (s.isWhite())
            for (State redState : redStates) {
                CandidateMerge pair = new CandidateMerge(redState, s);
                pair.computeScore(strategy);
                merges.add(pair);
                s.addMerge(pair);
            }
    }

    public void notifyDisposal(State s){
        Iterator<CandidateMerge> pairs = s.getMergesIterator();
        Collection<CandidateMerge> toRemove = new LinkedList<>();
        // gathering pairs to remove
        while (pairs.hasNext())
            toRemove.add(pairs.next());
        // doing the actual removal
        for (CandidateMerge pair : toRemove) {
            merges.remove(pair);
            s.removeMerge(pair);
        }
    }

    // END OF CANDIDATE MERGES STUFF

    public void minimize(String samplePath){
        prefixTree(samplePath);
        root.promote().promote();
        System.out.println("Prefix Tree created");
        while (! merges.isEmpty()){
            CandidateMerge pair = merges.poll();
            System.out.println("Considering couple " + pair);
            State bs = pair.getBlueState();
            if (strategy.assess(pair)) {
                State rs = pair.getRedState();
                rs.mergeWith(bs);
            }else {
                System.out.println("Discarding " + pair);
                merges.remove(pair);
                bs.removeMerge(pair);
                if (! bs.hasMerges())
                    bs.promote();
            }
        }
    }

    public void toDot(String path){
        try {
            Set<State> visited = new HashSet<>();
            LinkedList<State> toVisit = new LinkedList<>();
            toVisit.addFirst(root);
            FileWriter writer = new FileWriter(path, false);
            writer.write("digraph DFA {");
            while (! toVisit.isEmpty()) {
                State s = toVisit.removeFirst();
                if (! visited.contains(s)) {
                    visited.add(s);
                    writer.write("\n" + s.toDot());
                    Iterator<Transition> iterator = s.getOutgoingIterator();
                    while (iterator.hasNext()) {
                        Transition t = iterator.next();
                        State next = t.getDestination();
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


    private State root;
    private PriorityQueue<CandidateMerge> merges;
    private Set<State> redStates;
    private Strategy strategy;
    private static final Pattern stateRE = Pattern.compile(
            "^(?<sid>\\d+) \\[shape=(circle|doublecircle), label=\\\"\\d+\\\\n(?<mu>-?\\d*.?\\d+)\\\"\\];$");
    private static final Pattern transRE = Pattern.compile(
            "^\\t(?<ssid>\\d+) -> (?<dsid>\\d+) \\[label=\"(\\[|\\])(?<lguard>(-?\\d*\\.?\\d+)|(-Infinity)), " +
                    "(?<rguard>(-?\\d*.?\\d+)|(Infinity))(\\]|\\[)\\\"\\];$");

    //MAIN ROUTINES

    //unit test
    public static void main(String[] args){
        String train = "/home/npellegrino/LEMMA/state_merging_regressor/data/suite/3states/3states.sample";
        //double threshold = 0.41355618141549232;
        double threshold = 0.18;
        //double threshold = 1.9;
        String dot = train + ".DOT";
        Hypothesis h = new Hypothesis();
        h.setStrategy(new AvgPrefixEuclidean(threshold));
        //h.setStrategy(new VotingWithPrefixes(5., .2));
        h.minimize(train);
        h.toDot(dot);
        System.out.println("#states: " + h.redStates.size());
        System.out.println(h.redStates);
    }


}
