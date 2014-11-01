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
  
#### How to run the code
1. Prepare a file or folder with input data. Currently, the pre-defined format goes like this:  
    ```
    * -10.0 10.0 -10.0  
    * -10.0 -10.0 10.0  
    * -10.0 -10.0 -10.0  
    9.725720805017028 8.836225670904302 8.783642664161844  
    10.469045621231 9.697968403776377 -9.514122258282002  
    11.876154194561966 -8.077599554197107 9.84774913960382  
    9.956114269742638 -9.812786238606305 -8.413419757847734
    ```

Your file should numerical values that represent vectors, each line corresponding to a single vector. A sample file `bin/sample` is included for your convenience.

1. Execute the `run.sh` script provided in the `bin` folder (for other OSes, follow your own OS' syntax)
    ```
    ./run.sh
    ```
    
