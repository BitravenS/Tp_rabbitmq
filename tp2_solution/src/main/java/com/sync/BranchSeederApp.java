package com.sync;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

public class BranchSeederApp {
  public static void main(String[] args) throws Exception {
    if (args.length < 5) {
      System.out.println("Usage: BranchSeederApp <branchId> <csvPath> <dbUrl> <dbUser> <dbPass>");
      return;
    }

    String branchId = args[0];
    Path csvPath = Path.of(args[1]);
    String dbUrl = args[2];
    String dbUser = args[3];
    String dbPass = args[4];

    DbConfig dbConfig = new DbConfig(dbUrl, dbUser, dbPass);
    DbConnectionFactory dbFactory = new DbConnectionFactory(dbConfig);
    SalesCsvParser parser = new SalesCsvParser();
    SalesRepository repository = new SalesRepository();
    List<ProductSales> sales = parser.parse(csvPath);

    if (sales.isEmpty()) {
      System.out.println("No sales found in CSV: " + csvPath);
      return;
    }

    try (Connection connection = dbFactory.open()) {
      DbSchema.ensureSalesTable(connection);
      repository.insertSalesBatch(connection, sales, branchId);
    }

    System.out.println("Inserted " + sales.size() + " rows for branch " + branchId);
  }
}
