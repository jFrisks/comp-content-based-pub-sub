package pub.sub.matching;

import java.util.List;

/** Object that keeps the evaluation results in one place.
 */
public class Result {
    Long[] insertionTimes;
    Long[] precomputationTimes;
    Long[] memoryConsumption;
    Long[] matchingTimes;
    List<List<Subscription>> matchedSubs;
    Float[] matchabilities;

    public Result(Long[] precomputationTimes, Long[] memoryConsumption, Long[] insertionTimes, Long[] matchingTimes, List<List<Subscription>> matchedSubs, Float[] matchabilities) {
        this.insertionTimes = insertionTimes;
        this.matchingTimes = matchingTimes;
        this.matchedSubs = matchedSubs;
        this.precomputationTimes = precomputationTimes;
        this.memoryConsumption = memoryConsumption;
        this.matchabilities = matchabilities;
    }
}
