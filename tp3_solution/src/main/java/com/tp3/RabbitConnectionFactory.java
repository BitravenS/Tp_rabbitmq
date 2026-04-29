package com.tp3;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConnectionFactory {
    private final RabbitConfig config;

    public RabbitConnectionFactory(RabbitConfig config) {
        this.config = config;
    }

    public Connection open() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getHost());
        factory.setPort(config.getPort());
        factory.setUsername(config.getUsername());
        factory.setPassword(config.getPassword());
        return factory.newConnection();
    }
}
