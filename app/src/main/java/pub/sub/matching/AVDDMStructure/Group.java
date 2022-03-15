package pub.sub.matching.AVDDMStructure;

import pub.sub.matching.Predicate;

import java.util.ArrayList;

public class Group {
    public int max;
    public int min;
    public ArrayList<GroupPredicate> list;
    public Group(int min, int max) {
        this.list = new ArrayList();
        this.min = min;
        this.max = max;
    }

    public static class GroupPredicate extends Predicate{
        public int subId;

        public GroupPredicate(Predicate pred, int subId){
            super(pred.attribute, pred.lowValue, pred.highValue);
            this.subId = subId;
        }
    }
}
