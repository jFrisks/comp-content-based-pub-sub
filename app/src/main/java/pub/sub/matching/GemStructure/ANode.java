package pub.sub.matching.GemStructure;

import pub.sub.matching.MatchingAlgoGemTree;
import pub.sub.matching.Subscription;

import java.util.*;

public class ANode {
    public Map<Integer, VNode> attributeDirectory;
    public VNode parentVNode;
    public List<Subscription> bucket;
    public int currentBucketCapacity;
    HashMap<Integer, Integer> bucketAttributePopularity;

    public ANode(int bucketSize, VNode vNode){
        this.currentBucketCapacity = bucketSize;
        this.bucket = new ArrayList<>();
        this.attributeDirectory = new HashMap<>();
        this.bucketAttributePopularity = new HashMap<>();
        this.parentVNode = vNode;
    }

    /**
     * Takes a sub and inserts it into the bucket.
     * Updates the popularity of the attributes inserted as well.
     * @param sub
     */
    public void storeToBucket(Subscription sub, boolean isRanked) {
        bucket.add(sub);
        //update n if ranked GEMTree
        if(isRanked && parentVNode != null){
            parentVNode.n += 1;
        }

        //update attribute popularity
        for(int predAttribute: sub.predicates.keySet()){
            if(bucketAttributePopularity.containsKey(predAttribute)){
                bucketAttributePopularity.put(predAttribute, bucketAttributePopularity.get(predAttribute) +1);
            } else{
                bucketAttributePopularity.put(predAttribute, 1);
                //update h if ranked GEMTree
                if(isRanked && parentVNode != null){
                    parentVNode.updateH();
                }
            }
        }
    }

    /**
     * Used to remove a sub from the bucket. Called when the bucket is overflowing and a split is made.
     * Also decrements the popularity of the corresponding attributes.
     * @param sub
     */
    public void removeFromBucket(Subscription sub, boolean isRanked) {
        bucket.remove(sub);

        //update n if ranked GEMTree
        if(isRanked && parentVNode != null){
            parentVNode.n -= 1;
        }

        //update attribute popularity
        for (int predAttribute: sub.predicates.keySet()){
            if(bucketAttributePopularity.get(predAttribute) > 1) {
                bucketAttributePopularity.put(predAttribute, bucketAttributePopularity.get(predAttribute) -1);
            }else{
                bucketAttributePopularity.remove(predAttribute);
                //update h if ranked GEMTree
                if(isRanked && parentVNode != null){
                    parentVNode.updateH();
                }
            }
        }
    }

    /**
     * used to increase the capacity of the bucket.
     * @param newBucketSize
     */
    public void setCurrentBucketCapacity(int newBucketSize){
        currentBucketCapacity = newBucketSize;
    }

    /**
     * @return whether the bucket is filled to capacity, or if it can fit more subs.
     */
    public boolean isOverflowing() {
        return bucket.size() >= currentBucketCapacity;
    }

    /**
     *
     * @param root
     * @param vNode
     * @return the most popular attribute by its key or -1 if no key has more uses than the threshold
     * Made the assumption that the attribute can not already be in path for it to be the split attribute
     */
    public int selectMostPopularAttribute(ANode root, VNode vNode, int splitThreshold){
        int mostPopularAttribute = -1;
        for(int key : bucketAttributePopularity.keySet()){
            if(bucketAttributePopularity.get(key) > mostPopularAttribute && bucketAttributePopularity.get(key) > splitThreshold && !MatchingAlgoGemTree.inPath(root, this, vNode, key)){
                mostPopularAttribute = key;
            }
        }
        return mostPopularAttribute;
    }
}