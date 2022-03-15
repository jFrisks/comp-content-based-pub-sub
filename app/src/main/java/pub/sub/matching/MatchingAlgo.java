package pub.sub.matching;

import java.util.List;

public interface MatchingAlgo {
    List<Subscription> match(Event event); //TODO: refactor use of matchedsubs

    void insert(Subscription sub);
}
