package com.tp3;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ClientReaderApp {
  private static final int TIMEOUT_SECONDS = 5;

  public static void main(String[] args) throws Exception {
    RabbitConfig config = new RabbitConfig("localhost", 5672, "guest", "guest");
    RabbitConnectionFactory connectionFactory = new RabbitConnectionFactory(config);

    try (Connection connection = connectionFactory.open();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(QueueNames.READ_LAST_EXCHANGE, BuiltinExchangeType.FANOUT, true);
      channel.queueDeclare(QueueNames.READ_LAST_RESPONSE_QUEUE, true, false, false, null);

      System.out.println("Sending 'Read Last' request...");
      channel.basicPublish(
          QueueNames.READ_LAST_EXCHANGE, "", null, "Read Last".getBytes(StandardCharsets.UTF_8));

      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<String> firstResponse = new AtomicReference<>();

      DeliverCallback deliverCallback =
          (consumerTag, delivery) -> {
            if (latch.getCount() == 0) {
              return;
            }
            String response = new String(delivery.getBody(), StandardCharsets.UTF_8).trim();
            firstResponse.set(response);
            latch.countDown();
            System.out.println("Response: " + response);
          };

      channel.basicConsume(
          QueueNames.READ_LAST_RESPONSE_QUEUE, true, deliverCallback, consumerTag -> {});

      boolean received = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      if (!received) {
        System.out.println("Timeout: no replica responded.");
      }
    }
  }
}
