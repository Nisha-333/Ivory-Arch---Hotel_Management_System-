package com.ivoryarch.thread;
import com.ivoryarch.dao.RoomDAO;
import com.ivoryarch.model.RoomStatus;

// WEEK 3: extends Thread
public class CleaningThread extends Thread {
    private final int roomNumber;
    private final RoomDAO roomDAO;

    public CleaningThread(int roomNumber, RoomDAO roomDAO) {
        super("CleaningThread-Room-" + roomNumber);
        this.roomNumber = roomNumber; this.roomDAO = roomDAO;
    }

    @Override
    public void run() {
        System.out.println("[" + getName() + "] Cleaning Room " + roomNumber + "...");
        roomDAO.updateRoomStatus(roomNumber, RoomStatus.CLEANING);
        try {
            Thread.sleep(3000); // WEEK 3: sleep()
            Thread.yield();     // WEEK 3: yield()
            System.out.println("[" + getName() + "] Room " + roomNumber + " is ready!");
            roomDAO.updateRoomStatus(roomNumber, RoomStatus.AVAILABLE);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
