package com.ivoryarch.service;
import com.ivoryarch.dao.BillDAO;
import com.ivoryarch.model.*;
import com.ivoryarch.util.*;
import java.util.*;

public class BillingService {
    private final BillDAO billDAO = new BillDAO();
    private static final Map<String, Double> COUPONS = new HashMap<>();
    static {
        COUPONS.put("SAVE10", 0.10);
        COUPONS.put("ARCH20", 0.20);
        COUPONS.put("FIRST15", 0.15);
    }

    public Bill generateBill(Booking booking, Room room) {
        Bill bill = new Bill(booking.getBookingId(), booking.getCustomerId(),
                            room.getRoomNumber(), booking.getNumberOfDays(), room.getPricePerNight());
        bill.setCustomerName(booking.getCustomerName());
        bill.calculateTotal();
        int id = billDAO.saveBill(bill);
        bill.setBillId(id);
        FileUtil.writeInvoiceBytes(bill);
        FileUtil.writeLog("Bill generated: #" + id);
        return bill;
    }

    public Bill generateBillWithExtras(Booking booking, Room room,
            double food, double laundry, double service, double extraBed,
            String coupon, boolean lateCheckout) {
        Bill bill = new Bill(booking.getBookingId(), booking.getCustomerId(),
                            room.getRoomNumber(), booking.getNumberOfDays(), room.getPricePerNight());
        bill.setCustomerName(booking.getCustomerName());
        bill.setFoodCharge(food); bill.setLaundryCharge(laundry);
        bill.setServiceCharge(service); bill.setExtraBedCharge(extraBed);
        bill.setLateCheckoutFee(lateCheckout ? 500.0 : 0.0);
        bill.setCouponCode(coupon);
        if (coupon != null && COUPONS.containsKey(coupon.toUpperCase())) {
            double subtotal = bill.getRoomCharge() != null ? bill.getRoomCharge() : 0;
            bill.setDiscountAmount(subtotal * COUPONS.get(coupon.toUpperCase()));
        }
        bill.calculateTotal();
        int id = billDAO.saveBill(bill);
        bill.setBillId(id);
        FileUtil.writeInvoiceBytes(bill);
        return bill;
    }

    public boolean markAsPaid(int billId, String mode) {
        return billDAO.updatePaymentStatus(billId, "PAID", mode);
    }

    public List<Bill> getAllBills() { return billDAO.getAllBills(); }
    public List<Bill> getBillsByCustomer(int customerId) { return billDAO.getBillsByCustomer(customerId); }
    public Bill getBillByBookingId(int bookingId) { return billDAO.getBillByBookingId(bookingId); }
    public double getTotalRevenue() { return billDAO.getTotalRevenue(); }
    public java.util.Map<String, Double> getMonthlyRevenue() { return billDAO.getMonthlyRevenue(); }
}
