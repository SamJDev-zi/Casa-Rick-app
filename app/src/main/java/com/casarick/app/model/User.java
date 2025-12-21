package com.casarick.app.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private Long id;
    private String name;
    private String lastName;
    private String phoneNumber;
    private Role role;
    private List<Permission> permissionList;

    public User() {
    }

    public User(Long id, String name, String lastName, String phoneNumber, Role role) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.permissionList = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }
}
