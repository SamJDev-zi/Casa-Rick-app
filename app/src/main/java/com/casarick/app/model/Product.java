package com.casarick.app.model;

public class Product {
    private Long id;
    private String name;
    private Category category;
    private Type type;
    private Industry industry;
    private String color;
    private String size;
    private String photoUrl;
    private String barCodeNumber;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public Type getType() {
        return type;
    }

    public Industry getIndustry() {
        return industry;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getBarCodeNumber() {
        return barCodeNumber;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setBarCodeNumber(String barCodeNumber) {
        this.barCodeNumber = barCodeNumber;
    }
}
