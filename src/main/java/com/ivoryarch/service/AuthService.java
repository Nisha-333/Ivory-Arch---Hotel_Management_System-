package com.ivoryarch.service;
import com.ivoryarch.dao.UserDAO;
import com.ivoryarch.model.*;
import com.ivoryarch.util.*;

public class AuthService {
    private static User loggedInUser = null;
    private static final UserDAO userDAO = new UserDAO();

    public static User login(String email, String password) {
        String hashed = PasswordUtil.hashPassword(password);
        User user = userDAO.getUserByEmailAndPassword(email, hashed);
        if (user != null) {
            loggedInUser = user;
            FileUtil.writeLog("LOGIN: " + user.getEmail() + " [" + user.getRole() + "]");
        }
        return user;
    }

    public static boolean register(Customer c, String plainPassword) {
        c.setPassword(PasswordUtil.hashPassword(plainPassword));
        return userDAO.registerUser(c);
    }

    public static User getLoggedInUser() { return loggedInUser; }
    public static void logout() { loggedInUser = null; }
    public static boolean isAdmin() { return loggedInUser != null && "ADMIN".equals(loggedInUser.getRole()); }
}
