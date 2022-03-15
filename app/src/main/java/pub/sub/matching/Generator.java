package pub.sub.matching;

import java.util.*;

/** A generator for generating event matching algo as well as uniformly distributed events and subscriptions
 */
public class Generator {
    Random random;

    public Generator(long randomSeed) {
        this.random = new Random(randomSeed);
    }

    public Generator() {
        this.random = new Random(System.currentTimeMillis());
    }

    /**
     * Returns an algo based on the provided configuration and choice of algo
     **/
    static MatchingAlgo createMatchingAlgo(Config config, String algo) {
        switch (algo) {
            case "linear":
                return new MatchingAlgoLinear();
            case "linear-string":
                return new MatchingAlgoLinearString();
            case "maema":
                return new MatchingAlgoMaema(config.NBR_TOTAL_ATTRIBUTES, config.MAEMA_MAX_NUMBER_BUCKETS, config.NBR_SUBS, config.VAL_DOM, config.WIDTH, config.NBR_SUB_PREDICATES);
            case "gemUnRanked":
                return new MatchingAlgoGemTree(config.NBR_SUBS, config.NBR_TOTAL_ATTRIBUTES, config.NBR_SUB_PREDICATES, config.VAL_DOM, config.GEMTree_NBR_CELLS, config.GEMTree_SPLIT_THRESHOLD, config.GEMTree_INCREASE_BUCKET_SIZE_FACTOR, config.GEMTree_ALPHA, false);
            case "gem":
                return new MatchingAlgoGemTree(config.NBR_SUBS, config.NBR_TOTAL_ATTRIBUTES, config.NBR_SUB_PREDICATES, config.VAL_DOM, config.GEMTree_NBR_CELLS, config.GEMTree_SPLIT_THRESHOLD, config.GEMTree_INCREASE_BUCKET_SIZE_FACTOR, config.GEMTree_ALPHA, true);
            case "avddm":
                return new MatchingAlgoAVDDM(config.NBR_SUBS, config.VAL_DOM);
            default:
                return null;
        }
    }

    /**
     * Creates an array of event for the given number of events where all events have the same number of attributes and each attribute within an event is unique for that event.
     */
    public Event[] generateEvents(int nbrEvents, int nbrTotalAttributes, int nbrEventAttributes, int valDom) {
        Event[] events = new Event[nbrEvents];
        for (int i = 0; i < nbrEvents; i++) {
            Event event = new Event();
            Set<Integer> usedAttributes = new HashSet<>();
            for (int j = 0; j < nbrEventAttributes; j++) {
                int attributeToAdd = getAttributeToAdd(nbrTotalAttributes, random, usedAttributes);
                event.add(attributeToAdd, random.nextInt(valDom));
            }
            events[i] = event;
        }
        return events;
    }

    /**
     * Returns a random attribute that does not exist in the provided set of used attributes
     **/
    private int getAttributeToAdd(int nbrTotalAttributes, Random random, Set<Integer> usedAttributes) {
        int attributeToAdd = random.nextInt(nbrTotalAttributes);
        while (usedAttributes.contains(attributeToAdd)) {
            attributeToAdd = random.nextInt(nbrTotalAttributes);
        }
        usedAttributes.add(attributeToAdd);
        return attributeToAdd;
    }

    /**
     * For all subscription generate a list of predicates, where all preds have a unique attribute with a random lowvalue and high value to match according to the desired width.
     */
    public Subscription[] generateSubs(int nbrSubs, int nbrTotalAttributes, int nbrSubPredicates, int valDom, double width, String algo) {
        //If a expression string subscription
        boolean isExpressionSubscription = false;
        if (algo.contains("string")){
            return generateSubsWithStringExpression(nbrSubs, nbrTotalAttributes, nbrSubPredicates, valDom, width);
        }else {
            return generateSubscriptionsWithPredicates(nbrSubs, nbrTotalAttributes, nbrSubPredicates, valDom, width);
        }
    }

    private Subscription[] generateSubscriptionsWithPredicates(int nbrSubs, int nbrTotalAttributes, int nbrSubPredicates, int valDom, double width) {
        Subscription[] subs = new Subscription[nbrSubs];
        for (int i = 0; i < nbrSubs; i++) {
            HashMap<Integer, Predicate> predicates = new HashMap<>();
            Set<Integer> usedAttributes = new HashSet<>();
            for (int j = 0; j < nbrSubPredicates; j++) {
                int attributeToAdd = getAttributeToAdd(nbrTotalAttributes, random, usedAttributes);
                int lowValue = random.nextInt((int) (valDom * (1.0 - width)));
                int highValue = lowValue + (int) (valDom * width);
                //Generate either predciate subs or string subscriptions
                predicates.put(attributeToAdd, new Predicate(attributeToAdd, lowValue, highValue));
            }
            subs[i] = new Subscription(predicates, i);
        }
        return subs;
    }

    // add && if second+ pred before the new pred is created
    //Expression: (attr1 > 1 && attr1 < 10) && ()
    public Subscription[] generateSubsWithStringExpression(int nbrSubs, int nbrTotalAttributes, int nbrSubPredicates, int valDom, double width) {
        Subscription[] subs = new Subscription[nbrSubs];
        for (int i = 0; i < nbrSubs; i++) {
            StringBuilder sb = new StringBuilder();
            Set<Integer> usedAttributes = new HashSet<>();
            for (int j = 0; j < nbrSubPredicates; j++) {
                int attributeToAdd = getAttributeToAdd(nbrTotalAttributes, random, usedAttributes);
                int lowValue = random.nextInt((int) (valDom * (1.0 - width)));
                int highValue = lowValue + (int) (valDom * width);
                if(j>=1){
                    sb.append(" && ");
                }
                //"event.attributeValuePairs[1] > 5000 && event.attributeValuePairs[6] < 5000";
                sb.append(String.format("(event.attributeValuePairs[%d] > %d && event.attributeValuePairs[%d] < %d)", attributeToAdd, lowValue, attributeToAdd, highValue));
                //Generate either predciate subs or string subscriptions
                //predicates.put(attributeToAdd, new Predicate(attributeToAdd, lowValue, highValue));
            }
            subs[i] = new Subscription(sb.toString(), i);
        }
        return subs;
    }
}
