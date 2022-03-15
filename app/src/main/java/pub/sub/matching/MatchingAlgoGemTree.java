package pub.sub.matching;

import pub.sub.matching.GemStructure.ANode;
import pub.sub.matching.GemStructure.VNode;

import java.util.*;

/** The event matching algorithm named GEM-Tree
 * Based on the paper: "GEM-Tree: Tree-Based Analytic Geometrical Multi-Dimensional Content-Based Event Matching" (can be found in google scholar)
 * Link: https://www.researchgate.net/publication/337204006_GEM-Tree_Tree-Based_Analytic_Geometrical_Multi-Dimensional_Content-Based_Event_Matching
 * A GEM-tree consists of ANodes with attribute directories and buckets. Buckets store full subscriptions.
 * The att dir in turns contains VNodes that has triangle structures, which contains new ANodes.
 */
public class MatchingAlgoGemTree implements MatchingAlgo {
    int nbrSubscribers;
    int nbrTotalAttributes;
    int nbrSubPredicates;
    int nbrCells;
    int valDom;
    ANode root;
    int initialBucketSize;
    int splitThreshold;
    double increaseBucketSizeFactor;
    double alpha;
    boolean isRanked;

    public MatchingAlgoGemTree(int nbrSubscribers, int nbrTotalAttributes, int nbrSubPredicates, int valDom , int nbrCells, int splitThreshold, double increaseBucketSizeFactor, double alpha, boolean isRanked){
        this.nbrSubscribers = nbrSubscribers;
        this.nbrTotalAttributes = nbrTotalAttributes;
        this.nbrSubPredicates = nbrSubPredicates;
        this.valDom = valDom;
        this.nbrCells = nbrCells;
        this.initialBucketSize = (nbrCells * nbrCells)/2 + (nbrCells /2); //Assumed value
        this.root = new ANode(initialBucketSize, null);
        this.splitThreshold = splitThreshold;
        this.increaseBucketSizeFactor = increaseBucketSizeFactor;
        this.alpha = alpha;
        this.isRanked = isRanked;
    }

    /**
     * main match method, used to call GEMTree's match method
     * @param event
     * @return the list of matched subscription for the given event
     */
    @Override
    public List<Subscription> match(Event event) {
        List<Subscription> matchedSubs = new ArrayList<>();
        Set<Integer> intersectionAttribute = new HashSet<>();
        match(event, root, null, matchedSubs, intersectionAttribute);
        return matchedSubs;
    }

    /**
     * When calling the match method, an anode is the current Node while the vNode is the parent of the anode.
     * @param event
     * @param aNode
     * @param vNode
     * @param matchedSubs
     * @param intersectionAttribute
     * @return the list of matched subscriptions
     */
    public List<Subscription> match(Event event, ANode aNode, VNode vNode, List<Subscription> matchedSubs, Set<Integer> intersectionAttribute){
        ANode aNodeToCheck;
        checkANode(event, aNode, vNode, matchedSubs, intersectionAttribute);
        for(int eventAttribute : event.attributeValuePairs.keySet()){
            if(aNode.attributeDirectory.size() == 0){
                break; //break right away if anode attribute directory is empty
            }
            VNode vNodeToCheck = getVNodeFromAttribute(eventAttribute, aNode);
            if (vNodeToCheck != null){
                int index = location(event.attributeValuePairs.get(eventAttribute));
                if(index != 0) { //no need to continue if the inner loop is tied to index = 0
                    for (int row = index + 1; row < nbrCells; row++) {
                        for (int col = 0; col < index; col++) {
                            aNodeToCheck = vNodeToCheck.triangleStructure[row][col]; //newANode is the next node to check to go further down the tree.
                            match(event, aNodeToCheck, vNodeToCheck, matchedSubs, intersectionAttribute);
                        }
                    }
                }
                //Go into intersecting cells(A' and B' from the article) and check for matches
                intersectionAttribute.add(eventAttribute);
                for(int row=index; row<nbrCells; row++){
                    aNodeToCheck = vNodeToCheck.triangleStructure[row][index];
                    match(event, aNodeToCheck, vNodeToCheck, matchedSubs, intersectionAttribute);
                }
                for(int col=0; col < index; col++){
                    aNodeToCheck = vNodeToCheck.triangleStructure[index][col];
                    match(event, aNodeToCheck, vNodeToCheck, matchedSubs, intersectionAttribute);
                }
            }
        }
        return  matchedSubs;
    }

