import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds representative (approximate, illustrative) peak-severity economic
 * conditions for four historical U.S. recessions, and compares a current
 * snapshot against them to find the closest historical match.
 *
 * These benchmark numbers are simplified reference points for comparison
 * purposes, not precise historical statistics for any single city.
 */
public class RecessionComparator {

    public static class Benchmark {
        public final String label;
        public final double unemploymentRatePct;
        public final double inflationPct;
        public final double fedFundsRatePct;
        public final double creditStressIndex;
        public final double gdpGrowthPct;

        public Benchmark(String label, double unemploymentRatePct, double inflationPct,
                          double fedFundsRatePct, double creditStressIndex, double gdpGrowthPct) {
            this.label = label;
            this.unemploymentRatePct = unemploymentRatePct;
            this.inflationPct = inflationPct;
            this.fedFundsRatePct = fedFundsRatePct;
            this.creditStressIndex = creditStressIndex;
            this.gdpGrowthPct = gdpGrowthPct;
        }
    }

    private final Map<String, Benchmark> benchmarks = new LinkedHashMap<>();

    public RecessionComparator() {
        // Illustrative reference points, not precise historical data.
        benchmarks.put("1990-91", new Benchmark("1990-91", 7.8, 5.4, 8.0, 115, -1.4));
        benchmarks.put("2001", new Benchmark("2001", 5.7, 2.8, 3.5, 108, 1.0));
        benchmarks.put("2007-09", new Benchmark("2007-09", 9.9, 0.1, 0.25, 160, -2.8));
        benchmarks.put("2020", new Benchmark("2020", 14.7, 1.2, 0.25, 130, -3.4));
    }

    public Map<String, Benchmark> getBenchmarks() {
        return benchmarks;
    }

    /** Returns the label of the historical recession closest to current conditions. */
    public String findClosestMatch(EconomicSnapshot s) {
        String closestLabel = "None — conditions are mild";
        double closestDistance = Double.MAX_VALUE;

        for (Benchmark b : benchmarks.values()) {
            double distance = distance(s, b);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestLabel = b.label;
            }
        }
        // If even the closest historical match is far away, current conditions
        // don't really resemble any past recession.
        if (closestDistance > 16) {
            return "None — current conditions don't closely resemble a past recession";
        }
        return closestLabel;
    }

    private double distance(EconomicSnapshot s, Benchmark b) {
        double du = s.getUnemploymentRatePct() - b.unemploymentRatePct;
        double di = s.getInflationPct() - b.inflationPct;
        double dr = s.getFedFundsRatePct() - b.fedFundsRatePct;
        double dc = (s.getCreditStressIndex() - b.creditStressIndex) / 10.0;
        double dg = s.getGdpGrowthPct() - b.gdpGrowthPct;
        return Math.sqrt(du * du + di * di + dr * dr + dc * dc + dg * dg);
    }

    /** Simple severity rating (for display) of how a snapshot compares to a benchmark. */
    public String severityIcon(EconomicSnapshot s, Benchmark b) {
        double distance = distance(s, b);
        if (distance < 6) return "🔴";  // very close to this recession's severity
        if (distance < 12) return "🟡"; // somewhat comparable
        return "🟢"; // not comparable, current conditions much milder
    }
}
