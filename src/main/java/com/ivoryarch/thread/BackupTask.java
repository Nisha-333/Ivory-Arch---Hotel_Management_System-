package com.ivoryarch.thread;
import com.ivoryarch.dao.RoomDAO;
import com.ivoryarch.util.FileUtil;

// WEEK 3: implements Runnable
public class BackupTask implements Runnable {
    private final RoomDAO roomDAO = new RoomDAO();
    private volatile boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                FileUtil.serializeRooms(roomDAO.getAllRooms()); // WEEK 6: Serialization
                FileUtil.writeLog("Auto-backup completed.");
                System.out.println("[BackupTask] Auto-backup done.");
                Thread.sleep(120_000); // WEEK 3: sleep() every 2 minutes
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); running = false; }
        }
    }
    public void stop() { running = false; }
}
