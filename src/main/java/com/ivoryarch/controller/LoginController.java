package com.ivoryarch.controller;

import com.ivoryarch.model.*;
import com.ivoryarch.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;
    @FXML private Button loginBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "CUSTOMER"));
        roleCombo.setValue("CUSTOMER");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            setStatus("Please fill in all fields.", false); return;
        }
        User user = AuthService.login(email, password);
        if (user == null) { setStatus("Invalid email or password.", false); return; }
        if (!user.getRole().equals(role)) { setStatus("Wrong role selected.", false); return; }

        try {
            String fxml = "ADMIN".equals(user.getRole())
                ? "/com/ivoryarch/view/admin_dashboard.fxml"
                : "/com/ivoryarch/view/customer_dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1150, 720);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(user.getDashboardTitle());
            stage.setMaximized(true);
        } catch (Exception e) { setStatus("Error: " + e.getMessage(), false); e.printStackTrace(); }
    }

    @FXML
    private void handleRegister() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Guest Registration");
        dialog.setHeaderText("Create your Ivory Arch account");
        ButtonType regBtn = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(regBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        TextField nameF    = new TextField(); nameF.setPromptText("Full Name");
        TextField emailF   = new TextField(); emailF.setPromptText("Email Address");
        PasswordField passF = new PasswordField(); passF.setPromptText("Password");
        TextField phoneF   = new TextField(); phoneF.setPromptText("Phone Number");
        grid.addRow(0, new Label("Name:"),  nameF);
        grid.addRow(1, new Label("Email:"), emailF);
        grid.addRow(2, new Label("Password:"), passF);
        grid.addRow(3, new Label("Phone:"), phoneF);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == regBtn) return new Customer(nameF.getText(), emailF.getText(), passF.getText(), phoneF.getText());
            return null;
        });
        dialog.showAndWait().ifPresent(c -> {
            boolean ok = AuthService.register(c, c.getPassword());
            setStatus(ok ? "Account created! Please sign in." : "Email already in use.", ok);
        });
    }

    private void setStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success ? "-fx-text-fill:#2d6a4f;" : "-fx-text-fill:#c0392b;");
    }
}
