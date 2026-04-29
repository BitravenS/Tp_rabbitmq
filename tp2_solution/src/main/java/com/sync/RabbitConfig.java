package com.sync;

public class RabbitConfig {
  private final String host;
  private final int port;
  private final String username;
  private final String password;
  private final String queueName;

  public RabbitConfig(String host, int port, String username, String password, String queueName) {
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.queueName = queueName;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getQueueName() {
    return queueName;
  }
}
