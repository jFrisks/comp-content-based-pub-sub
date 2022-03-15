package pub.sub.matching;

/** An interface inorder to allow lambda-function inside TestSuite **/
public interface EvaluationFunction {
    void function(Config config, String algo, String experiment) throws InterruptedException;
}
