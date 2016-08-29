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


//In questa versione è stato rivisto il folding perché c'è un problema di overlap tra le transizioni dopo il folding.
//Sicco suggerisce di non cambiare mai le transizioni che escono da un RED state, ma il punto è che NON BISOGNA
//RIDISCUTERE le decisioni prese (red states).
//
//Un altro punto di innovazione riguarda il clustering delle transizioni. Viene modificato per EVITARE TRANSIZIONI
//SINGLETON (con un solo valore).


public class Hypothesis {


    public class CandidateMerge implements Comparable<CandidateMerge>{


        private CandidateMerge(RedState rs, BlueState bs){
            redState = rs;
            blueState = bs;
            computeScore(rs, bs);
            merges.add(this);
            bs.addMerge(this);
        }

        // SCORING STUFF

        public double getScore(){
            return score;
        }

        private void computeScore(State rs, State bs){
            //System.out.println("considering merge between " + rs + " and " + bs);
            score = 0.;
            int n = 0;
            Iterator<Future> blueFutures = bs.getFuturesIterator();
            while (blueFutures.hasNext()){
                Future blueFuture = blueFutures.next();
                Future redFuture = rs.getClosestFuture(blueFuture);
                score += redFuture.getAvgPrefixEuclideanScore(blueFuture);
                //System.out.println("\t" + blueFuture + " " + redFuture + " " + redFuture.getAvgPrefixEuclideanScore(blueFuture));
                n += 1;
            }
            Iterator<Future> redFutures = rs.getFuturesIterator();
            while (redFutures.hasNext()){
                Future redFuture = redFutures.next();
                Future blueFuture = bs.getClosestFuture(redFuture);
                score += blueFuture.getAvgPrefixEuclideanScore(redFuture);
                n += 1;
            }
            if (n == 0)
                score = Double.POSITIVE_INFINITY;
            score /= (double) n;
        }

        public boolean isCompatible(double alpha) {
            return redState.isLeaf() && blueState.isLeaf() || score < alpha;
        }

        //UTILITY

        @Override
        public int compareTo(CandidateMerge o){
            if (score != o.getScore())
                return (score > o.getScore())?(1):(-1);
            return 0;
        }

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
            return "{" + redState.getId() + ":" + blueState.getId() + ":" + score + "}";
        }

        public void dispose(){
            merges.remove(this);
            blueState.removeMerge(this);
        }



