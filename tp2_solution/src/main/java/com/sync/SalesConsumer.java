package com.sync;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class SalesConsumer {
  private final RabbitConnectionFactory rabbitFactory;
  private final RabbitConfig rabbitConfig;
  private final DbConnectionFactory dbFactory;
  private final SalesRepository repository;
  private final Gson gson = new Gson();

  public SalesConsumer(
      RabbitConnectionFactory rabbitFactory,
      RabbitConfig rabbitConfig,
      DbConnectionFactory dbFactory,
      SalesRepository repository) {
    this.rabbitFactory = rabbitFactory;
    this.rabbitConfig = rabbitConfig;
    this.dbFactory = dbFactory;
    this.repository = repository;
  }

  public void consumeForever() throws Exception {
    Connection rabbitConnection = rabbitFactory.open();
    Channel channel = rabbitConnection.createChannel();
    channel.queueDeclare(rabbitConfig.getQueueName(), true, false, false, null);

    DeliverCallback callback = (consumerTag, delivery) -> {
      String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
      SalesMessage message = gson.fromJson(payload, SalesMessage.class);
      try (java.sql.Connection connection = dbFactory.open()) {
        DbSchema.ensureSalesTable(connection);
        repository.insertSalesBatch(connection, message.getSales(), message.getSourceBranch());
      } catch (java.sql.SQLException e) {
        e.printStackTrace();
      }
    };

    channel.basicConsume(rabbitConfig.getQueueName(), true, callback, consumerTag -> {
    });
  }
}
