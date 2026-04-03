package com.ivoryarch.thread;
import javafx.application.Platform;
import javafx.scene.control.Alert;

// WEEK 3: Runnable + yield
public class NotificationTask implements Runnable {
    private final String title;
    private final String message;
    private final Alert.AlertType type;

    public NotificationTask(String title, String message, Alert.AlertType type) {
        this.title = title; this.message = message; this.type = type;
    }

    @Override
    public void run() {
        try { Thread.sleep(200); Thread.yield(); } // WEEK 3: yield
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    public static void notify(String title, String msg, Alert.AlertType type) {
        new Thread(new NotificationTask(title, msg, type)).start();
    }
}