        private RedState redState;
        private BlueState blueState;
        private double score;


    }


    public Hypothesis(double significance){
        root = new WhiteState();
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
                        state.addOutgoing(new UnclusteredTransition(state, new WhiteState(), value));
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
            Map<Integer, WhiteState> states = new HashMap<>();
            WhiteState current;
            while ((line = br.readLine()) != null) {
                Matcher lMatcher = stateRE.matcher(line);
                if (lMatcher.matches()){
                    //state id and mu
                    Integer sid = Integer.parseInt(lMatcher.group("sid"));
                    Double mu = Double.parseDouble(lMatcher.group("mu"));
                    if (! states.containsKey(sid))
                       states.put(sid, new WhiteState(sid));
                    current = states.get(sid);
                    if (current.getMu() == null)
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
                        if (! states.containsKey(dsid))
                            states.put(dsid, new WhiteState(dsid));
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
        // handling futures
        Iterator<Future> fIterator = bs.getFuturesIterator();
        while (fIterator.hasNext()){
            Future f = fIterator.next();
            fIterator.remove();
            rs.addFuture(f);
        }
        fold(rs, bs);
        bs.dispose();
    }


//    private void fold(State rs, State bs) {
//        // NOTA BENE:
//        // cambia la politica del fold: prima di tutto se sono un red state non posso updatarmi localmente
//        // perché le mie transizioni sono frozen, non le voglio più ridiscutere.
//        // Poi io non stacco la transizione per riattaccarla, piùttosto la copio. Serve per evitare problemi
//        // sulle chiamate ricorsive qualora ne abbia multiple (non dovrebbe succedere mai se costruisco un prefix
//        // tree destrutturato).
//        System.out.println("folding " + bs + " in " + rs);
//        Iterator<Transition> iterator = bs.getOutgoingIterator();
//        while (iterator.hasNext()) {
//            // for each outgoing transition of BLUE (bt)
//            Transition bt = iterator.next();
//            // get all transitions of RED that overlap with bt
//            Multiset<Transition> overlapping = rs.getOutgoing(bt);
//            if (overlapping.isEmpty()) {
//                // if no transitions overlap, there isn't any nondeterminism
//                // attach the transition to RED and remove it from BLUE
//                //
//                // nota che nell'ipotesi di transizioni uscenti totalizzanti (che coprono tutto l'alfabeto)
//                // questo branch è seguito solo se rs è NON RED.
//                iterator.remove();
//                //Transition newBt = bt.clone();
//                bt.setSource(rs);
//                //handling futures
//                State dest = bt.getDestination();
//                if (dest != null){
//                    Double firstValue = bt.getMu();
//                    Iterator<Future> fit = dest.getFuturesIterator();
//                    while (fit.hasNext()){
//                        Future f = fit.next();
//                        fit.remove();
//                        f.addFirst(firstValue);
//                        rs.addFuture(f);
//                    }
//                }
//                //attaching
//                rs.addOutgoing(bt);
//            } else {
//                // we have nondeterminism to solve
//                iterator.remove();
//                State nbs = bt.getDestination();
//                for (Transition rt : overlapping) {
//                    State nrs = rt.getDestination();
//                    fold(nrs, nbs);
//                }
//            }
//        }
//    }

    private void fold(State rs, State bs) {
        // NOTA BENE:
        // cambia la politica del fold.
        // Se rs è NON red allora aggiungo a prescindere se ci sia sovrapposizione tra transizioni.
        // Se rs è red, allora ci sarà per forza sovrapposizione (rs ha transizioni che coprono tutto il dominio). In
        // questo caso si aggiungono i valori della transizione di bs alla corrispettiva di rs, e si aggiungono le
        // le transizioni uscenti dalla destinazione (bianca) della transizione uscente da bs.
        System.out.println("folding " + bs + " in " + rs);
        Iterator<Transition> iterator = bs.getOutgoingIterator();
        while (iterator.hasNext()) {
            // for each outgoing transition of BLUE (bt)
            Transition bt = iterator.next();
            iterator.remove();
            if (rs instanceof RedState){
                Transition overlapping = rs.getOutgoing(bt.getMu());
                // nota bene: overlapping non può essere NULL.
                // nota bene: ocio che overlapping quì può terminare anche
                // in un RED state perché c'è stato il merge prima !!
                overlapping.addAll(bt);
                //handling futures
                State dest = bt.getDestination();
                if (dest != null){
                    Double firstValue = bt.getMu();
                    Iterator<Future> fit = dest.getFuturesIterator();
                    while (fit.hasNext()){
                        Future f = fit.next();
                        fit.remove();
                        f.addFirst(firstValue);
                        rs.addFuture(f);
                    }
                }
                fold(overlapping.getDestination(), dest);
            }else{
                bt.setSource(rs);
                //handling futures
                State dest = bt.getDestination();
                if (dest != null){
                    Double firstValue = bt.getMu();
                    Iterator<Future> fit = dest.getFuturesIterator();
                    while (fit.hasNext()){
                        Future f = fit.next();
                        fit.remove();
                        f.addFirst(firstValue);
                        rs.addFuture(f);
                    }
                }
                //attaching
                rs.addOutgoing(bt);
            }
        }
    }


    public State promote(State s){
        System.out.println("going to promote " + s);
        if (s instanceof WhiteState){
            BlueState bs = ((WhiteState) s).promote();
            for (RedState rs : redStates)
                new CandidateMerge(rs, bs);
            return bs;
        }
        if (s instanceof BlueState){
            RedState rs = ((BlueState) s).promote(alpha);
            // promote all white sons to blue
            Iterator<Transition> iterator = rs.getOutgoingIterator();
            while (iterator.hasNext()){
                Transition t = iterator.next();
                State ns = t.getDestination();
                if (ns instanceof WhiteState)
                    //BlueState bs = (BlueState) promote(ns);
                    ns = promote(ns);
                if (ns instanceof BlueState)
                    new CandidateMerge(rs, (BlueState) ns);
            }
            // deve stare quà altrimenti si aggiungono due volte le stesse coppie alla coda
            redStates.add(rs);
            return rs;
        }
        return s;
    }


    public void minimize(String outPath){
        toDot(outPath + ".prefix1.dot");
        root = promote(promote(root));
        toDot(outPath + ".prefix2.dot");
        while (! merges.isEmpty()){
            CandidateMerge pair = merges.poll();
            BlueState bs = pair.blueState;
            if (pair.isCompatible(alpha)) {
                System.out.println("Merging " + pair);
                merge(pair.redState, bs);
                // promoting possible white states connected to rs
                Iterator<Transition> oIterator = pair.redState.getOutgoingIterator();
                while (oIterator.hasNext()){
                    State s = oIterator.next().getDestination();
                    if (s instanceof WhiteState)
                        promote(s);
                }
            } else {
                System.out.println("Discarding " + pair);
                pair.dispose();
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

    public void exportPredictions(String testPath, String exportPath){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(testPath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(exportPath)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.trim().split(" ");
                Double[] past = new Double[fields.length - 1];
                int i = 0;
                for (String field : fields){
                    if (i >= fields.length - 1)
                        break;
                    past[i] = Double.parseDouble(field.split("/")[1]);
                    if (i == 0)
                        writer.write(field);
                    else writer.write(" " + field);
                    i += 1;
                }
                writer.write(" " + fields[fields.length - 1].split("/")[0] + "/" + predict(past) + "\n");
            }
            writer.close();
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private State root;
    private PriorityQueue<CandidateMerge> merges;
    private Set<RedState> redStates;
    private double alpha;
    private static final Pattern stateRE = Pattern.compile(
            "^(?<sid>\\d+) \\[shape=(circle|doublecircle), label=\\\"\\d+\\\\n(?<mu>-?\\d*.?\\d+)\\\"\\];$");
    private static final Pattern transRE = Pattern.compile(
            "^\\t(?<ssid>\\d+) -> (?<dsid>\\d+) \\[label=\"\\[(?<lguard>-?\\d*\\.?\\d+) - (?<rguard>-?\\d*.?\\d+)\\]\\\"\\];$");

    //MAIN ROUTINES

    //unit test
    public static void main(String[] args){
        //windowLength();
        generalLearning();
    }

    public static void generalLearning(){
        //String path = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/verytoy/verytoy.txt";
        //String path = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/sinus/sinus.lev3.txt";
        //String path = "/home/npellegrino/LEMMA/state_merging_regressor/data/windspeed/1hour/train/autumn.train";
        String path = "/home/npellegrino/LEMMA/state_merging_regressor/data/windspeed/1hour/train/qin.train";
        Hypothesis h = new Hypothesis(path, .3); // .2 for qin, 5. for verytoy
        System.out.println("start minimizing");
        h.minimize(path + ".minimized.dot");
        h.toDot(path + ".minimized.dot");
        System.out.println("red states: " + h.redStates.size());

//        //PREDICTION
//        String obspath = "/home/npellegrino/LEMMA/state_merging_regressor/data/windspeed/1hour/test/qin.test";
//        String respath = "/home/npellegrino/LEMMA/state_merging_regressor/data/windspeed/1hour/test/qin_mie.test";
//        h.exportPredictions(obspath, respath);

        //LOADING FROM DOT
//        String idot = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/toy.dot";
//        String odot = idot + ".canc";
//        Hypothesis h = new Hypothesis(idot);
//        System.out.println(h.root);
//        h.toDot(odot);
    }

    public static void windowLength(){
        Hypothesis h;
        String dir = "/home/npellegrino/LEMMA/state_merging_regressor/data/windspeed/window_length/";
        String[] seasons = new String[]{"autumn"};
        for (int i = 2; i <= 10; i++){
            for (String season : seasons) {
                System.out.println("generating predictions for season " + season + ", window " + i + " symbols long.");
                String trPath = dir + season + i + ".train";
                String tsPath = dir + season + i + ".test";
                String tstrPath = dir + season + i + ".testrain";
                String trPredictionsPath = dir + season + i + ".tr_predictions";
                String tsPredictionsPath = dir + season + i + ".ts_predictions";
                h = new Hypothesis(trPath, .02);
                h.exportPredictions(tsPath, tsPredictionsPath);
                h.exportPredictions(tstrPath, trPredictionsPath);
            }
        }
    }



}
