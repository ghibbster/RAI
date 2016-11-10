/*
 * Copyright (c) 2016, Gaetano Pellegrino
 *
 * This program is released under the GNU General Public License
 * Info online: http://www.gnu.org/licenses/quick-guide-gplv3.html
 * Or in the file: LICENSE
 * For information/questions contact: gllpellegrino@gmail.com
 */

package RAI;

import RAI.transition_clustering.Transition;
import RAI.transition_clustering.UnclusteredTransition;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// In questa versione sono apportati cambi NON concettuali ma di riorganizzazione del codice.
// Principali cambiamenti:
// 1) Le tre classi di stato sono fuse in un'unica classe per evitare nuove instanziazioni ad ogni promozione. Ora
//      esiste una sola classe STATE con una variabile di campo che assume i tre colori (in una enumeration).
// 2) La promozione è un fatto che si risolve localmente alla classe STATE, e non in HYPOTHESIS.
// 3) Rimozione di tutte le classi interne in HYPOTHESIS, nella fattispecie CANDIDATEMERGE, che diventa classe
//      a tutti gli effetti


public class Hypothesis {


    public Hypothesis(double significance){
        root = new State();
        merges = new PriorityQueue<>();
        redStates = new HashSet<>();
        alpha = significance;
    }

    public Hypothesis(String trainingPath, double significance){
        this(significance);
        try (BufferedReader br = new BufferedReader(new FileReader(trainingPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(" ");
                Future future = Future.parse(values);
                State state = root;
                for (int i = 0; i < values.length; i++){
                    double value = new Double(values[i]);
                    if (state.getOutgoing(value) == null)
                        state.addOutgoing(new UnclusteredTransition(state, new State(), value));
                    state.addFuture(future.getSuffix(i));
                    state = state.getClosestOutgoing(value).getDestination();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Hypothesis(String modelPath){
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
                        states.put(sid, new State(sid));
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
                            states.put(dsid, new State(dsid));
                        current = states.get(ssid);
                        Transition t = new UnclusteredTransition(current, states.get(dsid), lguard, rguard);
                        current.addOutgoing(t);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CANDIDATE MERGES STUFF

    private void registerPair(CandidateMerge pair){
        merges.add(pair);
        pair.getBlueState().addMerge(pair);
    }

    private void unregisterPair(CandidateMerge pair){
        merges.remove(pair);
        pair.getBlueState().removeMerge(pair);
    }

    // MERGING STUFF

    public void merge(State rs, State bs){
        // handling ingoing transitions
        Iterator<Transition> tIterator = bs.getIngoingIterator();
        while (tIterator.hasNext()) {
            Transition t = tIterator.next();
            t.setDestination(rs);
            bs.removeIngoing(t);
            rs.addIngoing(t);
        }
        Iterator<Future> fIterator = bs.getFuturesIterator();
        while (fIterator.hasNext()){
            Future f = fIterator.next();
            fIterator.remove();
            rs.addFuture(f);
        }
        fold(rs, bs);
        bs.dispose();
    }


    private void fold(State rs, State bs) {
        System.out.println("folding " + bs + " in " + rs);
        Iterator<Transition> iterator = bs.getOutgoingIterator();
        while (iterator.hasNext()) {
            // for each outgoing transition of BLUE (bt)
            Transition bt = iterator.next();
            iterator.remove();
            if (rs.isRed() && ! rs.isLeaf()) {
                Transition overlapping = rs.getOutgoing(bt.getMu());
                // nota bene: ocio che overlapping quì può terminare anche
                // in un RED state perché c'è stato il merge prima !!
                overlapping.addAll(bt);
                //handling futures
                State dest = bt.getDestination();
                if (dest != null && ! dest.equals(rs)){
                    Double firstValue = bt.getMu();
                    Iterator<Future> fit = dest.getFuturesIterator();
                    while (fit.hasNext()) {
                        Future f = fit.next();
                        fit.remove();
                        f.addFirst(firstValue);
                        rs.addFuture(f);
                    }
                    // recursive call. Overlapping.getDestination() cannot return null,
                    // we constructed the prefix tree
                    // in order to avoid that
                    fold(overlapping.getDestination(), dest);
                }
            } else {
                // RS IS BLUE, WHITE, OR RED-LEAF
                // DEST CAN BE OF ANY COLOR
                bt.setSource(rs);
                // handling futures
                State dest = bt.getDestination();
                if (dest != null) {
                    Double firstValue = bt.getMu();
                    Iterator<Future> fit = dest.getFuturesIterator();
                    while (fit.hasNext()) {
                        Future f = fit.next();
                        fit.remove();
                        f.addFirst(firstValue);
                        rs.addFuture(f);
                    }
                }
                // attaching
                rs.addOutgoing(bt);
                // possible promotions
                if (rs.isRed()) {
                    rs.cluster();
                    if (dest != null)
                        if (dest.isWhite())
                            promote(dest);
                        else if (dest.isBlue())
                            registerPair(new CandidateMerge(rs, dest));
                }
                else if (dest != null){
                    if (rs.isBlue() && dest.isRed())
                        registerPair(new CandidateMerge(dest, rs));
                    else if (rs.isWhite() && dest.isRed())
                        promote(rs);
                }
            }
        }
    }


    public State promote(State s){
        System.out.println("going to promote " + s);
        if (s.isWhite()){
            s.promote();
            for (State rs : redStates)
                registerPair(new CandidateMerge(rs, s));
            return s;
        }
        if (s.isBlue()){
            s.promote();
            // promote all white sons to blue
            Iterator<Transition> iterator = s.getOutgoingIterator();
            while (iterator.hasNext()){
                Transition t = iterator.next();
                State ns = t.getDestination();
                if (ns.isWhite())
                    ns.promote();
                if (ns.isBlue())
                    registerPair(new CandidateMerge(s, ns));
            }
            // deve stare quà altrimenti si aggiungono due volte le stesse coppie alla coda
            redStates.add(s);
            return s;
        }
        return s;
    }


    //public void minimize(String outPath){
    public void minimize(){
        //toDot(outPath);
        root = promote(promote(root));
        //toDot(outPath + ".root");
        while (! merges.isEmpty()){
            CandidateMerge pair = merges.poll();
            State bs = pair.getBlueState();
            State rs = pair.getRedState();
            if (pair.isCompatible(alpha)) {
                System.out.println("Merging " + pair);
                merge(rs, bs);
                // promoting possible white states connected to rs
                Iterator<Transition> oIterator = rs.getOutgoingIterator();
                while (oIterator.hasNext()){
                    State s = oIterator.next().getDestination();
                    if (s.isWhite())
                        promote(s);
                }
            } else {
                System.out.println("Discarding " + pair);
                unregisterPair(pair);
                if (! bs.hasMerges()) {
                    System.out.println("Promoting " + bs);
                    promote(bs);
                }
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

    // PREDICTION STUFF

    public double predict(Double[] past){
        State current = root;
        for (Double value : past){
            if (current.isLeaf())
                current = root;
            current = current.getClosestOutgoing(value).getDestination();
        }
        return  current.getMu();
    }


    public State state(State state, Double observation){
        if (state.isLeaf())
            return root;
        return state.getOutgoing(observation).getDestination();
    }


    public void exportPredictions(String testPath, String exportPath){
        // used by PADA
        try {
            BufferedReader reader = new BufferedReader(new FileReader(testPath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(exportPath)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.trim().split(" ");
                //System.out.println(Arrays.toString(fields));
                State current = root;
                for (String field : fields){
                    Double ob = Double.parseDouble(field);
                    State next = state(current, ob);
                    //System.out.println(next + " " + next.getMu());
                    writer.write(next.getMu() + "\n");
                    current = next;
                }
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private State root;
    private PriorityQueue<CandidateMerge> merges;
    private Set<State> redStates;
    private double alpha;
    private static final Pattern stateRE = Pattern.compile(
            "^(?<sid>\\d+) \\[shape=(circle|doublecircle), label=\\\"\\d+\\\\n(?<mu>-?\\d*.?\\d+)\\\"\\];$");
    private static final Pattern transRE = Pattern.compile(
            "^\\t(?<ssid>\\d+) -> (?<dsid>\\d+) \\[label=\"(\\[|\\])(?<lguard>(-?\\d*\\.?\\d+)|(-Infinity)), " +
                    "(?<rguard>(-?\\d*.?\\d+)|(Infinity))(\\]|\\[)\\\"\\];$");

    //MAIN ROUTINES

    //unit test
    public static void main(String[] args){
        learnRA();
        //predictWithRA();
    }

    public static void learnRA(){
        String train = "/home/npellegrino/LEMMA/state_merging_regressor/data/suite/3states/3states.sample";
        //double threshold = 0.1145;
        double threshold = 0.0200392800362497;
        String dot = train + ".DOT";
        Hypothesis h = new Hypothesis(train, threshold);
        //h.minimize(train + ".PREFIX.DOT");
        h.minimize();
        h.toDot(dot);
        System.out.println("#states: " + h.redStates.size());
    }

    public static void predictWithRA(){
        //PREDICTION
        String modelpath = "/media/npellegrino/DEDEC851DEC8241D1/CTU-13/datasets/with_background/cleaned/9/pada/EXP.DOT";
        String obspath = "/media/npellegrino/DEDEC851DEC8241D1/CTU-13/datasets/with_background/cleaned/9/pada/147.32.84.191.csv.rai";
        String respath = "/media/npellegrino/DEDEC851DEC8241D1/CTU-13/datasets/with_background/cleaned/9/pada/147.32.84.191.expectation";
        Hypothesis h = new Hypothesis(modelpath);
        h.exportPredictions(obspath, respath);
    }


}
