package pub.sub.matching;

import java.util.HashMap;
import java.util.List;

/** Object that represents the Subscription inside the index-structure of an algorithm
 *  May be refactored to separate subscriptions with predicates and subscription with expressionCriteria. **/
public class Subscription {
    Integer id;
    public HashMap<Integer, Predicate> predicates; //The key is the pred attribute
    String expressionCriteria;

    public Subscription(HashMap<Integer, Predicate> predicates, Integer subId){
        this.predicates = predicates;
        this.id = subId;
    }

    public Subscription(String expressionCriteria, Integer subId){
        this.expressionCriteria = expressionCriteria;
        this.id = subId;
    }

    public Subscription(HashMap<Integer, Predicate> predicates){
        this.id = (int) Math.round(Math.random()*10000);
        this.predicates = predicates;
    }
}
