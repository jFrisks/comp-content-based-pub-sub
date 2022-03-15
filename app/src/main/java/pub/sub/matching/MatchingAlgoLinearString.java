package pub.sub.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.projectnessie.cel.checker.Decls;
import org.projectnessie.cel.tools.Script;
import org.projectnessie.cel.tools.ScriptCreateException;
import org.projectnessie.cel.tools.ScriptException;
import org.projectnessie.cel.tools.ScriptHost;
import org.projectnessie.cel.types.jackson.JacksonRegistry;

/** Linear String is the implementation of "no algorithm" for event matching algorithms. We call it linear as we are linearly searching through each subscription and its predicates.
 *  String in Linear String connects to the fact that this version can handle any type of string expression criteria as a subscription criteria.
 *  An example that can be handled is `pred1 && (pred2 && !pred3)`.
 *  It uses google common expression language (cel) to evaluate expressions
 *  Each expression is translated into a script at insertion of subscription.
 *  In match(), each script is evaluated
 **/
public class MatchingAlgoLinearString implements MatchingAlgo {
    boolean debug = false;
    List<SubscriptionWrapper> subs = new ArrayList();

    @Override
    public List<Subscription> match(Event event) {
        List<Subscription> matchedSubs = new ArrayList<>();
        for(SubscriptionWrapper subscript : subs){
            try {
                if(evaluateExpression(event, subscript)){
                    matchedSubs.add(subscript.sub);
                }
            } catch (ScriptException e) {
                //e.printStackTrace();
                System.err.println("Could not evaluate expression string: " + subscript.sub.expressionCriteria + " for subid: "+ subscript.sub.id);
            }
        }
        return matchedSubs;
    }

    @Override
    public void insert(Subscription sub) {
        //Wrap subscription together with script that will execute (using google cel - project nessie)
        Script script = null;
        try {
            // Build the script factory
            ScriptHost scriptHost = ScriptHost.newBuilder()
                    .registry(JacksonRegistry.newRegistry())
                    .build();
            script = scriptHost.buildScript(sub.expressionCriteria)
                    .withDeclarations(
                            Decls.newVar("event", Decls.newObjectType(Event.class.getName())))
                    .withTypes(Event.class)
                    .build();
        } catch (ScriptCreateException e) {
            e.printStackTrace();
        }

        SubscriptionWrapper subscriptionWrapper = new SubscriptionWrapper(sub, script);
        subs.add(subscriptionWrapper);
    }

    //TODO: Refactor to reuse objects etc
    public boolean evaluateExpression(Event event, SubscriptionWrapper subscript) throws ScriptCreateException {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("event", event);

        Boolean result = null;
        try {
            result = subscript.script.execute(Boolean.class, arguments);
            if(debug)
                System.out.println("result is " + result + " for subscription criteria " + subscript.sub.expressionCriteria);
        } catch (ScriptException e) {
            //Attribute does not exists -> false
            result = false;
        }
        return result;
    }
}
