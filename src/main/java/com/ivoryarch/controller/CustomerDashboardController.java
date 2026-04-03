package com.ivoryarch.controller;

import com.ivoryarch.dao.*;
import com.ivoryarch.model.*;
import com.ivoryarch.service.*;
import com.ivoryarch.thread.NotificationTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class CustomerDashboardController implements Initializable {

    // Browse
    @FXML private Label welcomeLabel;
    @FXML private TableView<Room> availableRoomsTable;
    @FXML private TableColumn<Room,Integer> colAvailRoomNo, colAvailCap;
    @FXML private TableColumn<Room,String>  colAvailType, colAvailFloor, colAvailWifi, colAvailBreakfast, colAvailStatus;
    @FXML private TableColumn<Room,Double>  colAvailPrice;
    @FXML private ComboBox<RoomType> filterTypeCombo;

    // Book
    @FXML private TextField bookRoomNoField, guestsField, specialReqField;
    @FXML private DatePicker checkInPicker, checkOutPicker;
    @FXML private Label bookingStatusLabel;

    // My Bookings
    @FXML private TableView<Booking> myBookingsTable;
    @FXML private TableColumn<Booking,Integer> colMyBkgId, colMyBkgRoom, colMyBkgDays;
    @FXML private TableColumn<Booking,String>  colMyBkgCheckIn, colMyBkgCheckOut, colMyBkgStatus;
    @FXML private TableColumn<Booking,Double>  colMyBkgAmount;

    // My Bills
    @FXML private TableView<Bill> myBillsTable;
    @FXML private TableColumn<Bill,Integer> colMyBillId, colMyBillRoom, colMyBillDays;
    @FXML private TableColumn<Bill,Double>  colMyBillTotal, colMyBillRoomCharge, colMyBillGst, colMyBillDiscount;
    @FXML private TableColumn<Bill,String>  colMyBillStatus, colMyBillMode;
    @FXML private ComboBox<String> custPaymentModeCombo;
    @FXML private TextArea billBreakdownArea;

    // Recommend
    @FXML private TextField recBudgetField, recGuestsField, recNightsField;
    @FXML private CheckBox recWifiCheck, recBreakfastCheck;
    @FXML private TextArea recommendationArea;

    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingService bookingService = new BookingService();
    private final BillingService billingService = new BillingService();
    private final RecommendationService recService = new RecommendationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = AuthService.getLoggedInUser();
        if (user != null) welcomeLabel.setText(user.getName());
        filterTypeCombo.setItems(FXCollections.observableArrayList(RoomType.values()));
        custPaymentModeCombo.setItems(FXCollections.observableArrayList("CASH", "CARD", "UPI"));
        custPaymentModeCombo.setValue("CASH");
        setupAvailableTable(); setupMyBookingsTable(); setupMyBillsTable();
        loadAvailableRooms(); loadMyBookings(); loadMyBills();

        // Auto-show breakdown when bill is selected
        myBillsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> { if (sel != null) showBreakdown(sel); });
    }

    // ── BROWSE ────────────────────────────────────────────────────
    @FXML void handleShowAllRooms() { loadAvailableRooms(); }
    @FXML void handleRefreshAvailableRooms() { loadAvailableRooms(); }

    @FXML void handleShowAvailableOnly() {
        availableRoomsTable.setItems(FXCollections.observableArrayList(roomDAO.getAvailableRooms()));
    }

    @FXML void handleFilterRooms() {
        RoomType type = filterTypeCombo.getValue();
        if (type == null) { loadAvailableRooms(); return; }
        availableRoomsTable.setItems(FXCollections.observableArrayList(roomDAO.getRoomsByType(type)));
    }

    // ── BOOK ──────────────────────────────────────────────────────
    @FXML void handleBookRoom() {
        try {
            int roomNo     = Integer.parseInt(bookRoomNoField.getText().trim());
            LocalDate in   = checkInPicker.getValue();
            LocalDate out  = checkOutPicker.getValue();
            if (in == null || out == null || !out.isAfter(in)) {
                setBookStatus("Please select valid check-in and check-out dates.", false); return;
            }
            int guests = Integer.parseInt(guestsField.getText().isEmpty() ? "1" : guestsField.getText().trim());
            User user  = AuthService.getLoggedInUser();
            Room room  = roomDAO.getRoomByNumber(roomNo);
            if (room == null) { setBookStatus("Room " + roomNo + " does not exist.", false); return; }
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                setBookStatus("Room " + roomNo + " is not available right now.", false); return;
            }
            if (room.getCapacity() < guests) {
                setBookStatus("Room capacity is " + room.getCapacity() + ". Choose a room with higher capacity.", false); return;
            }
            Booking b = new Booking(user.getUserId(), room.getRoomId(), in, out, guests);
            b.setCustomerName(user.getName()); b.setRoomNumber(roomNo);
            b.setSpecialRequests(specialReqField.getText());
            b.setTotalAmount(room.calculateBillWithTax(b.getNumberOfDays()));
            boolean booked = bookingService.bookRoom(roomNo, b.getNumberOfDays());
            if (!booked) { setBookStatus("Booking failed. Room is no longer available.", false); return; }
            int id = bookingService.createBooking(b);
            setBookStatus("Reservation confirmed!  Booking ID: " + id +
                "  |  Total (with 18% GST): Rs." + String.format("%.2f", b.getTotalAmount()), true);
            NotificationTask.notify("Booking Confirmed",
                "Room " + roomNo + " reserved for " + b.getNumberOfDays() + " night(s).",
                Alert.AlertType.INFORMATION);
            loadAvailableRooms(); loadMyBookings();
        } catch (NumberFormatException e) { setBookStatus("Please enter valid numbers.", false); }
    }

    // ── MY BOOKINGS ───────────────────────────────────────────────
    @FXML void handleRefreshMyBookings() { loadMyBookings(); }
    @FXML void handleCancelMyBooking() {
        Booking sel = myBookingsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { return; }
        if ("CHECKED_OUT".equals(sel.getStatus()) || "CANCELLED".equals(sel.getStatus())) {
            NotificationTask.notify("Cannot Cancel", "This booking is already " + sel.getStatus() + ".", Alert.AlertType.WARNING);
            return;
        }
        bookingService.cancelBooking(sel.getBookingId(), sel.getRoomNumber());
        NotificationTask.notify("Cancelled", "Booking #" + sel.getBookingId() + " cancelled.", Alert.AlertType.INFORMATION);
        loadMyBookings(); loadAvailableRooms();
    }

    // ── MY BILLS ──────────────────────────────────────────────────
    @FXML void handleRefreshBills() { loadMyBills(); billBreakdownArea.clear(); }

    @FXML void handlePayBill() {
        Bill sel = myBillsTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            NotificationTask.notify("No Bill Selected", "Please select a bill to pay.", Alert.AlertType.WARNING); return;
        }
        if ("PAID".equals(sel.getPaymentStatus())) {
            NotificationTask.notify("Already Paid", "This bill has already been paid.", Alert.AlertType.INFORMATION); return;
        }
        String mode = custPaymentModeCombo.getValue() != null ? custPaymentModeCombo.getValue() : "CASH";
        boolean ok  = billingService.markAsPaid(sel.getBillId(), mode);
        if (ok) {
            sel.setPaymentStatus("PAID"); sel.setPaymentMode(mode);
            showBreakdown(sel);
            NotificationTask.notify("Payment Successful",
                "Bill #" + sel.getBillId() + " paid via " + mode + ".", Alert.AlertType.INFORMATION);
        } else {
            NotificationTask.notify("Error", "Payment update failed.", Alert.AlertType.ERROR);
        }
        loadMyBills();
    }

    private void showBreakdown(Bill b) {
        double room     = b.getRoomCharge()     != null ? b.getRoomCharge()     : 0;
        double food     = b.getFoodCharge()     != null ? b.getFoodCharge()     : 0;
        double laundry  = b.getLaundryCharge()  != null ? b.getLaundryCharge()  : 0;
        double service  = b.getServiceCharge()  != null ? b.getServiceCharge()  : 0;
        double extra    = b.getExtraBedCharge() != null ? b.getExtraBedCharge() : 0;
        double discount = b.getDiscountAmount() != null ? b.getDiscountAmount() : 0;
        double gst      = b.getGstAmount()      != null ? b.getGstAmount()      : 0;
        double late     = b.getLateCheckoutFee();
        double total    = b.getTotalAmount()    != null ? b.getTotalAmount()    : 0;

        String line = "─────────────────────────────────\n";
        StringBuilder sb = new StringBuilder();
        sb.append("  IVORY ARCH — BILL #").append(b.getBillId()).append("\n");
        sb.append(line);
        sb.append(String.format("  %-22s %s%n", "Guest:", b.getCustomerName()));
        sb.append(String.format("  %-22s %s%n", "Room:", b.getRoomNumber()));
        sb.append(String.format("  %-22s %s night(s)%n", "Duration:", b.getNumberOfDays()));
        sb.append(line);
        sb.append(String.format("  %-22s Rs. %,.2f%n", "Room Charge:", room));
        if (food     > 0) sb.append(String.format("  %-22s Rs. %,.2f%n", "Food & Beverages:", food));
        if (laundry  > 0) sb.append(String.format("  %-22s Rs. %,.2f%n", "Laundry:", laundry));
        if (service  > 0) sb.append(String.format("  %-22s Rs. %,.2f%n", "Service Charge:", service));
        if (extra    > 0) sb.append(String.format("  %-22s Rs. %,.2f%n", "Extra Bed:", extra));
        if (late     > 0) sb.append(String.format("  %-22s Rs. %,.2f%n", "Late Checkout:", late));
        if (discount > 0) sb.append(String.format("  %-22s - Rs. %,.2f%n", "Discount:", discount));
        sb.append(String.format("  %-22s Rs. %,.2f%n", "GST (18%):", gst));
        sb.append(line);
        sb.append(String.format("  %-22s Rs. %,.2f%n", "TOTAL:", total));
        sb.append(line);
        sb.append(String.format("  %-22s %s%n", "Payment Status:", b.getPaymentStatus()));
        if (b.getPaymentMode() != null && !b.getPaymentMode().isEmpty())
            sb.append(String.format("  %-22s %s%n", "Paid Via:", b.getPaymentMode()));
        if (b.getCouponCode() != null && !b.getCouponCode().isEmpty())
            sb.append(String.format("  %-22s %s%n", "Coupon Used:", b.getCouponCode()));
        if ("PENDING".equals(b.getPaymentStatus()))
            sb.append("\n  Select a payment mode above and click 'Pay Selected Bill'.");

        billBreakdownArea.setText(sb.toString());
    }

    // ── RECOMMEND ─────────────────────────────────────────────────
    @FXML void handleGetRecommendations() {
        try {
            double budget  = Double.parseDouble(recBudgetField.getText().trim());
            int    guests  = Integer.parseInt(recGuestsField.getText().trim());
            int    nights  = Integer.parseInt(recNightsField.getText().trim());
            if (budget <= 0 || guests <= 0 || nights <= 0) {
                recommendationArea.setText("Please enter values greater than 0."); return;
            }
            RecommendationService.RecommendResult result = recService.recommend(
                budget, guests, nights, recWifiCheck.isSelected(), recBreakfastCheck.isSelected());
            StringBuilder sb = new StringBuilder();
            if (result.message != null) sb.append(result.message).append("\n\n");
            if (result.rooms.isEmpty()) {
                sb.append("No rooms available within Rs.").append(String.format("%,.0f", budget))
                  .append(" for ").append(nights).append(" night(s).\n");
                sb.append("Try a higher budget, fewer nights, or fewer guests.");
            } else {
                if (result.message == null)
                    sb.append("Rooms within Rs.").append(String.format("%,.0f", budget))
                      .append(" total (").append(nights).append(" night(s), incl. 18% GST):\n\n");
                for (Room r : result.rooms) {
                    double totalWithGst = r.getPricePerNight() * nights * 1.18;
                    sb.append("  Room ").append(r.getRoomNumber())
                      .append("  —  ").append(r.getRoomType().getDisplayName()).append("\n");
                    sb.append("    Price  : Rs.").append(String.format("%,.0f", r.getPricePerNight())).append(" / night\n");
                    sb.append("    Total  : Rs.").append(String.format("%,.0f", totalWithGst)).append(" (incl. GST)\n");
                    sb.append("    Guests : up to ").append(r.getCapacity()).append("  |  Floor ").append(r.getFloorNumber()).append("\n");
                    List<String> am = new ArrayList<>();
                    if (r.isWifiAvailable()) am.add("WiFi");
                    if (r.isBreakfastIncluded()) am.add("Breakfast");
                    if (r.isParkingAvailable()) am.add("Parking");
                    sb.append("    Amenities: ").append(am.isEmpty() ? "None" : String.join(", ", am)).append("\n\n");
                }
            }
            recommendationArea.setText(sb.toString());
        } catch (NumberFormatException e) { recommendationArea.setText("Please enter valid numbers in all fields."); }
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

    // ── LOADERS ───────────────────────────────────────────────────
    private void loadAvailableRooms() {
        availableRoomsTable.setItems(FXCollections.observableArrayList(roomDAO.getAllRooms()));
    }
    private void loadMyBookings() {
        User user = AuthService.getLoggedInUser();
        if (user != null) myBookingsTable.setItems(
            FXCollections.observableArrayList(bookingService.getBookingsByCustomer(user.getUserId())));
    }
    private void loadMyBills() {
        User user = AuthService.getLoggedInUser();
        if (user != null) myBillsTable.setItems(
            FXCollections.observableArrayList(billingService.getBillsByCustomer(user.getUserId())));
    }

    // ── TABLE SETUP ───────────────────────────────────────────────
    private void setupAvailableTable() {
        colAvailRoomNo.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colAvailType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoomType().getDisplayName()));
        colAvailPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        colAvailCap.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colAvailFloor.setCellValueFactory(new PropertyValueFactory<>("floorNumber"));
        colAvailWifi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isWifiAvailable() ? "Yes" : "No"));
        colAvailBreakfast.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isBreakfastIncluded() ? "Yes" : "No"));
        colAvailStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().toString()));

        // Color-code status column
        colAvailStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setText(empty ? null : item);
                if (!empty) {
                    if ("Available".equals(item)) setStyle("-fx-text-fill:#2d6a4f; -fx-font-weight:bold;");
                    else if ("Booked".equals(item)) setStyle("-fx-text-fill:#9b2335; -fx-font-weight:bold;");
                    else setStyle("-fx-text-fill:#7a6010; -fx-font-weight:bold;");
                }
            }
        });
    }

    private void setupMyBookingsTable() {
        colMyBkgId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colMyBkgRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colMyBkgCheckIn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().toString()));
        colMyBkgCheckOut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().toString()));
        colMyBkgDays.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        colMyBkgStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colMyBkgAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void setupMyBillsTable() {
        colMyBillId.setCellValueFactory(new PropertyValueFactory<>("billId"));
        colMyBillRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colMyBillDays.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        colMyBillRoomCharge.setCellValueFactory(new PropertyValueFactory<>("roomCharge"));
        colMyBillGst.setCellValueFactory(new PropertyValueFactory<>("gstAmount"));
        colMyBillDiscount.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colMyBillTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colMyBillStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colMyBillMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));

        // Color-code payment status
        colMyBillStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setText(empty ? null : item);
                if (!empty) {
                    if ("PAID".equals(item)) setStyle("-fx-text-fill:#2d6a4f; -fx-font-weight:bold;");
                    else setStyle("-fx-text-fill:#9b2335; -fx-font-weight:bold;");
                }
            }
        });
    }

    private void setBookStatus(String msg, boolean success) {
        bookingStatusLabel.setText(msg);
        bookingStatusLabel.setStyle(success ? "-fx-text-fill:#2d6a4f;" : "-fx-text-fill:#c0392b;");
    }
}
