package pub.sub.matching.GemStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VNode {

    public ANode parentANode;
    public ANode[][] triangleStructure; //RTS -> right triangleStructure structure
    public int attribute;
    public int ranking;
    private int cost;
    private double num;
    private double m;
    double n;
    int h;
    public List<VNode> decendents;

    public VNode(int nbrCells, int attribute, int bucketSize, ANode aNode){
        this.triangleStructure = new ANode[nbrCells][nbrCells];
        for(int i = 0; i <nbrCells; i++ ){
            for(int j = 0; j<nbrCells; j++){
                this.triangleStructure[i][j] = new ANode(bucketSize, this);
            }
        }
        this.attribute = attribute;
        this.cost = 0;
        this.num = 1;
        this.ranking = 1;
        this.parentANode = aNode;
        this.decendents = new ArrayList<>();
    }

    /** Updating a vNodes ranking. Assumed it's called from bottom to top in branch
     *  Assumption: update ranking on vNodes instead on aNodes as pseudo showed*/
    public void updateRanking(double alpha){
        //calculate rank and set in VNode
        this.m = 0.5; //calcNewM(this.m, eventValue, valueDom);
        this.cost = cost(this, alpha);
        this.num = this.num();
        this.ranking = (int) (this.cost / this.num);
    }

    /** Updating a vNodes cost.
     * Assumption: it's called from bottom to top in branch &
     * removed nbrEventAttribute == pathLength condition as mentioned in pseudo code since we don't know variable events*/
    public int cost(VNode vNode, double alpha){
        if(isLeafNode(vNode)){
            return (int) ((vNode.m * vNode.n * alpha) + (vNode.m * vNode.h));
        }else{
            int sumMCost = 0;
            for(VNode decendent : vNode.decendents){
                sumMCost += vNode.m * decendent.cost;
            }
            return (int)(sumMCost + (vNode.m * vNode.n * alpha) + (vNode.m * vNode.h));
        }
    }


    /** Updating a vNodes num. Assumed it's called from bottom to top in branch */
    public double num(){
        if(isLeafNode(this)){
            return this.n;
        }else{
            int numSum = 0;
            for(VNode decendent : this.decendents){
                numSum += decendent.num;
            }
            return numSum + this.n;
        }
    }

    public boolean isLeafNode(VNode vNode){
        return vNode.decendents.size() == 0;
    }

    /** updates H that is a part of the ranking.
     * Improvement: Check if more efficient way. Removing specific attribute may remove an attribute also stored in other bucket -> incorrect. So we need to check all buckets anyway.
     */
    public void updateH() {
        //go through rts to find all aNodes. Go through their buckets to get sets of attributes.
        Set<Integer> attributes = new HashSet<>();
        for(int row = 0; row < triangleStructure.length; row ++){
            for(int col = 0; col < triangleStructure.length; col ++){
                ANode currentAnode = triangleStructure[row][col];
                Set<Integer> set = currentAnode.attributeDirectory.keySet();
                attributes.addAll(set);
            }
        }
        Set<Integer> attributesInPath = attributesInPath();
        attributes.removeAll(attributesInPath);
        this.h = attributes.size();
    }

    /** Returns the list of attributes that is in the current path, from current node to root node.
     * @return Set
     */
    private Set<Integer> attributesInPath() {
        Set<Integer> attributes = new HashSet<>();
        ANode aNodeToCheck = this.parentANode;
        VNode vNodeToCheck;
        while(aNodeToCheck.parentVNode != null){
            attributes.add(attribute);
            vNodeToCheck = aNodeToCheck.parentVNode;
            aNodeToCheck = vNodeToCheck.parentANode;
        }
        return attributes;
    }
}
