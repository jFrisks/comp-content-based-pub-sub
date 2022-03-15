package pub.sub.matching;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/** Holds config used for an evaluations of the algorithms. They affect the dynamics of events and subscriptions.
 *  Additionally, there are */
public class Config {
    /** The number of subscriptions that is used */
    int NBR_SUBS; //= 20000;

    /** The number of events that is produced in an evaluation. This may also be used as the number of insertions. */
    int NBR_EVENTS; //= 100;

    /** The number of total attributes that exists in the system, or in other words, the number of distinct attributes that can appear in an event*/
    int NBR_TOTAL_ATTRIBUTES;// = 100;

    /** The number of subscription predicates in each subscription */
    int NBR_SUB_PREDICATES;// = 5;

    /** The number of event attributes in each event that is seen by the event matching algorithm.
     *  If an event with 1 attribute is aggregated with more data to 10 attributes, this number of event attributes is 10. */
    int NBR_EVENT_ATTRIBUTES;// = 10;

    /** The value domain of all attributes. For the evaluations, the generation of subs, events, and algo use the same value domain which makes it somewhat useless.
     * But if the value domain was different for the algorithm compared to the events and subscriptions, the performance can change. */
    int VAL_DOM;// = 10000;

    /** The width is how big portion of the value domain that is covered by a subscription predicate range (low, high). Between 0-1.
     *  width = (pred.high-pred.low)/valuedomain. En example if the value domain for price is 100k and a normal predicate is price < 50k. Then the width becomes 0.5 */
    double WIDTH;

    /** The seed used for random generation of data */
    long RANDOM_SEED;// = 0;

    //Algo specific parameters that could be refactored to each algortihm-class but have been tested with different values.
    /** The numbers of buckets that is used by MAEMA */
    int MAEMA_MAX_NUMBER_BUCKETS;// = 500;

    /** A number used by GEM-Tree for ranking. */
    double GEMTree_ALPHA;

    /** The numbers of cells in a RTS in GEM-Tree */
    int GEMTree_NBR_CELLS;// = 8;

    /** The numbers of subscriptions that is needed for a split in GEM-Tree. Exact meaning is referred to the article of GEM-Tree. */
    int GEMTree_SPLIT_THRESHOLD;// = 2; //Assumption: Figure out how large

    /** A number used by GEM-Tree for how much a bucket should be extended if no split. */
    double GEMTree_INCREASE_BUCKET_SIZE_FACTOR;// = 1.1; //Assumption: Figure out how large

    public Config(int NBR_SUBS, int NBR_EVENTS, int NBR_TOTAL_ATTRIBUTES, int NBR_SUB_PREDICATES, int NBR_EVENT_ATTRIBUTES, int VAL_DOM, int MAEMA_MAX_NUMBER_BUCKETS, double GEMTree_ALPHA, double WIDTH, long RANDOM_SEED, int GEMTree_NBR_CELLS, int GEMTree_SPLIT_THRESHOLD, double GEMTree_INCREASE_BUCKET_SIZE_FACTOR){
        this.NBR_SUBS = NBR_SUBS;
        this.NBR_EVENTS = NBR_EVENTS;
        this.NBR_TOTAL_ATTRIBUTES = NBR_TOTAL_ATTRIBUTES;
        this.NBR_SUB_PREDICATES = NBR_SUB_PREDICATES;
        this.NBR_EVENT_ATTRIBUTES = NBR_EVENT_ATTRIBUTES;
        this.VAL_DOM = VAL_DOM;
        this.MAEMA_MAX_NUMBER_BUCKETS = MAEMA_MAX_NUMBER_BUCKETS;
        this.GEMTree_ALPHA = GEMTree_ALPHA;
        this.WIDTH = WIDTH;
        this.RANDOM_SEED = RANDOM_SEED;
        this.GEMTree_NBR_CELLS = GEMTree_NBR_CELLS;
        this.GEMTree_SPLIT_THRESHOLD = GEMTree_SPLIT_THRESHOLD;
        this.GEMTree_INCREASE_BUCKET_SIZE_FACTOR = GEMTree_INCREASE_BUCKET_SIZE_FACTOR;
    }

    /** A config object with default attributes */
    public Config() {}

    /** The default configuration parameters for tests evaluations and is normally called in App and AppKafka. */
    public static Config configDefaultTest() {
        Config config = new Config();
        config.NBR_SUBS = 200; //default 500k
        config.NBR_EVENTS = 100; //default: 100
        config.NBR_TOTAL_ATTRIBUTES = 101; //default is 101 but changed to 51
        config.NBR_EVENT_ATTRIBUTES = 51; //default is 51 but we will change to 10 and 90
        config.NBR_SUB_PREDICATES = 4; //default is 10 but we will change to 1 and 51
        config.VAL_DOM = 10000;
        config.GEMTree_ALPHA = 0.5;
        config.WIDTH = 0.2; //default: 0.5
        config.MAEMA_MAX_NUMBER_BUCKETS = 500;
        config.RANDOM_SEED = 0;
        config.GEMTree_NBR_CELLS = 8;
        config.GEMTree_SPLIT_THRESHOLD = 2;
        config.GEMTree_INCREASE_BUCKET_SIZE_FACTOR = 1.1; //Assumption: Figure out how large
        return config;
    }

