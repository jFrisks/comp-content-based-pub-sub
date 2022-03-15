package pub.sub.matching;

import org.projectnessie.cel.tools.Script;

public class SubscriptionWrapper {
    Subscription sub;
    Script script;
    public SubscriptionWrapper(Subscription sub, Script script) {
        this.sub = sub;
        this.script = script;
    }
}