    /**
     * @param attribute
     * @param aNode
     * @return the vNode corresponding to the attribute from the anode's att dir, null if there is none
     */
    public VNode getVNodeFromAttribute(int attribute, ANode aNode){
        return aNode.attributeDirectory.getOrDefault(attribute, null);
    }

    /**
     * Tries to add subscriptions to matchedSubs. For each subscription in the Anode's bucket, check all predicates against event's attribute.
     * @param event
     * @param aNode
     * @param vNode
     * @param matchedSubs
     * @param intersectionAttribute
     */
    public void checkANode(Event event, ANode aNode, VNode vNode, List<Subscription> matchedSubs, Set<Integer> intersectionAttribute){
        boolean isMatched;
        int value;
        for(Subscription sub : aNode.bucket){
            isMatched = true;
            for(Predicate pred: sub.predicates.values()){
                if(event.attributeValuePairs.containsKey(pred.attribute)){
                    value = event.attributeValuePairs.get(pred.attribute);
                    if(!inPath(root, aNode, vNode, pred.attribute)){
                        if(isOutsideSubRange(value, pred)){
                            isMatched = false;
                            break;
                        }
                    }else if(intersectionAttribute.contains(pred.attribute)){
                        if(isOutsideSubRange(value, pred)) {
                            isMatched = false;
                            break;
                        }
                    }
                }else{
                    isMatched = false;
                    break;
                }
            }
            if(isMatched){
                matchedSubs.add(sub);
            }
        }
    }

    /**
     * @param value
     * @param pred
     * @return true if the value given is outside the predicates value range, false if the predicate contain the value
     */
    private boolean isOutsideSubRange(int value, Predicate pred) {
        return !(pred.highValue >= value && pred.lowValue <= value);
    }

    /**
     * main insert method to call GEMTree's insert on sub. Starts by the root Anode.
     * @param sub
     */
    @Override
    public void insert(Subscription sub) {
        insert(sub, root, null);
    }

