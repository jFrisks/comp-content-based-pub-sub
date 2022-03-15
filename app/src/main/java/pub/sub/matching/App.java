/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package pub.sub.matching;

import java.io.*;
import java.util.*;

public class App {
    static Result matchingResult;
    static Result insertionResult;

    /** The main method that starts all evaluations with provided algorithms and with given testSuite.
     *  Possible algorithms in list are shown in `Generator.java`
     **/
    public static void main(String[] args) {
        System.out.println("Starting evaluation...");
        ArrayList<String> algos = new ArrayList(List.of("linear", "linear-string", "maema", "gem", "avddm"));
        //ArrayList<String> algos = new ArrayList(List.of("linear-string", "linear", "gem")); //two versions of linear vs the best algo for defautl config
        //ArrayList<String> algos = new ArrayList(List.of("linear"));
        for(String algo : algos){
            TestSuite.runTestSuitWithAlgo(App::isolatedEvaluation, algo);
        }
    }

    /** An evaluation that uses a specified configuration and algo, evaluates and prints the result as a csv
     **/
    static void isolatedEvaluation(Config config, String algo, String experiment) {
        matchingResult = isolatedEvaluationOfMatching(config, algo);
        insertionResult = isolatedEvaluationOfInsertion(config, algo);
        //If a result should be exluded - switch to an empty result below
        //matchingResult = new Result(new Long[0], new Long[0], new Long[0], new Long[0], new ArrayList<>(), null);
        //insertionResult = new Result(new Long[0], new Long[0], new Long[0], null, null, null);
        /* Output results */
        try {
            TestSuite.printToCSV(experiment, algo, config, insertionResult.precomputationTimes, insertionResult.memoryConsumption, insertionResult.insertionTimes, matchingResult.matchingTimes, matchingResult.matchedSubs, matchingResult.matchabilities);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Result isolatedEvaluationOfInsertion(Config config, String algo){
        int totalTestRuns = config.NBR_EVENTS;

        /* SETUP FOR MEASUREMENTS */
        Long[] insertionTimes = new Long[totalTestRuns];
        Long[] precomputationTimes = new Long[totalTestRuns];
        Long[] memoryConsumption = new Long[totalTestRuns];

        MatchingAlgo matcher;
        /* PERFORM INSERTION TEST (NBR_EVENTS) nbr of time  */
        for(int test_nbr = 0; test_nbr < totalTestRuns; test_nbr ++) {
            //Measure memory consumption before full insertion and generation
            Runtime rt = Runtime.getRuntime();
            rt.gc();
            long memoryBeforeTotal = rt.totalMemory();
            long memoryBefore = rt.freeMemory();
            long memoryConsumptionBefore = memoryBeforeTotal - memoryBefore;


            /* GENERATE DATA */
            Generator generator = new Generator(); //new Generator(config.RANDOM_SEED); //<-With Seed
            Subscription[] subs = generator.generateSubs(config.NBR_SUBS, config.NBR_TOTAL_ATTRIBUTES, config.NBR_SUB_PREDICATES, config.VAL_DOM, config.WIDTH, algo);
            matcher = Generator.createMatchingAlgo(config, algo);

            //INSERT
            //Measure precomputation
            long startTime = System.nanoTime();
            for (int sub_ind = 0; sub_ind < config.NBR_SUBS - 1; sub_ind++) {
                matcher.insert(subs[sub_ind]);
            }
            //Measure Insertion times
            long endTimeBeforeLast = System.nanoTime();
            matcher.insert(subs[config.NBR_SUBS - 1]);
            long endTimeAfterLast = System.nanoTime();

            //measure memory consumption after insertion
            long memoryAfterTotal = rt.totalMemory();
            long memoryAfter = rt.freeMemory();
            long memoryConsumptionAfter = memoryAfterTotal - memoryAfter;

            //Add results
            memoryConsumption[test_nbr] = (memoryConsumptionAfter - memoryConsumptionBefore);
            precomputationTimes[test_nbr] = (endTimeBeforeLast - startTime);
            insertionTimes[test_nbr] = (endTimeAfterLast - endTimeBeforeLast);
        }
        System.out.println("runInsertionWithAlgo done");

        return new Result(precomputationTimes, memoryConsumption, insertionTimes, null, null, null);
    }

    static Result isolatedEvaluationOfMatching(Config config, String algo) {
        /* SETUP FOR MEASUREMENTS */
        Long[] matchingTimes = new Long[config.NBR_EVENTS];
        Float[] eventMatchabilities = new Float[config.NBR_EVENTS];
        List<List<Subscription>> matchedSubs = new ArrayList<>();

        /* GENERATE DATA */
        Generator generator = new Generator(config.RANDOM_SEED); //A seed is used to keep events the same while subs will change with different configs. Thus more controlled.
        Event[] events = generator.generateEvents(config.NBR_EVENTS, config.NBR_TOTAL_ATTRIBUTES, config.NBR_EVENT_ATTRIBUTES, config.VAL_DOM);
        Subscription[] subs = generator.generateSubs(config.NBR_SUBS, config.NBR_TOTAL_ATTRIBUTES, config.NBR_SUB_PREDICATES, config.VAL_DOM, config.WIDTH, algo);

        /* PERFORM TEST */
        MatchingAlgo matcher = Generator.createMatchingAlgo(config, algo);

        //INSERT - measure precomputation time for all except last which will measure insertion time
        for(int i = 0; i < config.NBR_SUBS; i++){
            matcher.insert(subs[i]);
        }
        System.out.println("Insertion done");

        //Matching
        for(int i = 0; i < config.NBR_EVENTS; i++){
            long startTime = System.nanoTime();
            List<Subscription> matched = matcher.match(events[i]);
            long endTime = System.nanoTime();
            matchingTimes[i] = (endTime - startTime);
            matchedSubs.add(matched);
            float eventMatchability = matched.size() /(float) subs.length;
            eventMatchabilities[i] = eventMatchability;
        }
        System.out.println("Matching done");

        return new Result(null, null,null, matchingTimes, matchedSubs, eventMatchabilities);
    }

}