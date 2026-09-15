/**
 * Converts an EconomicSnapshot into a 0-100 risk score and a category.
 *
 * IMPORTANT: this score is an INDICATOR based on weighted historical
 * relationships between economic stress signals — it is not a prediction
 * or a guarantee of future conditions. Treat it as one input among many.
 */
public class RiskScoreCalculator {

    public enum RiskCategory {
        NORMAL("🟢", "NORMAL"),
        ELEVATED("🟡", "ELEVATED"),
        SEVERE("🔴", "SEVERE"),
        DEPRESSION_LEVEL("⚫", "DEPRESSION-LEVEL");

        public final String icon;
        public final String label;

        RiskCategory(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    public static class RiskResult {
        public final int score; // 0-100
        public final RiskCategory category;

        public RiskResult(int score, RiskCategory category) {
            this.score = score;
            this.category = category;
        }
    }

    public RiskResult calculate(EconomicSnapshot s) {
        double score = 0;

        // Unemployment: baseline ~4%, each point above adds risk
        score += clamp((s.getUnemploymentRatePct() - 4.0) * 6, 0, 30);

        // Inflation: baseline ~2%, each point above adds risk
        score += clamp((s.getInflationPct() - 2.0) * 4, 0, 20);

        // Interest rates: high rates add moderate risk (tightening pressure)
        score += clamp((s.getFedFundsRatePct() - 2.0) * 2.5, 0, 15);

        // Consumer debt index: 100 = baseline, above adds risk
        score += clamp((s.getConsumerDebtIndex() - 100) * 0.3, 0, 15);

        // Credit/banking stress index: 100 = baseline, above adds risk heavily
        score += clamp((s.getCreditStressIndex() - 100) * 0.4, 0, 20);

        // Foreclosure rate: each point adds meaningful risk
        score += clamp(s.getForeclosureRatePct() * 5, 0, 15);

        // GDP growth: negative growth adds risk, positive growth reduces slightly
        score += clamp(-s.getGdpGrowthPct() * 5, -5, 15);

        int finalScore = (int) Math.round(clamp(score, 0, 100));
        return new RiskResult(finalScore, categoryFor(finalScore));
    }

    private RiskCategory categoryFor(int score) {
        if (score < 35) return RiskCategory.NORMAL;
        if (score < 60) return RiskCategory.ELEVATED;
        if (score < 85) return RiskCategory.SEVERE;
        return RiskCategory.DEPRESSION_LEVEL;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
