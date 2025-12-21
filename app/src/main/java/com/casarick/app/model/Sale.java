package com.casarick.app.model;

import java.time.LocalDateTime;

public class Sale {
    private Long id;
    private String description;
    private int stock;
    private Double saleAmount;
    private Double saleDiscount;
    private Double saleTotal;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Customer customerDTO;
    private User userDTO;
    private Branch branchDTO;
    private Inventory InventoryDTO;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getStock() {
        return stock;
    }

    public Double getSaleAmount() {
        return saleAmount;
    }

    public Double getSaleDiscount() {
        return saleDiscount;
    }

    public Double getSaleTotal() {
        return saleTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Customer getCustomerDTO() {
        return customerDTO;
    }

    public User getUserDTO() {
        return userDTO;
    }

    public Branch getBranchDTO() {
        return branchDTO;
    }

    public Inventory getInventoryDTO() {
        return InventoryDTO;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setSaleAmount(Double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public void setSaleDiscount(Double saleDiscount) {
        this.saleDiscount = saleDiscount;
    }

    public void setSaleTotal(Double saleTotal) {
        this.saleTotal = saleTotal;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCustomerDTO(Customer customerDTO) {
        this.customerDTO = customerDTO;
    }

    public void setUserDTO(User userDTO) {
        this.userDTO = userDTO;
    }

    public void setBranchDTO(Branch branchDTO) {
        this.branchDTO = branchDTO;
    }

    public void setInventoryDTO(Inventory inventoryDTO) {
        InventoryDTO = inventoryDTO;
    }
}
