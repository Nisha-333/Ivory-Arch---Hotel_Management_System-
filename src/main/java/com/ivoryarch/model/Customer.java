package com.ivoryarch.model;

public class Customer extends User {
    private String address;
    private String idProof;

    public Customer() { super(); setRole("CUSTOMER"); }
    public Customer(String name, String email, String password, String phone) {
        super(name, email, password, phone);
        setRole("CUSTOMER");
    }
    @Override public String getDashboardTitle() { return "Ivory Arch — Guest Portal"; }
    @Override public String getRoleDescription() { return "Hotel Guest"; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }
}
