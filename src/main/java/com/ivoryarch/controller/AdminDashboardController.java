package com.ivoryarch.controller;

import com.ivoryarch.dao.*;
import com.ivoryarch.model.*;
import com.ivoryarch.service.*;
import com.ivoryarch.thread.*;
import com.ivoryarch.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;

public class AdminDashboardController implements Initializable {

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room,Integer> colRoomNo, colCapacity;
    @FXML private TableColumn<Room,String>  colRoomType, colRoomStatus, colWifi, colBreakfast, colFloor;
    @FXML private TableColumn<Room,Double>  colRoomPrice;
    @FXML private TextField roomNumberField, roomPriceField, roomCapacityField, roomFloorField;
    @FXML private ComboBox<RoomType> roomTypeCombo;
    @FXML private CheckBox wifiCheck, breakfastCheck;

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking,Integer> colBkgId, colBkgRoom, colBkgDays;
    @FXML private TableColumn<Booking,String>  colBkgCustomer, colBkgCheckIn, colBkgCheckOut, colBkgStatus;
    @FXML private TableColumn<Booking,Double>  colBkgAmount;

    @FXML private TableView<Bill> billsTable;
    @FXML private TableColumn<Bill,Integer> colBillId, colBillRoom, colBillDays;
    @FXML private TableColumn<Bill,String>  colBillCustomer, colBillStatus, colBillMode;
    @FXML private TableColumn<Bill,Double>  colBillRoom2, colBillGst, colBillDisc, colBillTotal;
    @FXML private TextField billBookingIdField, billFoodField, billLaundryField, billCouponField;
    @FXML private CheckBox lateCheckoutCheck;
    @FXML private ComboBox<String> paymentModeCombo;

    @FXML private PieChart roomStatusPie;
    @FXML private BarChart<String,Number> roomTypeBar;
    @FXML private LineChart<String,Number> revenueLineChart;
    @FXML private Label totalRoomsLabel, occupiedLabel, revenueLabel, checkinsLabel;

    @FXML private TableView<Room> housekeepingTable;
    @FXML private TableColumn<Room,Integer> colHkRoom;
    @FXML private TableColumn<Room,String>  colHkType, colHkStatus, colHkAssigned;

    @FXML private TableView<Customer> customersTable;
    @FXML private TableColumn<Customer,Integer> colCustId;
    @FXML private TableColumn<Customer,String>  colCustName, colCustEmail, colCustPhone;

