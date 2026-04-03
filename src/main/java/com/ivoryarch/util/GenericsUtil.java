package com.ivoryarch.util;
import java.util.*;

/**
 * WEEK 7: All Generics concepts.
 * No DAO imports — data is passed in from the controller to avoid
 * classloader ordering issues with the JavaFX module runner.
 */
public class GenericsUtil {

    // WEEK 7: Generic class
    public static class Pair<T, U> {
        private T first; private U second;
        public Pair(T first, U second) { this.first = first; this.second = second; }
        public T getFirst() { return first; }
        public U getSecond() { return second; }
        @Override public String toString() { return "(" + first + ", " + second + ")"; }
    }

    // WEEK 7: Generic method
    public static <T> void display(T value) { System.out.println("[Generic Display] " + value); }

    // WEEK 7: Generic array method
    public static <T> void printArray(T[] arr) {
        System.out.print("[Generic Array] ");
        for (T item : arr) System.out.print(item + " ");
        System.out.println();
    }

    // WEEK 7: Bounded generic class
    public static class PriceCalculator<T extends Number> {
        private final List<T> prices = new ArrayList<>();
        public void addPrice(T price) { prices.add(price); }
        public double getTotal() { return prices.stream().mapToDouble(Number::doubleValue).sum(); }
        public double getAverage() { return prices.isEmpty() ? 0 : getTotal() / prices.size(); }
    }

    // WEEK 7: Bounded generic method
    public static <T extends Number> double sumCharges(T a, T b) {
        return a.doubleValue() + b.doubleValue();
    }

    /**
     * Run demo with live data passed in from AdminDashboardController.
     * Returns output as String for display in the logArea.
     */
    public static String runGenericsDemo(List<Integer> roomNumbers,
                                          List<String> roomNames,
                                          List<Double> roomPrices) {
        StringBuilder out = new StringBuilder();
        out.append("=== WEEK 7: GENERICS DEMO (live DB data) ===\n\n");

        // 1. Pair<Integer, String>
        if (!roomNumbers.isEmpty()) {
            Pair<Integer, String> roomInfo = new Pair<>(roomNumbers.get(0), roomNames.get(0));
            String line = "[Pair] Room " + roomInfo;
            System.out.println(line); out.append(line).append("\n");
        }

        // 2. Generic array method
        String[] typeArr = roomNames.stream().distinct().toArray(String[]::new);
        StringBuilder arrLine = new StringBuilder("[Generic Array] ");
        for (String t : typeArr) arrLine.append(t).append("  ");
        System.out.println(arrLine); out.append(arrLine).append("\n");

        // 3. Bounded generic class PriceCalculator<Double>
        PriceCalculator<Double> calc = new PriceCalculator<>();
        roomPrices.forEach(calc::addPrice);
        String totalLine = "[PriceCalculator] Total: Rs." + (int)calc.getTotal()
            + "  |  Average: Rs." + String.format("%.2f", calc.getAverage());
        System.out.println(totalLine); out.append(totalLine).append("\n");

        // 4. Bounded generic method
        if (roomPrices.size() >= 2) {
            double min = roomPrices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double max = roomPrices.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            String sumLine = "[sumCharges] Cheapest (Rs." + (int)min + ") + Priciest (Rs." + (int)max + ") = Rs." + (int)sumCharges(min, max);
            System.out.println(sumLine); out.append(sumLine).append("\n");
        }

        out.append("\n— Generics concepts covered —\n");
        out.append("  1. Generic class     Pair<T,U>\n");
        out.append("  2. Generic method    display(T)\n");
        out.append("  3. Generic array     printArray(T[])\n");
        out.append("  4. Bounded class     PriceCalculator<T extends Number>\n");
        out.append("  5. Bounded method    sumCharges(T a, T b)\n");
        return out.toString();
    }
}
