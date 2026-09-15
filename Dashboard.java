import java.util.Map;

public class Dashboard {

    private final RiskScoreCalculator riskCalculator = new RiskScoreCalculator();
    private final RecessionComparator recessionComparator = new RecessionComparator();
    private final BuyingOutlookAdvisor buyingAdvisor = new BuyingOutlookAdvisor();

    public void render(EconomicSnapshot s, AlertService alerts) {
        RiskScoreCalculator.RiskResult risk = riskCalculator.calculate(s);
        BuyingOutlookAdvisor.Outlook outlook = buyingAdvisor.evaluate(s);
        String closestRecession = recessionComparator.findClosestMatch(s);

        line();
        System.out.println("🇺🇸 ECONOMIC WATCH");
        System.out.println(s.getYear() + " (data as of " + s.getRecordedOn() + ")");
        System.out.println();
        System.out.println("📍 PRIMARY LOCATION");
        System.out.println(s.getLocation() + " ⭐");
        line();

        System.out.println("🚨 ECONOMIC RISK (indicator, not a prediction)");
        System.out.printf("       %s %d / 100%n", risk.category.icon, risk.score);
        System.out.println(riskBar(risk.score));
        System.out.println("Category: " + risk.category.label);
        line();

        System.out.println("👷 JOBS");
        System.out.printf("Employed              %,d%n", s.getEmployedCount());
        System.out.printf("Unemployed            %,d%n", s.getUnemployedCount());
        System.out.printf("Unemployment Rate     %.1f%%%n", s.getUnemploymentRatePct());
        System.out.println();
        System.out.println("🏠 HOUSING");
        System.out.printf("Home Price Index      %.1f%n", s.getHomePriceIndex());
        System.out.printf("Inventory Index       %.1f%n", s.getHousingInventoryIndex());
        System.out.printf("Mortgage Rate         %.2f%%%n", s.getMortgageRatePct());
        System.out.printf("Foreclosure Rate      %.2f%%%n", s.getForeclosureRatePct());
        System.out.println();
        System.out.println("📈 ECONOMY");
        System.out.printf("GDP Growth            %.1f%%%n", s.getGdpGrowthPct());
        System.out.printf("Inflation             %.1f%%%n", s.getInflationPct());
        System.out.printf("Fed Funds Rate        %.2f%%%n", s.getFedFundsRatePct());
        System.out.printf("Consumer Debt Index   %.1f%n", s.getConsumerDebtIndex());
        System.out.printf("Credit Stress Index   %.1f%n", s.getCreditStressIndex());
        line();

        System.out.println("📚 RECESSION COMPARISON (indicator only)");
        for (Map.Entry<String, RecessionComparator.Benchmark> e : recessionComparator.getBenchmarks().entrySet()) {
            String icon = recessionComparator.severityIcon(s, e.getValue());
            System.out.printf("%-14s%s%n", e.getKey(), icon);
        }
        System.out.println();
        System.out.println("Closest historical match: " + closestRecession);
        line();

        System.out.println("🏠 BUYING OUTLOOK (guidance, not a guarantee)");
        System.out.println(outlook.icon + " " + outlook.message);
        line();

        System.out.println("🔮 FUTURE (illustrative outlook only — not a forecast guarantee)");
        int currentYear = s.getYear();
        for (int y = currentYear; y <= currentYear + 4; y++) {
            String tag = (y == currentYear) ? "ACTUAL" : "OUTLOOK";
            System.out.println(y + "   " + tag);
        }
        line();

        System.out.println("🔔 ALERTS");
        System.out.println((alerts.isSoundEnabled() ? "☑" : "☐") + " Sound");
        System.out.println((alerts.isEmailEnabled() ? "☑" : "☐") + " Email  (" + alerts.getEmailAddress() + ")");
        System.out.println((alerts.isSmsEnabled() ? "☑" : "☐") + " SMS");
        line();
    }

    private String riskBar(int score) {
        int totalSlots = 20;
        int filled = (int) Math.round(score / 100.0 * totalSlots);
        StringBuilder sb = new StringBuilder("NORMAL ");
        for (int i = 0; i < totalSlots; i++) {
            sb.append(i == filled ? "●" : "─");
        }
        sb.append(" DEPRESSION");
        return sb.toString();
    }

    private void line() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
