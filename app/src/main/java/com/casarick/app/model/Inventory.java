package com.casarick.app.model;

import java.time.LocalDateTime;

public class Inventory {
    private Long id;
    private Product product;
    private Double costPrice;
    private Double salePrice;
    private int stock;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Branch branch;

    public Inventory(Long id, Product product,Double costPrice,Double salePrice,int stock,LocalDateTime created,LocalDateTime updated,Branch branch){
        this.id = id;
        this.product = product;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.created = created;
        this.updated = updated;
        this.branch = branch;
    }
    public Inventory(){}

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public int getStock() {
        return stock;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Inventory copy() {
        return new Inventory(id, product, costPrice, salePrice, stock, created, updated, branch);
    }
}
