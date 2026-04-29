package com.sync;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalesRepository {
    private static final String INSERT_SQL =
            "INSERT IGNORE INTO product_sales (sale_date, region, product, qty, cost, amt, tax, total, source_branch) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_ALL_SQL =
            "SELECT sale_date, region, product, qty, cost, amt, tax, total FROM product_sales WHERE source_branch = ?";

    public void insertSale(Connection connection, ProductSales sale, String sourceBranch) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
            stmt.setString(1, sale.getSaleDate());
            stmt.setString(2, sale.getRegion());
            stmt.setString(3, sale.getProduct());
            stmt.setInt(4, sale.getQty());
            stmt.setBigDecimal(5, sale.getCost());
            stmt.setBigDecimal(6, sale.getAmt());
            stmt.setBigDecimal(7, sale.getTax());
            stmt.setBigDecimal(8, sale.getTotal());
            stmt.setString(9, sourceBranch);
            stmt.executeUpdate();
        }
    }

    public List<ProductSales> fetchSalesByBranch(Connection connection, String sourceBranch) throws SQLException {
        List<ProductSales> sales = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL)) {
            stmt.setString(1, sourceBranch);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductSales sale = new ProductSales(
                            rs.getString("sale_date"),
                            rs.getString("region"),
                            rs.getString("product"),
                            rs.getInt("qty"),
                            rs.getBigDecimal("cost"),
                            rs.getBigDecimal("amt"),
                            rs.getBigDecimal("tax"),
                            rs.getBigDecimal("total")
                    );
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    public void insertSalesBatch(Connection connection, List<ProductSales> sales, String sourceBranch) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {
            for (ProductSales sale : sales) {
                stmt.setString(1, sale.getSaleDate());
                stmt.setString(2, sale.getRegion());
                stmt.setString(3, sale.getProduct());
                stmt.setInt(4, sale.getQty());
                stmt.setBigDecimal(5, sale.getCost());
                stmt.setBigDecimal(6, sale.getAmt());
                stmt.setBigDecimal(7, sale.getTax());
                stmt.setBigDecimal(8, sale.getTotal());
                stmt.setString(9, sourceBranch);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public static ProductSales fromCsvRow(String[] row) {
        if (row.length != 8) {
            throw new IllegalArgumentException("Invalid row length: " + row.length);
        }
        return new ProductSales(
                row[0].trim(),
                row[1].trim(),
                row[2].trim(),
                Integer.parseInt(row[3].trim()),
                new BigDecimal(row[4].trim()),
                new BigDecimal(row[5].trim()),
                new BigDecimal(row[6].trim()),
                new BigDecimal(row[7].trim())
        );
    }
}