    /**
     * Takes the sub and inserts it by looking recursively at the anode and vnode.
     * It does this by trying to find a Vnode that shares an attribute with the sub's predicates.
     * @param sub
     * @param aNode
     * @param vNode
     */
    private void insert(Subscription sub, ANode aNode, VNode vNode){
        VNode nextOptimalVNodeInPath = null;
        boolean foundAttribute = false;
        int col;
        int row;

        if(aNode.attributeDirectory.size() > 0){
            for(int predAttribute: sub.predicates.keySet()){
                if(!inPath(root, aNode, vNode, predAttribute)){
                    for(VNode currentVNode: aNode.attributeDirectory.values()){
                        if(currentVNode.attribute == predAttribute){
                            foundAttribute = true;
                            if(isRanked){
                                if(nextOptimalVNodeInPath == null){
                                    nextOptimalVNodeInPath = currentVNode;
                                }
                                if(currentVNode.ranking > nextOptimalVNodeInPath.ranking){
                                    nextOptimalVNodeInPath = currentVNode; //Tanke: blir anode.next fel iom att vi baserar optimal på current node alltid och ej på rank?
                                }
                            }else{
                                nextOptimalVNodeInPath = currentVNode;
                            }
                        }
                    }
                }
            }
        }

        if(foundAttribute){
            col = location(sub.predicates.get(nextOptimalVNodeInPath.attribute).lowValue);
            row = location(sub.predicates.get(nextOptimalVNodeInPath.attribute).highValue);
            ANode nextANodeInPath = nextOptimalVNodeInPath.triangleStructure[row][col];

            insert(sub, nextANodeInPath, nextOptimalVNodeInPath);
            if(isRanked && vNode != null) {
                vNode.updateRanking(alpha);
            }
        }else{
            // put the sub in the bucket and if ranked GEMTree, do the corresponding updating of n & h
            aNode.storeToBucket(sub, isRanked);
            if(aNode.isOverflowing()){
                int attributeSplit = aNode.selectMostPopularAttribute(root, vNode, splitThreshold);
                if(attributeSplit > -1){
                    VNode newVNode = new VNode(nbrCells, attributeSplit, initialBucketSize, aNode);
                    moveSubs(attributeSplit, aNode, vNode, newVNode);
                    //update vNode descendants if ranked
                    if(isRanked && vNode != null) {
                        vNode.decendents.add(newVNode);
                    }
                    //update aNode
                    aNode.attributeDirectory.put(attributeSplit, newVNode);
                }else{
                    //if the split attribute is not powerful enough it is not worth doing the split. Instead, we increase the capacity in the node. See text.
                    int newBucketCapacity = (int) (aNode.currentBucketCapacity *  increaseBucketSizeFactor);
                    aNode.setCurrentBucketCapacity(newBucketCapacity);
                }
            }
        }
    }

    /**
     * Based on the split attribute, finds the subscriptions in the bucket that shares that attribute and that are supposed to be put in the newly created VNode.
     * It then saves these one by one to the new VNode's triangle structure in an ANode's corresponding bucket based on their range and removes them from the old bucket
     * @param attributeSplit
     * @param aNode
     * @param vNode
     * @param newVNode
     */
    public void moveSubs(int attributeSplit, ANode aNode, VNode vNode, VNode newVNode){
        int col;
        int row;
        Iterator<Subscription> iter = aNode.bucket.iterator();
        while(iter.hasNext()){
            Subscription sub = iter.next();
            for(Predicate pred : sub.predicates.values()){
                if(pred.attribute == attributeSplit){
                    col=location(pred.lowValue);
                    row=location(pred.highValue);
                    ANode aNodeNext = newVNode.triangleStructure[row][col];
                    // put the sub in the bucket and if ranked GEMTree, do the corresponding updating of n & h
                    aNodeNext.storeToBucket(sub, isRanked);
                    //Remove sub (use iter to handle ConcurrentModificationException)
                    iter.remove();
                    // remove the sub in the bucket and if ranked GEMTree, do the corresponding updating of n & h
                    aNode.removeFromBucket(sub, isRanked);
                    break;
                }
            }
        }
    }

    /**
     * Checks if an attribute exists in the path's VNodes from the current ANode to the root ANode.
     * @param root
     * @param aNode
     * @param vNode
     * @param attribute
     * @return True if the attribute exists in the path, false otherwise.
     */
    public static boolean inPath(ANode root, ANode aNode, VNode vNode, int attribute){
        ANode aNodeToCheck = aNode;
        VNode vNodeToCheck = vNode;
        if(aNode == root && vNode == null){
            return false;
        }
        while(aNodeToCheck.parentVNode != null){
            if(vNodeToCheck.attribute == attribute){
                return true;
            }
            vNodeToCheck = aNodeToCheck.parentVNode;
            aNodeToCheck = vNodeToCheck.parentANode;
        }
        return false;
    }

    /**
     * @param value
     * @return the value of the index to use (the location) based on the value of an attribute.
     */
    private int location(int value) {
        double index;
        if(value < valDom && value >= 0){
            index = (value)/((double)(valDom)/(nbrCells));
        }else{
            index= nbrCells -1;
        }
        return (int) index;
    }
}
