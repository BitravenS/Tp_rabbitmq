# TP2 Database Synchronization with RabbitMQ

## Goal
Synchronize product sales from two branch office (BO) databases to a head office (HO) database using RabbitMQ as a message queue. Each BO sends its sales rows, and HO consumes and merges them.

## Architecture
- Two BO databases store local sales in the same table structure.
- HO database stores the global view of sales.
- RabbitMQ queue `sales.sync` transports sales batches from BO to HO.

## Data Model
Table `product_sales` (created by `DbSchema.ensureSalesTable`):
- `id` BIGINT AUTO_INCREMENT
- `sale_date` VARCHAR(20)
- `region` VARCHAR(30)
- `product` VARCHAR(30)
- `qty` INT
- `cost` DECIMAL(10,2)
- `amt` DECIMAL(10,2)
- `tax` DECIMAL(10,2)
- `total` DECIMAL(10,2)
- `source_branch` VARCHAR(10)
- `created_at` TIMESTAMP
- Unique key prevents duplicate rows from the same branch.

## Code Walkthrough

### Core Models
- `ProductSales`
  - Holds a sale row (date, region, product, qty, cost, amt, tax, total).
  - Uses `BigDecimal` for currency values.

- `SalesMessage`
  - Envelope sent through RabbitMQ.
  - Fields: `sourceBranch` and `List<ProductSales>`.

### DB Helpers
- `DbConfig`
  - Simple holder for JDBC url, user, password.

- `DbConnectionFactory`
  - Opens JDBC connections using `DbConfig`.

- `DbSchema`
  - `ensureSalesTable(Connection)` creates the `product_sales` table if missing.

- `SalesRepository`
  - `insertSale(...)` inserts one row into `product_sales`.
  - `insertSalesBatch(...)` inserts multiple rows in batch.
  - `fetchSalesByBranch(...)` reads all rows for a specific branch.
  - Uses `INSERT IGNORE` to avoid duplicate inserts at HO.

### RabbitMQ Helpers
- `RabbitConfig`
  - Holds host, port, username, password, and queue name.

- `RabbitConnectionFactory`
  - Opens RabbitMQ connections.

- `SalesPublisher`
  - Serializes `SalesMessage` to JSON and publishes to queue.

- `SalesConsumer`
  - Listens on queue and writes received sales into the HO database.

### Apps (Entry Points)
- `BranchSeederApp`
  - Seeds a BO database from a CSV file.
  - Parses CSV rows and inserts them with `source_branch = branchId`.

- `BranchSenderApp`
  - Reads all sales for one branch from its BO DB.
  - Sends them to RabbitMQ as a `SalesMessage`.
  - Optional `--dry-run` prints a summary without publishing.

- `HqReceiverApp`
  - Runs at HO.
  - Consumes messages from RabbitMQ and merges them into HO DB.

## How Synchronization Works
1. `BranchSeederApp` loads sales into BO databases (one database per branch).
2. `BranchSenderApp` selects all rows for that branch and sends them to RabbitMQ.
3. `HqReceiverApp` consumes the message and inserts rows into HO.
4. `INSERT IGNORE` plus the unique key avoid duplicates.

## CSV Format (from appendix table)
```
Date,Region,Product,Qty,Cost,Amt,Tax,Total
1-Apr,East,Paper,73,12.95,945.35,66.17,1011.52
1-Apr,West,Paper,33,12.95,427.35,29.91,457.26
2-Apr,East,Pens,14,2.19,30.66,2.15,32.81
2-Apr,West,Pens,40,2.19,87.60,6.13,93.73
3-Apr,East,Paper,21,12.95,271.95,18.04,290.99
3-Apr,West,Paper,10,12.95,129.50,9.07,138.57
```

## Running the Apps (example)
1. Start RabbitMQ locally and ensure credentials.
2. Create BO1, BO2, HO databases in MySQL.
3. Build:
```
mvn -q -f tp2_solution/pom.xml package
```
4. Seed BO1 and BO2:
```
java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.BranchSeederApp BO1 /path/to/bo1.csv jdbc:mysql://localhost:3306/bo1db user pass

java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.BranchSeederApp BO2 /path/to/bo2.csv jdbc:mysql://localhost:3306/bo2db user pass
```
5. Start HO receiver:
```
java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.HqReceiverApp jdbc:mysql://localhost:3306/hodb user pass localhost 5672 rabbituser rabbitpass
```
6. Send from BOs:
```
java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.BranchSenderApp BO1 jdbc:mysql://localhost:3306/bo1db user pass localhost 5672 rabbituser rabbitpass

java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.BranchSenderApp BO2 jdbc:mysql://localhost:3306/bo2db user pass localhost 5672 rabbituser rabbitpass
```

Dry run example:
```
java -cp tp2_solution/target/db-sync-rabbitmq-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.sync.BranchSenderApp BO1 jdbc:mysql://localhost:3306/bo1db user pass localhost 5672 rabbituser rabbitpass --dry-run
```

## Notes for the Report
- RabbitMQ decouples BOs from HO. BOs can send data whenever connectivity is available.
- Unique key guarantees idempotency on the HO side.
- Batch insert keeps DB writes efficient.
- JSON payloads keep the transport simple and portable.
