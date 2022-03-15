package pub.sub.matching;

import java.io.*;
import java.util.List;

/** Orchestrates the evaluations of an algo. Provides methods for changing to different types of dynamics of events and subscriptions.
 *  These are set by Config where all its parameter is fixed except one parameter. Examples are runTime2Width where the width is varying.
 *  Depending on EvaluationFunction, it may take a long time with given ranges for runTestSuitWithAlgo.
 *  So smaller ranges and number of subscriptions, event attributes etc can reduce time. **/
public class TestSuite {
    /** The main method of the TestSuite. Runs an evaluation multiple times with different types of dynamics of events and subscriptions.
     *  These are set by the Config where all its parameter is fixed except one parameter that is varied by TestSuite.
     *  An examples is runTime2Width where the width is varied.
     *  The parameter that is changed is varied between startValue and endValue with an given increment.
     *  This will allow for multiple measuring points for the evaluation that can be used to create plots.
     *  The actual evaluation is provided with a lambda-function as an argument.
     **/
    static void runTestSuitWithAlgo(EvaluationFunction evaluationFunction, String algo) {

        System.out.println("Running "+algo + "...");
        runTime2Subs(evaluationFunction, algo, 200, 400, 200);
        /*
        runTime2Subs(evaluationFunction, algo, 5000, 250000, 15000);
        System.out.println("time to sub low done");
        runTime2Subs(evaluationFunction, algo, 250000, 2500000, 250000);
        System.out.println("time to sub high done");
        runTime2EventAttributes(evaluationFunction, algo, 1, 101, 10);
        System.out.println("time to event att done");
        runTime2Width(evaluationFunction, algo, 1, 9, 1);
        System.out.println("time to width done");
        runTime2SubPreds(evaluationFunction, algo, 1, 51, 5);
        System.out.println("time to subpreds done");
         */
    }

    /** Evaluates an algorithm when varying the width. A larger width emulates a less strict predicate.
     *  The number of width is varied between startValue and endValue with a given increment between each measuring point. **/
    private static void runTime2Width(EvaluationFunction evaluationFunction, String algo, int startValue, int endValue, int increment) {
        Config config = Config.configDefaultTest();
        for(int i = startValue; i<=endValue; i += increment){
            config.setWIDTH(i*0.1);
            try {
                evaluationFunction.function(config, algo, "time_width");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Evaluates an algorithm when varying the Event Attributes. An increased number of event attributes emulates "bigger" events.
     *  The number of Event Attributes is varied between startValue and endValue with a given increment between each measuring point. **/
    private static void runTime2EventAttributes(EvaluationFunction evaluationFunction, String algo, int startValue, int endValue, int increment) {
        Config config = Config.configDefaultTest();
        for(int i = startValue; i<=endValue; i += increment){
            config.setNBR_EVENT_ATTRIBUTES(i);
            try {
                evaluationFunction.function(config, algo, "time_event_attribute");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Evaluates an algorithm when varying the Subscription Predicates. More predicates means "longer" subscription criteria, ie pred1 && pred2 && ... .
     *  The number of Subscription Predicates is varied between startValue and endValue with a given increment between each measuring point. **/
    private static void runTime2SubPreds(EvaluationFunction evaluationFunction, String algo, int startValue, int endValue, int increment) {
        Config config = Config.configDefaultTest();
        for(int i = startValue; i<=endValue; i += increment){
            config.setNBR_SUB_PREDICATES(i);
            try {
                evaluationFunction.function(config, algo, "time_sub_predicate");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Evaluates an algorithm when varying the number of Subscriptions. Subscriptions are commonly increased in system and can show different future scenarios.
     *  The number of Subscriptions is varied between startValue and endValue with a given increment between each measuring point. **/
    private static void runTime2Subs(EvaluationFunction evaluationFunction, String algo, int startValue, int endValue, int increment) {
        Config config = Config.configDefaultTest();
        for(int i = startValue; i<=endValue; i += increment){
            config.setNBR_SUBS(i);
            try {
                evaluationFunction.function(config, algo, "time_subs");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Prints output in a csv compatible string.
     * */
    static void printToCSV(String experiment, String algo, Config config, Long[] precomputationTimes, Long[] memoryConsumption, Long[] insertionTimes, Long[] matchingTimes, List<List<Subscription>> matchedSubs, Float[] matchabilities) throws IOException {
        String resultPrecomputationTimes = getCSVStringTimes(experiment, algo, precomputationTimes, config, matchabilities);
        saveToCsv("precomputation_times.csv", resultPrecomputationTimes, config.toHeaderCSVString());

        String resultMemoryConsumption = getCSVStringTimes(experiment, algo, memoryConsumption, config, matchabilities);
        saveToCsv("memory_consumption.csv", resultMemoryConsumption, config.toHeaderCSVString());

        String resultInsertionTimes = getCSVStringTimes(experiment, algo, insertionTimes, config, matchabilities);
        saveToCsv("insertion_times.csv", resultInsertionTimes, config.toHeaderCSVString());

        String resultMatchingTimes = getCSVStringTimes(experiment, algo, matchingTimes, config, matchabilities);
        saveToCsv("matching_times.csv", resultMatchingTimes, config.toHeaderCSVString());
    }

    static String getCSVStringTimes(String experiment, String algo, Long[] times, Config config, Float[] matchabilities) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < times.length; i++){
            Long time = times[i];
            Float matchability = matchabilities != null ? matchabilities[i] : null;
            sb.append(getExperimentString(experiment, algo, time, config, matchability));
            if(i != times.length -1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static String getExperimentString(String experiment, String algo, Long time, Config config, Float matchability) {
        String configString = config.toCSVString();
        return experiment + ";" + algo + ";" + time + ";" + configString + (matchability != null ? ";" + matchability : "");
    }

    public static void saveToCsv(String filename, String csvString, String headerString) throws IOException {
        File file = new File(filename);
        if(file.exists()){
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(csvString);
            pw.flush();
            pw.close();
        }else{
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(headerString);
            pw.println(csvString);
            pw.flush();
            pw.close();
        }

    }
}
