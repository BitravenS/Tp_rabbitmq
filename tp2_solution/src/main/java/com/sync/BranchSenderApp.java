package com.sync;

import java.util.List;

public class BranchSenderApp {
    public static void main(String[] args) throws Exception {
        if (args.length < 8) {
            System.out.println("Usage: BranchSenderApp <branchId> <dbUrl> <dbUser> <dbPass> <rabbitHost> <rabbitPort> <rabbitUser> <rabbitPass> [--dry-run]");
            return;
        }

        String branchId = args[0];
        String dbUrl = args[1];
        String dbUser = args[2];
        String dbPass = args[3];
        String rabbitHost = args[4];
        int rabbitPort = Integer.parseInt(args[5]);
        String rabbitUser = args[6];
        String rabbitPass = args[7];
        boolean dryRun = args.length > 8 && "--dry-run".equalsIgnoreCase(args[8]);

        DbConfig dbConfig = new DbConfig(dbUrl, dbUser, dbPass);
        DbConnectionFactory dbFactory = new DbConnectionFactory(dbConfig);
        SalesRepository repository = new SalesRepository();
        List<ProductSales> sales;
        try (java.sql.Connection connection = dbFactory.open()) {
            DbSchema.ensureSalesTable(connection);
            sales = repository.fetchSalesByBranch(connection, branchId);
        }
        if (sales.isEmpty()) {
            System.out.println("No sales found in branch database for branch: " + branchId);
            return;
        }

        if (dryRun) {
            System.out.println("Dry run: would publish " + sales.size() + " sales from branch " + branchId);
            for (int i = 0; i < Math.min(3, sales.size()); i++) {
                System.out.println("  " + sales.get(i));
            }
            return;
        }

        RabbitConfig rabbitConfig = new RabbitConfig(rabbitHost, rabbitPort, rabbitUser, rabbitPass, "sales.sync");
        SalesPublisher publisher = new SalesPublisher(new RabbitConnectionFactory(rabbitConfig), rabbitConfig);
        SalesMessage message = new SalesMessage(branchId, sales);
        publisher.publish(message);

        System.out.println("Published " + sales.size() + " sales from branch " + branchId);
    }
}
