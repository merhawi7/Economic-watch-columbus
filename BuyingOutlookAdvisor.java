/**
 * Produces a "should I buy a home right now" indicator based on housing
 * metrics. This is explicitly framed as guidance, not a guarantee — markets
 * are unpredictable and this is only one signal among many a buyer should
 * consider.
 */
public class BuyingOutlookAdvisor {

    public enum Outlook {
        GOOD("🟢", "NOW MAY BE A GOOD TIME TO BUY"),
        MIXED("🟡", "CONDITIONS ARE MIXED — WEIGH YOUR OPTIONS CAREFULLY"),
        UNFAVORABLE("🔴", "CONDITIONS CURRENTLY LOOK LESS FAVORABLE FOR BUYING");

        public final String icon;
        public final String message;

        Outlook(String icon, String message) {
            this.icon = icon;
            this.message = message;
        }
    }

    public Outlook evaluate(EconomicSnapshot s) {
        int favorablePoints = 0;

        // Lower mortgage rates favor buying
        if (s.getMortgageRatePct() < 6.0) favorablePoints++;
        else if (s.getMortgageRatePct() > 7.5) favorablePoints--;

        // Higher inventory (more homes available) favors buyers
        if (s.getHousingInventoryIndex() > 100) favorablePoints++;
        else if (s.getHousingInventoryIndex() < 85) favorablePoints--;

        // Home prices near or below baseline favor buyers
        if (s.getHomePriceIndex() < 105) favorablePoints++;
        else if (s.getHomePriceIndex() > 130) favorablePoints--;

        // High foreclosure rates can signal a distressed, riskier market
        if (s.getForeclosureRatePct() > 2.0) favorablePoints--;

        if (favorablePoints >= 2) return Outlook.GOOD;
        if (favorablePoints <= -2) return Outlook.UNFAVORABLE;
        return Outlook.MIXED;
    }
}
