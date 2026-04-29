# TP3 - Replication with RabbitMQ

The TP3 solution lives in `tp3_solution/` and uses Maven. The legacy root-level Java files are kept as the original reference.

## Build
```
mvn -q -f tp3_solution/pom.xml package
```

## Run
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
