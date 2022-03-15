package pub.sub.matching;

import pub.sub.matching.AVDDMStructure.Group;
import pub.sub.matching.AVDDMStructure.Group.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** The event matching algorithm named AVDDM
 *  Based on the paper: "An event matching algorithm of attribute value domain division for content-based publish/subscribe systems" (can be found in google scholar)
 *  Link: https://ieeexplore.ieee.org/document/8552305 (locked)
 */
public class MatchingAlgoAVDDM implements MatchingAlgo {

    private HashMap<Integer, Subscription> subs;
    HashMap<Integer, Group[]> allAttributes; // attribute -> attributeGroups
    int nbrGroups;
    int valDom;
    int groupStep;

    public MatchingAlgoAVDDM(int expectedNbrSubscribers, int valDom){
        int potentialNbrGroups = (int) Math.pow(expectedNbrSubscribers, (double)1/3);  //our assumptions based on articles choice of 25 groups with 50k subs

        this.subs = new HashMap<>();
        this.allAttributes = new HashMap<>();
        this.nbrGroups = Math.min(potentialNbrGroups, valDom); //our assumptions
        this.valDom = valDom;
        this.groupStep = valDom / nbrGroups;
    }

    @Override
    public List<Subscription> match(Event event) {
        List<Subscription> matchedSubs = new ArrayList<>();

        HashMap<Integer, Integer> subcounter = new HashMap<>(); //subId -> int count
        for(int attribute: event.attributeValuePairs.keySet()){
            var attributeGroups = allAttributes.get(attribute);
            for(Group group : attributeGroups){
                int value = event.attributeValuePairs.get(attribute);
                //if low event value is larger than any of the group's lowValues but the sub.highValue could still match the event
                if(value > group.max ){
                    for(GroupPredicate pred : group.list){
                        if(value <= pred.highValue){
                            subcounter.put(pred.subId, subcounter.getOrDefault(pred.subId, 0)+1);
                        }
                    }
                //if the event value is within the group's lowvalues, check for matches
                }else if(value <= group.max &&  value >= group.min){ //asumption that we have changed min to allow value=0 to be matched in first group
                    for(GroupPredicate pred : group.list){
                        if(value <= pred.highValue && value >= pred.lowValue){
                            subcounter.put(pred.subId, subcounter.getOrDefault(pred.subId, 0)+1);
                        }
                    }
                }else{
                    break;
                }
            }
        }

        //check
        for(int subId : subcounter.keySet()){
            Subscription sub = subs.get(subId);
            if(subcounter.get(subId) == sub.predicates.size()){
                matchedSubs.add(sub);
            }
        }
        return matchedSubs;
    }

    @Override
    public void insert(Subscription sub) {
        //add sub to hashmap
        subs.put(sub.id, sub);

        /*add in structure*/
        //For each attribute in sub pred -> add to each attributeList in correct group
        for(Predicate pred : sub.predicates.values()){
            //getting correct group and mutably modifying it.

            //Initiate or get attribute list
            Group[] attributeList;
            if(!allAttributes.containsKey(pred.attribute)){
                attributeList = new Group[nbrGroups];
                populateAttributeList(attributeList, groupStep);
                allAttributes.put(pred.attribute, attributeList);
            }else{
                attributeList = allAttributes.get(pred.attribute);
            }

            //get group and add
            Group correctGroup = attributeList[groupIndex(pred)];
            Group.GroupPredicate groupPred = new Group.GroupPredicate(pred, sub.id);
            correctGroup.list.add(groupPred);
        }

    }

    private void populateAttributeList(Group[] attributeList, int groupStep) {
        for(int i = 0; i < attributeList.length; i++){
            Group group = new Group(i*groupStep, (i+1)*groupStep);
            attributeList[i] = group;
        }
    }

    private int groupIndex(Predicate pred) {
        return (int) ((pred.lowValue / (double)valDom) * nbrGroups);
    }
}
