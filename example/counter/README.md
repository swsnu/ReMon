# Collector
Collector is a component of Remon to collects the metrics generated from REEF Application and send them to the Monitor via WebSocket interface.

## How to configure
In the application developers' perspective, there are some configurations have to be set.
(TODO) Write code-level instrumentation

## Example
We will provide some examples to show how ReMon works.

### CounterREEF
It is the first REEF example instrumented with ReMon to monitor the apps behaviour. The logic is too simple. When application runs, one evaluator is created
and the task counts a variable `i`'s value up to 999. Before it counts beyond 999, goes back to 0 and repeat to count from the point.
In the application code, you can find the counter value `i` is the target metric to observe.
(TODO) Write code

Whenever the Evaluator sends heartbeat message, the driver receives the message and sends it to the Monitor server.

#### How to run it
In the local mode, build the application and run with this command
```
java -cp ${project.root}/target/collector-1.0-SNAPSHOT-shaded.jar edu.snu.cms.remon.collector.examples.counter.CounterREEF -monitor_addr ${MONITOR_ADDRESS}
```
Note that when you pass the argument {MONITOR_ADDRESS} you should specify the exact address for the websocket interface.
