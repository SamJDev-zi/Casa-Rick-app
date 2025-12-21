package com.casarick.app.model;

public class CartItem {
    private Inventory inventory;
    private int quantity;
    private double discount;
    private double subtotal;

    public CartItem(Inventory inventory, int quantity, double discount) {
        this.inventory = inventory;
        this.quantity = quantity;
        this.discount = discount;
        this.subtotal = (inventory.getSalePrice() * quantity) - discount;
    }

    // Getters y Setters
    public Inventory getInventory() { return inventory; }
    public int getQuantity() { return quantity; }
    public double getDiscount() { return discount; }
    public double getSubtotal() { return subtotal; }

    public String getProductName() { return inventory.getProduct().getName(); }
    public double getUnitPrice() { return inventory.getSalePrice(); }
}