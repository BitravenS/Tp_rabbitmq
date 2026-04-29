package com.sync;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.nio.charset.StandardCharsets;

public class SalesPublisher {
  private final RabbitConnectionFactory connectionFactory;
  private final RabbitConfig rabbitConfig;
  private final Gson gson = new Gson();

  public SalesPublisher(RabbitConnectionFactory connectionFactory, RabbitConfig rabbitConfig) {
    this.connectionFactory = connectionFactory;
    this.rabbitConfig = rabbitConfig;
  }

  public void publish(SalesMessage message) throws Exception {
    try (Connection connection = connectionFactory.open();
        Channel channel = connection.createChannel()) {
      channel.queueDeclare(rabbitConfig.getQueueName(), true, false, false, null);
      String payload = gson.toJson(message);
      channel.basicPublish(
          "", rabbitConfig.getQueueName(), null, payload.getBytes(StandardCharsets.UTF_8));
    }
  }
}
