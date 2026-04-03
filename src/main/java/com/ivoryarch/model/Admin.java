package com.ivoryarch.model;

public class Admin extends User {
    public Admin() { super(); setRole("ADMIN"); }
    public Admin(String name, String email, String password, String phone) {
        super(name, email, password, phone);
        setRole("ADMIN");
    }
    @Override public String getDashboardTitle() { return "Ivory Arch — Admin Panel"; }
    @Override public String getRoleDescription() { return "Hotel Administrator"; }
}