    @FXML private TextArea logArea;
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingService bookingService = new BookingService();
    private final BillingService billingService = new BillingService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = AuthService.getLoggedInUser();
        if (user != null) welcomeLabel.setText(user.getName());
        roomTypeCombo.setItems(FXCollections.observableArrayList(RoomType.values()));
        paymentModeCombo.setItems(FXCollections.observableArrayList("CASH", "CARD", "UPI"));
        setupRoomTable(); setupBookingTable(); setupBillTable();
        setupHousekeepingTable(); setupCustomerTable();
        loadRooms(); loadBookings(); loadBills(); loadAnalytics();
        loadHousekeeping(); loadCustomers();
    }

    @FXML void handleAddRoom() {
        try {
            if (roomTypeCombo.getValue() == null) { showAlert("Please select a room type.", false); return; }
            int no = Integer.parseInt(roomNumberField.getText().trim());
            double pr = Double.parseDouble(roomPriceField.getText().trim());
            int cap = Integer.parseInt(roomCapacityField.getText().trim());
            DeluxeRoom room = new DeluxeRoom();
            room.setRoomNumber(no); room.setRoomType(roomTypeCombo.getValue());
            room.setPricePerNight(pr); room.setCapacity(cap);
            room.setFloorNumber(roomFloorField.getText().trim());
            room.setWifiAvailable(wifiCheck.isSelected());
            room.setBreakfastIncluded(breakfastCheck.isSelected());
            room.setStatus(RoomStatus.AVAILABLE);
            boolean ok = roomDAO.addRoom(room);
            showAlert(ok ? "Room " + no + " added successfully." : "Room number already exists.", ok);
            if (ok) { roomNumberField.clear(); roomPriceField.clear(); roomCapacityField.clear(); roomFloorField.clear(); }
            loadRooms(); loadHousekeeping();
        } catch (NumberFormatException e) { showAlert("Enter valid numeric values for Room No., Price, and Capacity.", false); }
    }

    @FXML void handleUpdateRoom() {
        Room sel = roomsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a room first.", false); return; }
        try {
            if (!roomPriceField.getText().isEmpty()) sel.setPricePerNight(Double.parseDouble(roomPriceField.getText()));
            if (roomTypeCombo.getValue() != null) sel.setRoomType(roomTypeCombo.getValue());
            sel.setWifiAvailable(wifiCheck.isSelected()); sel.setBreakfastIncluded(breakfastCheck.isSelected());
            showAlert(roomDAO.updateRoom(sel) ? "Room updated." : "Update failed.", roomDAO.updateRoom(sel));
            loadRooms();
        } catch (NumberFormatException e) { showAlert("Invalid price.", false); }
    }

    @FXML void handleDeleteRoom() {
        Room sel = roomsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a room first.", false); return; }
        boolean ok = roomDAO.deleteRoom(sel.getRoomNumber());
        showAlert(ok ? "Room deleted." : "Delete failed.", ok); loadRooms();
    }

    @FXML void handleRefreshRooms() { loadRooms(); }

    @FXML void handleCheckout() {
        Booking sel = bookingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a booking.", false); return; }
        boolean ok = bookingService.checkout(sel.getBookingId(), sel.getRoomNumber());
        showAlert(ok ? "Checkout complete. Cleaning started." : "Checkout failed.", ok);
        loadBookings(); loadRooms();
    }

    @FXML void handleCancelBooking() {
        Booking sel = bookingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a booking.", false); return; }
        boolean ok = bookingService.cancelBooking(sel.getBookingId(), sel.getRoomNumber());
        showAlert(ok ? "Booking cancelled." : "Cancel failed.", ok); loadBookings(); loadRooms();
    }

    @FXML void handleRefreshBookings() { loadBookings(); }

    @FXML void handleGenerateBill() {
        try {
            int bookingId = Integer.parseInt(billBookingIdField.getText());
            Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId() == bookingId).findFirst().orElse(null);
            if (booking == null) { showAlert("Booking not found.", false); return; }
            Room room = roomDAO.getRoomByNumber(booking.getRoomNumber());
            if (room == null) { showAlert("Room not found.", false); return; }
            double food    = parseDouble(billFoodField.getText());
            double laundry = parseDouble(billLaundryField.getText());
            Bill bill = billingService.generateBillWithExtras(booking, room, food, laundry, 0, 0,
                billCouponField.getText(), lateCheckoutCheck.isSelected());
            showAlert("Bill #" + bill.getBillId() + " generated. Total: Rs." +
                String.format("%.2f", bill.getTotalAmount()), true);
            loadBills();
        } catch (NumberFormatException e) { showAlert("Invalid Booking ID.", false); }
    }

    @FXML void handleMarkPaid() {
        Bill sel = billsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a bill.", false); return; }
        String mode = paymentModeCombo.getValue() != null ? paymentModeCombo.getValue() : "CASH";
        boolean ok = billingService.markAsPaid(sel.getBillId(), mode);
        showAlert(ok ? "Payment recorded." : "Update failed.", ok); loadBills();
    }

    @FXML void handleExportInvoice() {
        Bill sel = billsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Select a bill first.", false); return; }
        FileUtil.writeInvoiceBytes(sel);
        showAlert("Invoice saved to data/invoices.txt", true);
    }

    @FXML void handleRefreshAnalytics() { loadAnalytics(); }

    private void loadAnalytics() {
        List<Room> all = roomDAO.getAllRooms();
        long occupied  = all.stream().filter(r -> r.getStatus() == RoomStatus.BOOKED).count();
        long available = all.stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
        long maintenance = all.stream().filter(r -> r.getStatus() == RoomStatus.MAINTENANCE).count();
        double revenue = billingService.getTotalRevenue();
        totalRoomsLabel.setText("Total: " + all.size());
        occupiedLabel.setText("Occupied: " + occupied);
        revenueLabel.setText("Revenue: Rs." + String.format("%.0f", revenue));
        checkinsLabel.setText("Available: " + available);

        roomStatusPie.getData().clear();
        if (available > 0) roomStatusPie.getData().add(new PieChart.Data("Available (" + available + ")", available));
        if (occupied > 0)  roomStatusPie.getData().add(new PieChart.Data("Occupied ("  + occupied  + ")", occupied));
        if (maintenance > 0) roomStatusPie.getData().add(new PieChart.Data("Maintenance (" + maintenance + ")", maintenance));

        roomTypeBar.getData().clear();
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        series.setName("Rooms");
        roomDAO.getRoomTypeStats().forEach((type, count) -> series.getData().add(new XYChart.Data<>(type, count)));
        roomTypeBar.getData().add(series);

        revenueLineChart.getData().clear();
        // Show real monthly revenue from actual bills in DB
        Map<String, Double> monthlyRevenue = billingService.getMonthlyRevenue();
        if (monthlyRevenue.isEmpty()) {
            // No paid bills yet — show empty chart with message
            revenueLineChart.setTitle("Revenue Trend (No paid bills yet)");
        } else {
            revenueLineChart.setTitle("Revenue Trend");
            XYChart.Series<String,Number> revSeries = new XYChart.Series<>();
            revSeries.setName("Revenue (Rs.)");
            String[] monthOrder = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
            for (String m : monthOrder) {
                if (monthlyRevenue.containsKey(m))
                    revSeries.getData().add(new XYChart.Data<>(m, monthlyRevenue.get(m)));
            }
            revenueLineChart.getData().add(revSeries);
        }
    }

    @FXML void handleRefreshHousekeeping() { loadHousekeeping(); }
    @FXML void handleMarkCleaned() {
        Room sel = housekeepingTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        roomDAO.updateRoomStatus(sel.getRoomNumber(), RoomStatus.AVAILABLE);
        loadHousekeeping(); loadRooms();
    }
    @FXML void handleSetMaintenance() {
        Room sel = housekeepingTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        roomDAO.updateRoomStatus(sel.getRoomNumber(), RoomStatus.MAINTENANCE);
        loadHousekeeping(); loadRooms();
    }
    private void loadHousekeeping() {
        housekeepingTable.setItems(FXCollections.observableArrayList(roomDAO.getAllRooms()));
    }

    @FXML void handleRefreshCustomers() { loadCustomers(); }
    private void loadCustomers() {
        customersTable.setItems(FXCollections.observableArrayList(userDAO.getAllCustomers()));
    }

    @FXML void handleSerializeRooms() {
        List<Room> rooms = roomDAO.getAllRooms();
        FileUtil.serializeRooms(rooms);
        StringBuilder sb = new StringBuilder();
        sb.append("=== BACKUP COMPLETE ===\n");
        sb.append("Saved ").append(rooms.size()).append(" rooms to data/backups.dat\n\n");
        for (Room r : rooms) sb.append("  Room ").append(r.getRoomNumber())
            .append(" — ").append(r.getRoomType().getDisplayName())
            .append(" | Rs.").append(r.getPricePerNight())
            .append(" | ").append(r.getStatus()).append("\n");
        logArea.setText(sb.toString());
    }

    @FXML void handleDeserializeRooms() {
        List<Room> rooms = FileUtil.deserializeRooms();
        StringBuilder sb = new StringBuilder();
        if (rooms.isEmpty()) {
            sb.append("No backup found at data/backups.dat\nClick 'Backup Rooms to File' first.");
        } else {
            sb.append("=== RESTORE COMPLETE ===\n");
            sb.append("Loaded ").append(rooms.size()).append(" rooms from data/backups.dat\n\n");
            for (Room r : rooms) sb.append("  Room ").append(r.getRoomNumber())
                .append(" — ").append(r.getRoomType().getDisplayName())
                .append(" | Rs.").append(r.getPricePerNight())
                .append(" | ").append(r.getStatus()).append("\n");
        }
        logArea.setText(sb.toString());
    }

    @FXML void handleViewLog() {
        List<String> lines = FileUtil.readLog();
        if (lines.isEmpty()) {
            logArea.setText("Activity log is empty or not yet created.\nFile path: data/activity.log");
        } else {
            // Show last 100 lines so it doesn't overflow
            int start = Math.max(0, lines.size() - 100);
            logArea.setText(String.join("\n", lines.subList(start, lines.size())));
        }
    }

    @FXML void handleGenericsDemo() {
        List<Room> rooms = roomDAO.getAllRooms();
        List<Integer> nums   = rooms.stream().map(Room::getRoomNumber).collect(java.util.stream.Collectors.toList());
        List<String>  names  = rooms.stream().map(r -> r.getRoomType().getDisplayName()).collect(java.util.stream.Collectors.toList());
        List<Double>  prices = rooms.stream().map(Room::getPricePerNight).collect(java.util.stream.Collectors.toList());
        String output = GenericsUtil.runGenericsDemo(nums, names, prices);
        logArea.setText(output);
    }

    @FXML void handleDeadlockDemo() {
        logArea.setText("=== DEADLOCK DEMO ===\n\n" +
            "Two threads are started:\n" +
            "  Thread 1 locks ROOM_LOCK, waits for BILLING_LOCK\n" +
            "  Thread 2 locks BILLING_LOCK, waits for ROOM_LOCK\n\n" +
            "Both threads are now waiting forever — this is a deadlock.\n" +
            "In production, you fix this by always acquiring locks in the same order.\n\n" +
            "Check the terminal for the thread output.");
        DeadlockDemo.simulateDeadlock();
    }

    @FXML void handleLogout() {
    AuthService.logout();
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/com/ivoryarch/view/login.fxml"));
        Scene scene = new Scene(root, 920, 620);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm()); // ← this line was missing
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.setMaximized(false);
    } catch (Exception e) { e.printStackTrace(); }
}

    private void setupRoomTable() {
        colRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colFloor.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        colRoomStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));
        colWifi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isWifiAvailable() ? "Yes" : "No"));
        colBreakfast.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isBreakfastIncluded() ? "Yes" : "No"));
    }

    private void setupBookingTable() {
        colBkgId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colBkgCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colBkgRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBkgCheckIn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        colBkgCheckOut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));
        colBkgDays.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        colBkgStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBkgAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void setupBillTable() {
        colBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colBillCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colBillRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBillDays.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        colBillRoom2.setCellValueFactory(new PropertyValueFactory<>("roomCharge"));
        colBillGst.setCellValueFactory(new PropertyValueFactory<>("gstAmount"));
        colBillDisc.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colBillTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colBillStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colBillMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));
    }

    private void setupHousekeepingTable() {
        colHkRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colHkType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));
        colHkStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));
        colHkAssigned.setCellValueFactory(c -> new SimpleStringProperty("Housekeeping Staff"));
    }

    private void setupCustomerTable() {
        colCustId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colCustName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCustPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void loadRooms()    { roomsTable.setItems(FXCollections.observableArrayList(roomDAO.getAllRooms())); }
    private void loadBookings() { bookingsTable.setItems(FXCollections.observableArrayList(bookingService.getAllBookings())); }
    private void loadBills()    { billsTable.setItems(FXCollections.observableArrayList(billingService.getAllBills())); }

    private double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0; } }

    private void showAlert(String msg, boolean success) {
        NotificationTask.notify(success ? "Success" : "Error", msg,
            success ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.ERROR);
    }
}