    /** This config may be used when debugging. */
    public static Config configDebug() {
        Config config = new Config();
        config.NBR_SUBS = 150;
        config.NBR_EVENTS = 100;
        config.NBR_TOTAL_ATTRIBUTES = 10;
        config.MAEMA_MAX_NUMBER_BUCKETS = 5;
        config.NBR_EVENT_ATTRIBUTES = 3;
        config.NBR_SUB_PREDICATES = 2;
        config.VAL_DOM = 10;
        config.WIDTH = 0.5;
        config.GEMTree_NBR_CELLS = 3;
        return config;
    }

    /** Config set in test! */
    public static Config configTest() {
        Config config = new Config();
        config.NBR_SUBS = 20000;
        config.NBR_EVENTS = 100;
        config.NBR_TOTAL_ATTRIBUTES = 20;
        config.NBR_EVENT_ATTRIBUTES = 10;
        config.NBR_SUB_PREDICATES = 5;
        config.VAL_DOM = 20;
        config.GEMTree_ALPHA = 0.1;
        config.WIDTH = 0.5;
        config.MAEMA_MAX_NUMBER_BUCKETS = 500;
        config.RANDOM_SEED = 0;
        config.GEMTree_NBR_CELLS = 8;
        return config;
    }

    /** A config object that represent parameters that are used in MAEMA article */
    public static Config configMaema() {
        Config config = new Config();
        config.NBR_SUBS = 1000000;
        config.NBR_EVENTS = 100;
        config.NBR_TOTAL_ATTRIBUTES = 40;
        config.NBR_EVENT_ATTRIBUTES = 40;
        config.NBR_SUB_PREDICATES = 10; //decrease nbr preds would increase speed
        config.VAL_DOM = 1000;
        config.GEMTree_ALPHA = 0.1;
        config.WIDTH = 0.5; //increase width would increase speed
        config.MAEMA_MAX_NUMBER_BUCKETS = 500;
        config.RANDOM_SEED = 0;
        config.GEMTree_NBR_CELLS = 8;
        return config;
    }

    /** A config object that represent parameters that are used in AVDDM article */
    public static Config configAvddm() {
        Config config = new Config();
        config.NBR_SUBS = 50000;
        config.NBR_EVENTS = 100;
        config.NBR_TOTAL_ATTRIBUTES = 5;
        config.NBR_EVENT_ATTRIBUTES = 4;
        config.NBR_SUB_PREDICATES = 3;
        config.VAL_DOM = 1000;
        config.GEMTree_ALPHA = 0.1;
        config.WIDTH = 0.1;
        config.MAEMA_MAX_NUMBER_BUCKETS = 500;
        config.RANDOM_SEED = 0;
        config.GEMTree_NBR_CELLS = 8;
        return config;
    }


    public void setWIDTH(double newWidth){
        this.WIDTH = newWidth;
    }

    public void setNBR_EVENT_ATTRIBUTES(int nbr_event_attributes){
        this.NBR_EVENT_ATTRIBUTES = nbr_event_attributes;
    }

    public void setNBR_SUBS(int nbr_subs){
        this.NBR_SUBS = nbr_subs;
    }

    public void setNBR_SUB_PREDICATES(int nbrSubPredicates) {
        this.NBR_SUB_PREDICATES = nbrSubPredicates;
    }

    /** returns csv-formatted header-string of important attributes in config */
    public String toHeaderCSVString(){ //TODO: generalize based on list in toCSVString
        return "Experiment;Algo;Time;NBR_SUBS;NBR_EVENTS;NBR_TOTAL_ATTRIBUTES;NBR_SUB_PREDICATES;NBR_EVENT_ATTRIBUTES;VAL_DOM;MAX_NUMBER_BUCKETS;ALPHA;WIDTH;RANDOM_SEED;NBR_CELLS;INIT_BUCKET_SIZE;SPLIT_THRESHOLD;INCREASE_BUCKET_SIZE_FACTOR; MATCHABILITY";
    }

    /** returns csv-formatted config-string of important attributes in config */
    public String toCSVString() {
        Collection<Object> listOfConfigs = Arrays.asList(NBR_SUBS, NBR_EVENTS, NBR_TOTAL_ATTRIBUTES, NBR_SUB_PREDICATES, NBR_EVENT_ATTRIBUTES, VAL_DOM, MAEMA_MAX_NUMBER_BUCKETS, GEMTree_ALPHA, WIDTH, RANDOM_SEED, GEMTree_NBR_CELLS, GEMTree_SPLIT_THRESHOLD, GEMTree_INCREASE_BUCKET_SIZE_FACTOR);
        return listOfConfigs.stream().map(Object::toString).collect(Collectors.joining(";")); //-> "nbr_events; nbr_subscribers"
    }
}
