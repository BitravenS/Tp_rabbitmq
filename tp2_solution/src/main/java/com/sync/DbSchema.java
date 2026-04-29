package com.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbSchema {
    public static void ensureSalesTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS product_sales (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "sale_date VARCHAR(20) NOT NULL," +
                "region VARCHAR(30) NOT NULL," +
                "product VARCHAR(30) NOT NULL," +
                "qty INT NOT NULL," +
                "cost DECIMAL(10,2) NOT NULL," +
                "amt DECIMAL(10,2) NOT NULL," +
                "tax DECIMAL(10,2) NOT NULL," +
                "total DECIMAL(10,2) NOT NULL," +
                "source_branch VARCHAR(10) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY unique_sale (sale_date, region, product, qty, cost, amt, tax, total, source_branch)" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
