package com.tp3;

import java.nio.file.Path;

public class ReplicaApp {
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || !args[0].matches("\\d+")) {
            System.out.println("Usage: ReplicaApp <id>");
            return;
        }

        String replicaId = args[0];
        String dirPath = "replica_" + replicaId;
        Path filePath = Path.of(dirPath, "data.txt");

        RabbitConfig config = new RabbitConfig("localhost", 5672, "guest", "guest");
        RabbitConnectionFactory connectionFactory = new RabbitConnectionFactory(config);
        ReplicaService service = new ReplicaService(replicaId, connectionFactory, filePath);
        service.start();
    }
}
