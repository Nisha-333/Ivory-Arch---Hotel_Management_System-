package com.ivoryarch.util;
import com.ivoryarch.model.Bill;
import com.ivoryarch.model.Room;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class FileUtil {
    private static final String LOG_FILE    = "data/activity.log";
    private static final String INVOICE_FILE= "data/invoices.txt";
    private static final String BACKUP_FILE = "data/backups.dat";
    private static final String RAF_FILE    = "data/rooms.raf";
    private static final int RECORD_SIZE    = 128;

    static { new File("data").mkdirs(); }

    public static void writeLog(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write("[" + LocalDateTime.now() + "] " + message + "\n");
        } catch (IOException e) { System.err.println("writeLog: " + e.getMessage()); }
    }

    public static List<String> readLog() {
        List<String> lines = new ArrayList<>();
        File f = new File(LOG_FILE);
        if (!f.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line; while ((line = br.readLine()) != null) lines.add(line);
        } catch (IOException e) { System.err.println("readLog: " + e.getMessage()); }
        return lines;
    }

    public static void writeInvoiceBytes(Bill bill) {
        try (FileOutputStream fos = new FileOutputStream(INVOICE_FILE, true)) {
            fos.write(buildInvoiceText(bill).getBytes());
        } catch (IOException e) { System.err.println("writeInvoiceBytes: " + e.getMessage()); }
    }

    public static String readInvoiceFile() {
        File f = new File(INVOICE_FILE);
        if (!f.exists()) return "No invoices found.";
        try (FileInputStream fis = new FileInputStream(INVOICE_FILE)) {
            return new String(fis.readAllBytes());
        } catch (IOException e) { return "Error reading invoices."; }
    }

    public static void serializeRooms(List<Room> rooms) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BACKUP_FILE))) {
            oos.writeObject(rooms);
            writeLog("Rooms serialized. Count: " + rooms.size());
        } catch (IOException e) { System.err.println("serializeRooms: " + e.getMessage()); }
    }

    @SuppressWarnings("unchecked")
    public static List<Room> deserializeRooms() {
        File f = new File(BACKUP_FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BACKUP_FILE))) {
            List<Room> rooms = (List<Room>) ois.readObject();
            writeLog("Rooms deserialized. Count: " + rooms.size());
            return rooms;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("deserializeRooms: " + e.getMessage()); return new ArrayList<>();
        }
    }

    public static void saveRoomToRAF(int roomNumber, double price) {
        try (RandomAccessFile raf = new RandomAccessFile(RAF_FILE, "rw")) {
            raf.seek((long) roomNumber * RECORD_SIZE);
            raf.writeInt(roomNumber); raf.writeDouble(price); raf.writeBoolean(true);
        } catch (IOException e) { System.err.println("saveRoomToRAF: " + e.getMessage()); }
    }

    public static double readRoomPriceFromRAF(int roomNumber) {
        try (RandomAccessFile raf = new RandomAccessFile(RAF_FILE, "r")) {
            raf.seek((long) roomNumber * RECORD_SIZE);
            raf.readInt(); return raf.readDouble();
        } catch (IOException e) { return -1; }
    }

    private static String buildInvoiceText(Bill bill) {
        return "\n==============================\n" +
               "    IVORY ARCH — INVOICE #" + bill.getBillId() + "\n" +
               "==============================\n" +
               "Guest      : " + bill.getCustomerName() + "\n" +
               "Room       : " + bill.getRoomNumber() + "\n" +
               "Days       : " + bill.getNumberOfDays() + "\n" +
               "Room Charge: Rs." + String.format("%.2f", bill.getRoomCharge()) + "\n" +
               "Food       : Rs." + String.format("%.2f", bill.getFoodCharge()) + "\n" +
               "GST (18%)  : Rs." + String.format("%.2f", bill.getGstAmount()) + "\n" +
               "Discount   : Rs." + String.format("%.2f", bill.getDiscountAmount()) + "\n" +
               "TOTAL      : Rs." + String.format("%.2f", bill.getTotalAmount()) + "\n" +
               "Status     : " + bill.getPaymentStatus() + "\n" +
               "==============================\n";
    }
}
