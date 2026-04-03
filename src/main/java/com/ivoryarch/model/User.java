package com.ivoryarch.model;
import java.io.Serializable;
//week 1 and serialization, abstarction encapsulation
public abstract class User implements Serializable {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;

    public User() {}
    public User(String name, String email, String password, String phone) {
        this.name = name; this.email = email;
        this.password = password; this.phone = phone;
    }

    public abstract String getDashboardTitle();
    public abstract String getRoleDescription();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    @Override public String toString() { return "User[" + userId + "] " + name + " (" + role + ")"; }
}
