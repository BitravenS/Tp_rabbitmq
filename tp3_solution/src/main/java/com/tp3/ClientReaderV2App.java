package com.tp3;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientReaderV2App {
  private static final int REPLICA_COUNT = 3;
  private static final int TIMEOUT_SEC = 8;

  public static void main(String[] args) throws Exception {
    RabbitConfig config = new RabbitConfig("localhost", 5672, "guest", "guest");
    RabbitConnectionFactory connectionFactory = new RabbitConnectionFactory(config);

    try (Connection connection = connectionFactory.open();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(QueueNames.READ_ALL_EXCHANGE, BuiltinExchangeType.FANOUT, true);
      channel.queueDeclare(QueueNames.READ_ALL_RESPONSE_QUEUE, true, false, false, null);

      ConcurrentHashMap<String, Integer> votes = new ConcurrentHashMap<>();
      CountDownLatch latch = new CountDownLatch(REPLICA_COUNT);

      DeliverCallback callback = (consumerTag, delivery) -> {
        String msg = new String(delivery.getBody(), StandardCharsets.UTF_8).trim();
        if (msg.startsWith("__EOF__|")) {
          latch.countDown();
        } else if (!msg.isEmpty()) {
          votes.merge(msg, 1, Integer::sum);
        }
      };

      String consumerTag = channel.basicConsume(QueueNames.READ_ALL_RESPONSE_QUEUE, true, callback, ct -> {
      });

      System.out.println("Sending 'Read All' request...");
      channel.basicPublish(
          QueueNames.READ_ALL_EXCHANGE, "", null, "Read All".getBytes(StandardCharsets.UTF_8));

      boolean allResponded = latch.await(TIMEOUT_SEC, TimeUnit.SECONDS);
      if (!allResponded) {
        System.out.println("Timeout: some replicas did not respond.");
      }

      List<String> consensusLines = new ArrayList<>();
      for (Map.Entry<String, Integer> entry : votes.entrySet()) {
        if (entry.getValue() >= 2) {
          consensusLines.add(entry.getKey());
        }
      }

      consensusLines.sort(
          (a, b) -> {
            try {
              int numA = Integer.parseInt(a.split(" ")[0]);
              int numB = Integer.parseInt(b.split(" ")[0]);
              return Integer.compare(numA, numB);
            } catch (Exception e) {
              return 0;
            }
          });

      System.out.println("\nConsensus (majority >= 2/3):");
      if (consensusLines.isEmpty()) {
        System.out.println("(no lines reached majority)");
      } else {
        consensusLines.forEach(System.out::println);
      }

      channel.basicCancel(consumerTag);
    }
  }
}
