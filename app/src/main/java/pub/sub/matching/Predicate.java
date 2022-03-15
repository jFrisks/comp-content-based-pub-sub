package pub.sub.matching;

/** Predicates inside a subscription.
  * Is a constraint as a range of low and high value range.
 *  Up to usage if inclusive or exclusive, but does not */
public class Predicate {
    public int attribute;
    public int lowValue;
    public int highValue;

    public Predicate(int attribute, int lowValue, int highValue){
        this.attribute = attribute;
        this.lowValue = lowValue;
        this.highValue = highValue;
    }
}
