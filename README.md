# pub-sub-matching
This repo is developed in order to test a publish-subscribe system with Kafka.
It is especially used to further investigate if the state-of-the-art event matching algorithm are efficient and useful in comparison with two versions of linearly searching through all subscriptions.
It is both evaluated in isolation and in a Kafka System.

The work has been performed for Volvo Cars as a Master Thesis.  
Master Thesis students: Linnea Johnsson and Jonathan Frisk.
Supervisor: Oscar Bäckström.

Title of thesis: **Comparing Event Matching Algorithms for Content-Based Publish-Subscribe Systems.**
Link: *TBA*

Short results:
In isolation, the best performing algorithm (GEM-Tree) showed up to 1800x times better than a linear algorithm. This was when the system used small events (1 to 6 event attributes), but quickly declined.
However, with a configuration that mimics the events and subscriptions of a real life system, the results show slightly more to than 5x times better in matching time. With this, it impacts the memory consumption with 4x more than no algorithm.
One downside is that these algos do not handle all types of subscription expression criterias.

An additional event matching algorithm with no algo has been implemented that handles any subscription criteria expression string (called linear-string):

If put into a POC real system that mimics a real configuration including event aggregation in database (10ms response), the matching engine:
- `linear`  handles around 65 event/s at 80k subscriptions.
- `GEM-Tree` handles around 81 event/s at 80k subscriptions.
- `linear-string` handles around 1.3 event/s at 80k subscriptions.

# How to run

There are two ways to run evaluate how the different solutions and algos perform.
1. Test in isolation - big scale, millions of subscriptions.
2. Test in Kafka System - no scaling, up to ~5k subscriptions. (Excluded in public release)

### Prerequisites
+ Linux / Windows / Mac (No Apple Silicon) 
+ Docker
+ Java JDK (17.0.2 used)

### 1. Run in isolation
1. Build project in gradle. One way is with the command  `./gradlew build`.
2. Change the configuration and its parameters in `configDefaultTest()` in `Config.java` to represent your system in the method. It changes the dynamics of event and subscriptions. More can be read in javadocs or later in this readme.
3. Pick test suite configuration in `TestSuite.java` and its method `runTestSuitWithAlgo`(). It will run various evaluations by varying one parameter at the time.
4. Run main method in `App.java`. Either through an IDE or by creating a jar with `./gradlew appJar`, which outputs a jar in the root folder of project, and can be run it with `java -jar appIsolated.jar`.
5. Results will be outputted in .csv files. Matching time will be outputted in `matching_times.csv` while insertions/subscription time is added in `insertion_times.csv`.
6. The results can be added in to excel-file mentioned later in the readme.

### 2. Run in Kafka system
*Excluded in public release*

## Configuration
The configuration represents the system and its dynamics of events and subscriptions.
It is configured by changing parameters in the method `configDefaultTest()` in `Config.java`.

Here are the most common parameters and what they are (also mentioned in javadoc):

| Parameter            | Description                                                                                   |
|----------------------|-----------------------------------------------------------------------------------------------|
| NBR_SUBS             | The number of subscriptions that exists in the system.                                        |
| NBR_EVENTS           | The number of events that is produced in one evaluation                                       |
| NBR_TOTAL_ATTRIBUTES | The number of distinct attributes that exists in the system                                   |
| NBR_SUB_PREDICATES   | The number of subscription predicates in each subscription                                    |
| NBR_EVENT_ATTRIBUTES | The number of event attributes in each event during matching.                                 |
| WIDTH                | The portion of the value domain that is covered by a subscription predicate range (low, high) |
| VAL_DOM              | The value domain of an attribute                                                              |

## Excel-file
To present our results, an excel-file is included.
It holds outputted data from the evaluations and presents plots and tables.
The data is added in the right-most tabs.
Then there are multiple tabs for the different evaluation results. 
To add error-bars with stddev, these need to be added manually in the plots by providing the stdev from the tables in each tab.
The data outputed to csv is in microseconds and bytes, while the excel file expects seconds and GB. So some data conversion is needed.