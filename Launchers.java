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
import RAI.nnstrategy.NNDataBuilder;
import RAI.nnstrategy.NNKData;
import RAI.nnstrategy.NNKDataBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Launchers {


    //MAIN ROUTINES

    //yahoo! data
    public static void learnPrefixTreesForModelSelection(){
        int maxLength = 12;
        int maxFanOut = 12;
        String baseDir = "/home/npellegrino/LEMMA/ydata-labeled-time-series-anomalies-v1_0/A1Benchmark/anomaly_datasets/";
        for (int i = 1; i < 68; i++){
            System.out.println("TS " + i);
            for (int j = 2; j < maxLength + 1; j ++){
                String trnPath = baseDir + i + "/sliding_window/" + j + "/training.slided";
                for (int k = 1; k < maxFanOut + 1; k++) {
                    System.out.println("\tWL = " + j + ", FS = " + k);
                    //String rptPath = baseDir + i + "/sliding_window/" + j + "/rpt.rai";
                    String mselPath = baseDir + i + "/sliding_window/" + j + "/raimodel_" + k + ".dot";
                    //extendedPrefixTree(trnPath, rptPath);
                    NNDataBuilder n = new NNDataBuilder(0.05);
                    Hypothesis<NNData> h = new Hypothesis<>(n, k);
                    //VotingDataBuilder v = new VotingDataBuilder(0.12, 0.2);
                    //Hypothesis<VotingData> h = new Hypothesis<>(v);
                    h.minimize(trnPath);
                    h.toDot(mselPath);
                }
            }
        }
    }

//    public static void learnPrefixTrees(){
//        int maxLength = 20;
//        String baseDir = "/home/npellegrino/LEMMA/ydata-labeled-time-series-anomalies-v1_0/A1Benchmark/anomaly_datasets/";
//        for (int i = 1; i < 68; i++){
//            System.out.println("TS " + i);
//            for (int j = 2; j < maxLength + 1; j ++){
//                System.out.println("\tWL = " + j);
//                // setting paths
//                String trnPath = baseDir + i + "/sliding_window/" + j + "/training.slided";
//                String modelPath = baseDir + i + "/sliding_window/" + j + "/raimodel.dot";
//                // getting alphabet size
//                extendedPrefixTree(trnPath, modelPath);
//            }
//        }
//    }

//    //yahoo! data
//    public static void extendedPrefixTree(String train_path, String model_path){
//        NNDataBuilder n = new NNDataBuilder(0.05);
//        Hypothesis<NNData> h = new Hypothesis<>(n);
//        h.prefixTree(train_path);
//        h.expand().toDot(model_path);
//    }

    // general
    public static void learn(){
        //String problem_id = "2statesV4";
        //String train_path = "/home/npellegrino/LEMMA/state_merging_regressor/data/suite/" + problem_id + "/" + problem_id + ".sample";
        String train_path = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/sinus/sinus10.slided";
        //String train_path = "/home/npellegrino/LEMMA/state_merging_regressor/data/toys/verytoy/verytoy.txt";
        String dot = train_path + ".DOT";
        NNDataBuilder n = new NNDataBuilder(0.05);
        Hypothesis<NNData> h = new Hypothesis<>(n, 2);
        //VotingDataBuilder v = new VotingDataBuilder(0.12, 0.2);
        //Hypothesis<VotingData> h = new Hypothesis<>(v);
        h.minimize(train_path);
        h.toDot(dot);
    }

    private static int[] loadParameters(String path){
        int[] p = new int[67];
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                int val = Integer.parseInt(line.trim().split(" ")[1]);
                p[i] = val;
                i += 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static void suiteLearn() {
        String[] pids = new String[]{"2states", "2statesV2", "2statesV3", "3statesV2", "5statesV2"};
        int[] mats = new int[]{2, 1, 2, 1, 2};
        for (int i = 0; i < pids.length; i ++) {
            System.out.println(pids[i]);
            String train_path = "/home/npellegrino/LEMMA/state_merging_regressor/data/suite/" + pids[i] + "/" + pids[i] + ".sample";
            String dot = train_path + ".DOT";
            NNKDataBuilder n = new NNKDataBuilder(0.05, 1);
            Hypothesis<NNKData> h = new Hypothesis<>(n, mats[i]);
            h.minimize(train_path);
            h.toDot(dot);
        }
    }

    public static void yahooModelsLearner(){
        // the big difference here is that we are gonna learn specific models after parameter estimations.
        String matPath = "/home/npellegrino/PycharmProjects/pada/src/yahoo/mat.txt";
        String problemDir = "/media/npellegrino/DEDEC851DEC8241D/yahoo/A1Benchmark/anomaly_datasets/";
        // we read parameters from two text files.
        int[] mats = loadParameters(matPath);
        // now we can start learning models
        for (int i = 1; i < 68; i ++) {
            if (i != 2 && i != 11 && i != 14 && i != 31 && i != 32) {
                System.out.println("Problem " + i);
                String trainPath = problemDir + i + "/training_logtrend.slided";
                String modelPath = problemDir + i + "/model_logtrend.dot";
                // learning the model
                NNKDataBuilder n = new NNKDataBuilder(0.05, 5);
                Hypothesis<NNKData> h = new Hypothesis<>(n, mats[i - 1]);
                h.minimize(trainPath);
                h.toDot(modelPath);
            }
        }
    }

    public static void yahooSingleModelLearner(int pid){
        System.out.println("Problem " + pid);
        String matPath = "/home/npellegrino/PycharmProjects/pada/src/yahoo/mat.txt";
        String problemDir = "/media/npellegrino/DEDEC851DEC8241D/yahoo/A1Benchmark/anomaly_datasets/";
        int[] mats = loadParameters(matPath);
        String trainPath = problemDir + pid + "/training_logtrend.slided";
        String modelPath = problemDir + pid + "/model_logtrend.dot";
        // learning the model
        NNDataBuilder n = new NNDataBuilder(0.05);
        Hypothesis<NNData> h = new Hypothesis<>(n, mats[pid - 1]);
        h.minimize(trainPath);
        h.toDot(modelPath);
    }

    //unit test
    public static void main(String[] args){
        learn();
    }


}
