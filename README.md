reef-examples
=============

Sample maching learning code for the Big Data and Cloud Systems Course offered at Seoul National University (SNU)

## *k*-means clustering

#### How to build the code
1. Build REEF: check https://cwiki.apache.org/confluence/display/REEF/Compiling+REEF

2. Build shimoga (must be done after 1):
    ```
    git clone https://github.com/Microsoft-CISL/shimoga
    mvn build install
    ```

3. Build reef-tutorial (must be done after 1):
    ```
    git clone https://github.com/cmssnu/reef-tutorial
    mvn build install
    ```
  
#### Preparing data
1. Prepare a file or folder with input data. Currently, the pre-defined format goes like this:
    ```
    * -10.000000000 10.000000000 -10.000000000
    * -10.000000000 -10.000000000 10.000000000
    * -10.000000000 -10.000000000 -10.000000000
    9.725720805017028 8.836225670904302 8.783642664161844
    10.469045621231 9.697968403776377 -9.514122258282002
    11.876154194561966 -8.077599554197107 9.84774913960382
    9.956114269742638 -9.812786238606305 -8.413419757847734
    ```
Your file should contain numerical values that represent vectors, with each vector separated by new lines and each value separated within a vector with at least one space. The algorithm is constructed to read initial cluster centroids too, so mark the vectors that you will use as centroids with an asterik (although they are all in one place in the above example, actually you may place them anywhere like above). A sample file `bin/sample` is included for your convenience.  
If your data files are too big and is rather hard to modify, you can always define your own format by writing your own `KMeansDataParser` class, which is again included in the code (search the `src` folder).  

2. To run your application in YARN runtime, you need to upload your local file to the HDFS:
    ```
    hdfs dfs -put filename
    ```
If you have another way to load your data into the HDFS, or if you're planning to run the code on local rutime, then this step can be skipped.

#### How to run the code
You can use either local runtime, or YARN runtime to run the code.
* Run the application in YARN runtime by executing the `run.sh` script provided in the `bin` folder while giving your data that is in HDFS as input (for OSes other than Linux, use your OS's syntax of executing `.sh`):
    ```
    ./run.sh -input /user/username/sample
    ```

* Run the application in local runtime by adding the `-local true` option (in this case add the `file:///` prefix for your data):
    ```
    ./run.sh -local true -input file:///user/username/sample
    ```
    
#### Additional options
* Set a time limit for your application if you need your job done quickly, by the `-timeout` option (default 100000 milliseconds):
    ```
    ./run.sh -input /user/username/sample -timeout 100000
    ```
    
* Choose the sizes for your evaluators by the `-evalSize` option (default 1024 MBs):
    ```
    ./run.sh -input /user/username/sample -evalSize 1024
    ```
    
* Specify the threshold value used to determine algorithm convergence by the `-convThr` option (default 0.01):
    ```
    ./run.sh -input /user/username/sample -convThr
    ```

* Restrict the maximum number of iterations the algorithm is allowed to perform before it stops, by the `-maxIter` option (default 20 times):
    ```
    ./run.sh -input /user/username/sample -maxIter 20
    ```
    
* Give the number of desired splits you want to read your data in, by the `-splitNum` option (default 4 spilts). However, your request may be neglected if the input data is too big (in this case REEF will read your data in more splits).
    ```
    ./run.sh -input /user/username/sample -splitNum 4
    ```
