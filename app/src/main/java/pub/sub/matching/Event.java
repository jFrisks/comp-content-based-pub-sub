package pub.sub.matching;

import java.util.HashMap;
import java.util.Map;

public class Event {
    public HashMap<Integer, Integer> attributeValuePairs;

    public Event(){
        attributeValuePairs = new HashMap<>();
    }

    void add(int attribute, int value){
        attributeValuePairs.put(attribute, value);
    }
}
