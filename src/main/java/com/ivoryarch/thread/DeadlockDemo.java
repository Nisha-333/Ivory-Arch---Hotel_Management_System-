package com.ivoryarch.thread;

// WEEK 4: Deadlock demonstration
public class DeadlockDemo {
    private static final Object ROOM_LOCK    = new Object();
    private static final Object BILLING_LOCK = new Object();

    public static void simulateDeadlock() {
        Thread t1 = new Thread(() -> {
            synchronized (ROOM_LOCK) {
                System.out.println("T1 holds ROOM_LOCK, waiting for BILLING_LOCK...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (BILLING_LOCK) { System.out.println("T1 acquired both locks"); }
            }
        }, "DeadlockThread-1");

        Thread t2 = new Thread(() -> {
            synchronized (BILLING_LOCK) {
                System.out.println("T2 holds BILLING_LOCK, waiting for ROOM_LOCK...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (ROOM_LOCK) { System.out.println("T2 acquired both locks"); }
            }
        }, "DeadlockThread-2");

        System.out.println("=== DEADLOCK DEMO START ===");
        t1.start(); t2.start();
    }
}
