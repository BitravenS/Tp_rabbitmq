package com.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SalesCsvParser {
    public List<ProductSales> parse(Path csvPath) throws IOException {
        List<ProductSales> sales = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null) {
                return sales;
            }
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                sales.add(SalesRepository.fromCsvRow(parts));
            }
        }
        return sales;
    }
}
