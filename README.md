# actyx-challenge
An attempt on the [Actyx Tech Challenges](https://www.actyx.io/en/tech-challenges/) using [Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html) and [Scala](https://www.scala-lang.org/).

## Challenge 1: Power Usage Alert
`
In order to complete this challenge you should develop an application which monitors the current being drawn by all machines in the Actyx Machine Park, and alerts a fictitious operator whenever this current goes above the machine specific alert threshold.
`

The alert is realized as a simple console output for now.
The respective parameters are to be found in [application.conf](https://github.com/abo64/actyx-challenge/blob/master/src/main/resources/application.conf#L44).

### Try it out
Clone this repository, `cd` into it and start [sbt](http://www.scala-sbt.org).
Then execute `run` and choose `PowerUsageAlertApp`.
You will see the alerts on the console.
Press `return` to stop the application.

## Challenge 2: Environmental Correlation Analysis
`
In order to complete this challenge you should develop an application that derives the correlation between the three environmental factors temperature, pressure and humidity, and the current drawn by the different types of machines available in the Actyx Machine Park.
`

This is implemented in two steps:
- Gather data: [EnvironmentalCorrelationStoreDataApp](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/actyx/challenge2/EnvironmentalCorrelationStoreDataApp.scala)
- Analyze the data with Machine Learning (ML): [EnvironmentalCorrelationAnalysisBatchApp](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/actyx/challenge2/EnvironmentalCorrelationAnalysisBatchApp.scala)
- There is also a scaffolding for real-time/online ML: [EnvironmentalCorrelationAnalysisOnlineApp](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/actyx/challenge2/EnvironmentalCorrelationAnalysisOnlineApp.scala)
Problem with it is that I could not find a released ML library yet that supports this kind of incremental ML.

Again, the respective parameters are to be found in [application.conf](https://github.com/abo64/actyx-challenge/blob/master/src/main/resources/application.conf#L44).

### About the Machine Learning (ML) models used
This is a classical ML regression problem: Find the hidden function from environmental parameters to machine currents.

I have hidden the actual implementations behind trait [MLRegression](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/machinelearning/MLRegression.scala).
Furthermore I assumed a simple linear correlation and used some respective models from [Apache Commons Math](http://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/org/apache/commons/math3/stat/regression/package-frame.html) and [Smile](http://haifengl.github.io/smile/api/scala/index.html#smile.regression.package), all to be found in package [abo.machinelearning](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/machinelearning/). To test them just change [abo.machinelearning.MLRegression.getImpl](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/machinelearning/MLRegression.scala#L12).
Problem with both libraries is that the whole DataSet has to be in RAM in order to be passed as a parameter. For bigger data some library like [Spark MLib](http://spark.apache.org/mllib/) might be a better choice.

Now I am waiting for the new releases of those two libraries to also try out online ML with [abo.machinelearning.IncrementalML](https://github.com/abo64/actyx-challenge/blob/master/src/main/scala/abo/machinelearning/IncrementalML.scala).

### Try it out
Clone this repository, `cd` into it and start [sbt](http://www.scala-sbt.org).
Then execute `run` and choose `EnvironmentalCorrelationStoreDataApp` and let it run for a while (> 10 minutes at least, say) to have enough data for ML (or just use the existing file [ml-data.csv.gz](https://github.com/abo64/actyx-challenge/blob/master/ml-data.csv.gz) and skip this step).

After that execute `run` and choose `EnvironmentalCorrelationAnalysisBatchApp`. You will see the linear coefficients for the environmental parameters to calculate the machine current on the console.
