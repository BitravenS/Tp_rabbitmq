package com.sync;

public class HqReceiverApp {
    public static void main(String[] args) throws Exception {
        if (args.length < 7) {
            System.out.println("Usage: HqReceiverApp <dbUrl> <dbUser> <dbPass> <rabbitHost> <rabbitPort> <rabbitUser> <rabbitPass>");
            return;
        }

        String dbUrl = args[0];
        String dbUser = args[1];
        String dbPass = args[2];
        String rabbitHost = args[3];
        int rabbitPort = Integer.parseInt(args[4]);
        String rabbitUser = args[5];
        String rabbitPass = args[6];

        DbConfig dbConfig = new DbConfig(dbUrl, dbUser, dbPass);
        DbConnectionFactory dbFactory = new DbConnectionFactory(dbConfig);
        RabbitConfig rabbitConfig = new RabbitConfig(rabbitHost, rabbitPort, rabbitUser, rabbitPass, "sales.sync");
        SalesRepository repository = new SalesRepository();
        SalesConsumer consumer = new SalesConsumer(new RabbitConnectionFactory(rabbitConfig), rabbitConfig, dbFactory, repository);
        consumer.consumeForever();
    }
}
