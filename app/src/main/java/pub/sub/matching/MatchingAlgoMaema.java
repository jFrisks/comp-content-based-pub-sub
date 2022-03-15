package pub.sub.matching;

import pub.sub.matching.MaemaStructure.Bucket;
import pub.sub.matching.MaemaStructure.ValIdPair;
import pub.sub.matching.MaemaStructure.BucketType;

import java.util.ArrayList;
import java.util.List;

/** The event matching algorithm named MAEMA
 * Based on the paper: "Adjusting Matching Algorithm to Adapt to Dynamic Subscriptions in Content-Based Publish/Subscribe Systems" (can be found in google scholar)
 * Link: https://ieeexplore.ieee.org/document/8672310
 * With help from c-code implementation in repo: https://github.com/xizeroplus/matching-algorithm
 */
public class MatchingAlgoMaema implements MatchingAlgo {
    List<Subscription> subs;
    private Bucket[][][] bucketlist;
    private boolean[] bitSet;
    int valDom;
    double width;
    int nbrPredicatesSubs;
    int bucketStep;
    int maxNbrBuckets;
    int nbrBuckets; //in c-code: bucks
    int y = Integer.MAX_VALUE/2;


    public MatchingAlgoMaema(int nbrTotalAttributes, int maxNbrBuckets, int nbrSubscribers, int valDom, double width, int nbrSubPredicates){
        this.subs = new ArrayList<>();
        this.bucketlist = new Bucket[nbrTotalAttributes][2][maxNbrBuckets];
        for(int i = 0; i < nbrTotalAttributes; i++){
            for (int j = 0; j < 2; j++){
                for(int k = 0; k < maxNbrBuckets; k++){
                    this.bucketlist[i][j][k] = new Bucket();
                }
            }
        }
        this.bitSet = new boolean[nbrSubscribers];
        this.valDom = valDom;
        this.width = width;
        this.nbrPredicatesSubs = nbrSubPredicates;
        this.maxNbrBuckets = maxNbrBuckets;
        this.bucketStep = (this.valDom - 1) / maxNbrBuckets + 1;
        this.nbrBuckets = (this.valDom - 1) / this.bucketStep + 1;
        this.y = (int) (nbrBuckets * (1 - Math.pow(((1 - width) / (1 - Math.pow(width, nbrPredicatesSubs))), (1.0 / (nbrPredicatesSubs - 1))) ));
    }

    /** Match an event to subscriptions. Using y to know how many neighbouring buckets to traverse
     */
    public List<Subscription> match(Event event, int y) {
        List<Subscription> matchedSubs = new ArrayList<>();
        this.bitSet = new boolean[this.bitSet.length];

        for(int attribute : event.attributeValuePairs.keySet()){
            int value = event.attributeValuePairs.get(attribute);
            int bucket = value / bucketStep;
            int upper = Math.min(nbrBuckets, bucket + y);
            int lower = Math.max(0, bucket - y);

            //Go through lower anchor-bucket
            for(int k = 0; k < bucketlist[attribute][BucketType.lowValue][bucket].bucket.size(); k++) {
                if(bucketlist[attribute][BucketType.lowValue][bucket].bucket.get(k).val > value)
                    bitSet[bucketlist[attribute][BucketType.lowValue][bucket].bucket.get(k).subId] = true;
            }
            //Go through lower neighbouring bucket to anchor-bucket
            for (int j = bucket + 1; j < upper; j++){
                for (int k = 0; k < bucketlist[attribute][BucketType.lowValue][j].bucket.size(); k++)
                    bitSet[bucketlist[attribute][BucketType.lowValue][j].bucket.get(k).subId] = true;
            }

            //Go through upper anchor-bucket
            for(int k = 0; k < bucketlist[attribute][BucketType.highValue][bucket].bucket.size(); k++) {
                if(bucketlist[attribute][BucketType.highValue][bucket].bucket.get(k).val < value)
                    bitSet[bucketlist[attribute][BucketType.highValue][bucket].bucket.get(k).subId] = true;
            }
            //Go through upper neighbouring bucket to anchor-bucket
            for (int j = bucket - 1; j >= lower; j--){
                for (int k = 0; k < bucketlist[attribute][BucketType.highValue][j].bucket.size(); k++)
                    bitSet[bucketlist[attribute][BucketType.highValue][j].bucket.get(k).subId] = true;
            }
        }

        /* DEBUG */
        //debugNbrFalseInBitset();

        //DOUBLE CHECK
        for(Subscription sub : subs){
            if(!bitSet[sub.id]){
                boolean isMatched = true;
                for(Predicate pred : sub.predicates.values()){
                    if(!event.attributeValuePairs.containsKey(pred.attribute)) {
                        isMatched = false;
                        break;
                    }
                    int eventValue = event.attributeValuePairs.get(pred.attribute);
                    if((eventValue < pred.lowValue || eventValue > pred.highValue)){
                        isMatched = false;
                        break;
                    }
                }
                if(isMatched){
                    matchedSubs.add(sub);
                }
            }
        }
        return matchedSubs;
    }

    @Override
    public List<Subscription> match(Event event) {
        return match(event, y);
    }

    @Override
    public void insert(Subscription sub) {
        this.subs.add(sub);
        for(Predicate pred : sub.predicates.values()){
            ValIdPair newPairLow = new ValIdPair(sub.id, pred.lowValue);
            Bucket bucketLow = bucketlist[pred.attribute][BucketType.lowValue][pred.lowValue / bucketStep];
            bucketLow.bucket.add(newPairLow);
            ValIdPair newPairHigh= new ValIdPair(sub.id, pred.highValue);
            Bucket bucketHigh = bucketlist[pred.attribute][BucketType.highValue][pred.highValue / bucketStep];
            bucketHigh.bucket.add(newPairHigh);
        }
    }

    /** DEBUG HELPER */
    private void debugNbrFalseInBitset() {
        int nbrFalse = 0;
        for(boolean bool : bitSet){
            if(!bool)
                nbrFalse += 1;
        }
        System.out.println("Nbr false: " + nbrFalse);
    }
}
