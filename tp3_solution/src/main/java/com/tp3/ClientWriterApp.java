package com.tp3;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientWriterApp {
  public static void main(String[] args) throws Exception {
    RabbitConfig config = new RabbitConfig("localhost", 5672, "guest", "guest");
    RabbitConnectionFactory connectionFactory = new RabbitConnectionFactory(config);

    try (Connection connection = connectionFactory.open();
        Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(QueueNames.WRITE_EXCHANGE, BuiltinExchangeType.FANOUT, true);

      Scanner scanner = new Scanner(System.in);
      int lineNumber = 1;
      System.out.println("ClientWriter ready. Type lines (exit to quit):");

      while (scanner.hasNextLine()) {
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("exit")) {
          break;
        }
        if (input.isEmpty()) {
          continue;
        }

        String message = lineNumber + " " + input;
        channel.basicPublish(
            QueueNames.WRITE_EXCHANGE,
            "",
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes(StandardCharsets.UTF_8));
        System.out.println("[x] Sent: " + message);
        lineNumber++;
      }
    }
  }
}
