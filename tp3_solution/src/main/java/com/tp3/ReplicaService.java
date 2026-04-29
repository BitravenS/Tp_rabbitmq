package com.tp3;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.file.Path;

public class ReplicaService {
    private final String replicaId;
    private final RabbitConnectionFactory connectionFactory;
    private final TextFileRepository repository;

    public ReplicaService(String replicaId, RabbitConnectionFactory connectionFactory, Path filePath) {
        this.replicaId = replicaId;
        this.connectionFactory = connectionFactory;
        this.repository = new TextFileRepository(filePath);
    }

    public void start() throws Exception {
        repository.ensureDirectory();

        Connection connection = connectionFactory.open();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(QueueNames.WRITE_EXCHANGE, BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(QueueNames.READ_LAST_EXCHANGE, BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(QueueNames.READ_ALL_EXCHANGE, BuiltinExchangeType.FANOUT, true);

        String writeQueue = "replica_" + replicaId + "_queue";
        String readLastQueue = "replica_" + replicaId + "_requests";
        String readAllQueue = "replica_" + replicaId + "_read_req";

        channel.queueDeclare(writeQueue, true, false, false, null);
        channel.queueBind(writeQueue, QueueNames.WRITE_EXCHANGE, "");

        channel.queueDeclare(readLastQueue, true, false, false, null);
        channel.queueBind(readLastQueue, QueueNames.READ_LAST_EXCHANGE, "");

        channel.queueDeclare(readAllQueue, true, false, false, null);
        channel.queueBind(readAllQueue, QueueNames.READ_ALL_EXCHANGE, "");

        channel.queueDeclare(QueueNames.READ_LAST_RESPONSE_QUEUE, true, false, false, null);
        channel.queueDeclare(QueueNames.READ_ALL_RESPONSE_QUEUE, true, false, false, null);

        DeliverCallback writeCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            try {
                repository.appendLine(message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println("[✓] Replica " + replicaId + " wrote: " + message.trim());
            } catch (IOException e) {
                System.err.println("[!] Replica " + replicaId + " write failed: " + e.getMessage());
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };
        channel.basicConsume(writeQueue, false, writeCallback, consumerTag -> {});

        DeliverCallback readLastCallback = (consumerTag, delivery) -> {
            String command = new String(delivery.getBody(), "UTF-8").trim();
            if (!"Read Last".equals(command)) {
                return;
            }
            try {
                String lastLine = repository.readLastLine().orElse("Fichier vide");
                channel.basicPublish("", QueueNames.READ_LAST_RESPONSE_QUEUE, null, lastLine.getBytes("UTF-8"));
                System.out.println("[→] Replica " + replicaId + " replied last line.");
            } catch (IOException e) {
                System.err.println("[!] Replica " + replicaId + " read last failed: " + e.getMessage());
            }
        };
        channel.basicConsume(readLastQueue, true, readLastCallback, consumerTag -> {});

        DeliverCallback readAllCallback = (consumerTag, delivery) -> {
            String command = new String(delivery.getBody(), "UTF-8").trim();
            if (!"Read All".equals(command)) {
                return;
            }
            try {
                for (String line : repository.readAllLines()) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    channel.basicPublish("", QueueNames.READ_ALL_RESPONSE_QUEUE, null, line.getBytes("UTF-8"));
                }
                String eof = "__EOF__|" + replicaId;
                channel.basicPublish("", QueueNames.READ_ALL_RESPONSE_QUEUE, null, eof.getBytes("UTF-8"));
                System.out.println("[→] Replica " + replicaId + " replied full file.");
            } catch (IOException e) {
                System.err.println("[!] Replica " + replicaId + " read all failed: " + e.getMessage());
            }
        };
        channel.basicConsume(readAllQueue, true, readAllCallback, consumerTag -> {});

        System.out.println("Replica " + replicaId + " ready. Press Enter to stop.");
        System.in.read();

        channel.close();
        connection.close();
    }
}
