package pub.sub.matching;

import java.util.ArrayList;
import java.util.List;

/** Linear is the implementation of "no algorithm" for event matching algorithms. We call it linear as we are linearly searching through each subscription and its predicates.
 *  This assumes that each subscription predicate needs to be true in order for the subscription to be marked as a match, i.e. pred1 && pred2 && pred3.
 *  A more granular approach of linear is implemented in a separate algo which handles any expression. This is called linear-string.
 */
public class MatchingAlgoLinear implements MatchingAlgo {

    List<Subscription> subs = new ArrayList();
    @Override
    public List<Subscription> match(Event event) {
        List<Subscription> matchedSubs = new ArrayList<>();
        for(Subscription sub : subs){
            boolean matched = true;
            for(Predicate currentPred : sub.predicates.values()){
                if(!event.attributeValuePairs.containsKey(currentPred.attribute)){
                    matched = false;
                    break;
                } else if(!(event.attributeValuePairs.get(currentPred.attribute) >= currentPred.lowValue && event.attributeValuePairs.get(currentPred.attribute) <= currentPred.highValue)) {
                    matched = false;
                    break;
                }
            }
            if(matched){
                matchedSubs.add(sub);
            }
        }
        return matchedSubs;
    }

    @Override
    public void insert(Subscription sub) {
        subs.add(sub);
    }
}
