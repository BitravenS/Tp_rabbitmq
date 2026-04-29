package com.sync;

import java.util.List;

public class SalesMessage {
  private String sourceBranch;
  private List<ProductSales> sales;

  public SalesMessage() {
  }

  public SalesMessage(String sourceBranch, List<ProductSales> sales) {
    this.sourceBranch = sourceBranch;
    this.sales = sales;
  }

  public String getSourceBranch() {
    return sourceBranch;
  }

  public void setSourceBranch(String sourceBranch) {
    this.sourceBranch = sourceBranch;
  }

  public List<ProductSales> getSales() {
    return sales;
  }

  public void setSales(List<ProductSales> sales) {
    this.sales = sales;
  }
}
