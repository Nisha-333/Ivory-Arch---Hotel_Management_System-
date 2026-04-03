package com.ivoryarch;

import com.ivoryarch.dao.DatabaseConnection;
import com.ivoryarch.thread.BackupTask;
import com.ivoryarch.util.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Thread backupThread;

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseConnection.initializeDatabase();
        FileUtil.writeLog("Application started.");

        BackupTask backupTask = new BackupTask();
        backupThread = new Thread(backupTask, "AutoBackupThread");
        backupThread.setDaemon(true);
        backupThread.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ivoryarch/view/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 920, 620);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Ivory Arch — Hotel Management");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (backupThread != null) backupThread.interrupt();
        DatabaseConnection.closeConnection();
        FileUtil.writeLog("Application stopped.");
    }

    public static void main(String[] args) { launch(args); }
}
