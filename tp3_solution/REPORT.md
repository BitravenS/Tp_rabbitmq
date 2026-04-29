# TP3 Distributed Replication (RabbitMQ)

## Goal
Build a simple distributed replication prototype using RabbitMQ. A client writes lines, three replicas persist them into separate files, and readers query the replicas either for the last line or for a majority-consensus view of the full file.

## Processes
- `ClientWriterApp`: sends lines to all replicas (fanout write).
- `ReplicaApp`: consumes writes and handles read requests.
- `ClientReaderApp`: requests the last line and prints the first response (availability-first).
- `ClientReaderV2App`: requests full files and keeps majority lines (consistency-first).

## Messaging Topology
- Write exchange: `replication_fanout` (fanout)
- Read-last exchange: `read_request_fanout` (fanout)
- Read-all exchange: `read_all_fanout` (fanout)
- Read-last responses: `client_reader_responses` (queue)
- Read-all responses: `reader_v2_responses` (queue)

## Code Map
- `RabbitConfig`: stores broker connection parameters.
- `RabbitConnectionFactory`: creates RabbitMQ connections.
- `QueueNames`: centralizes exchange/queue names.
- `TextFileRepository`: file I/O (append, read last, read all).
- `ReplicaService`: core replica logic (write + read-last + read-all).
- `ReplicaApp`: CLI entry to start a replica with an id.
- `ClientWriterApp`: CLI entry to publish write lines.
- `ClientReaderApp`: CLI entry for Read Last (first reply wins).
- `ClientReaderV2App`: CLI entry for Read All + majority vote.

## How it satisfies the TP
- Q1: `ClientWriterApp` publishes lines to a fanout exchange.
- Q2: `ReplicaApp` receives and appends lines in its local file.
- Q3: Each replica writes its own `replica_<id>/data.txt`.
- Q4: `ClientReaderApp` broadcasts `Read Last` and prints the first response.
- Q5: If a replica is down, the first available reply still works (availability).
- Q6: Stop `Replica 2`, write lines, restart; files diverge then reconcile.
- Q7: `ClientReaderV2App` gathers all lines and outputs majority consensus.

## Extra Feature
- Line format validation: replicas reject malformed lines (must start with a number then text).

## Run (simple)
Build:
```
mvn -q -f tp3_solution/pom.xml package
```

Start replicas (3 terminals):
```
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ReplicaApp 1
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ReplicaApp 2
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ReplicaApp 3
```

Write lines:
```
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ClientWriterApp
```

Read last:
```
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ClientReaderApp
```

Read all + majority:
```
java -cp tp3_solution/target/tp3_solution-1.0-SNAPSHOT-jar-with-dependencies.jar com.tp3.ClientReaderV2App
```


## CAP Mapping (short)
- `ClientReaderApp`: favors Availability (AP) by accepting first response.
- `ClientReaderV2App`: favors Consistency (CP) with majority vote.
