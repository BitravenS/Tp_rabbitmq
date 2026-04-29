package com.sync;

import java.io.Serializable;
import java.math.BigDecimal;

public class ProductSales implements Serializable {
    private String saleDate;
    private String region;
    private String product;
    private int qty;
    private BigDecimal cost;
    private BigDecimal amt;
    private BigDecimal tax;
    private BigDecimal total;

    public ProductSales() {}

    public ProductSales(String saleDate, String region, String product, int qty, BigDecimal cost, BigDecimal amt, BigDecimal tax, BigDecimal total) {
        this.saleDate = saleDate;
        this.region = region;
        this.product = product;
        this.qty = qty;
        this.cost = cost;
        this.amt = amt;
        this.tax = tax;
        this.total = total;
    }

    // Getters and Setters
    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public BigDecimal getAmt() { return amt; }
    public void setAmt(BigDecimal amt) { this.amt = amt; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    @Override
    public String toString() {
        return "ProductSales{" +
                "saleDate='" + saleDate + '\'' +
                ", region='" + region + '\'' +
                ", product='" + product + '\'' +
                ", qty=" + qty +
                ", cost=" + cost +
                ", amt=" + amt +
                ", tax=" + tax +
                ", total=" + total +
                '}';
    }
}
