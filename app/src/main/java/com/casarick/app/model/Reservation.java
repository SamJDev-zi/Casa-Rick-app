package com.casarick.app.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String description;
    private Double deposit;
    private Double balance;
    private String status;
    private int stock;
    private LocalDateTime created;
    private LocalDateTime updated;
    private LocalDateTime expiration;
    private Customer customer;
    private User user;
    private Branch branch;
    private Inventory inventory;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Double getDeposit() {
        return deposit;
    }

    public Double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
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

    public LocalDateTime getExpiration() {
        return expiration;
    }

    public Customer getCustomer() {
        return customer;
    }

    public User getUser() {
        return user;
    }

    public Branch getBranch() {
        return branch;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
