import java.time.LocalDate;

/**
 * A single point-in-time snapshot of economic conditions for one location.
 * One EconomicSnapshot is stored per year (or per update) so history is
 * never overwritten — see DataStore.
 */
public class EconomicSnapshot {

    private final int year;
    private final LocalDate recordedOn;
    private final String location;

    // Jobs
    private final double unemploymentRatePct;
    private final int employedCount;
    private final int unemployedCount;

    // Economy
    private final double gdpGrowthPct;        // YoY % change
    private final double inflationPct;        // YoY CPI % change
    private final double fedFundsRatePct;      // interest rate
    private final double consumerDebtIndex;    // normalized index, 100 = baseline
    private final double creditStressIndex;    // normalized index, 100 = baseline

    // Housing
    private final double homePriceIndex;       // normalized index, 100 = baseline
    private final double housingInventoryIndex; // months of supply, normalized
    private final double mortgageRatePct;
    private final double foreclosureRatePct;

    public EconomicSnapshot(Builder b) {
        this.year = b.year;
        this.recordedOn = b.recordedOn;
        this.location = b.location;
        this.unemploymentRatePct = b.unemploymentRatePct;
        this.employedCount = b.employedCount;
        this.unemployedCount = b.unemployedCount;
        this.gdpGrowthPct = b.gdpGrowthPct;
        this.inflationPct = b.inflationPct;
        this.fedFundsRatePct = b.fedFundsRatePct;
        this.consumerDebtIndex = b.consumerDebtIndex;
        this.creditStressIndex = b.creditStressIndex;
        this.homePriceIndex = b.homePriceIndex;
        this.housingInventoryIndex = b.housingInventoryIndex;
        this.mortgageRatePct = b.mortgageRatePct;
        this.foreclosureRatePct = b.foreclosureRatePct;
    }

    public int getYear() { return year; }
    public LocalDate getRecordedOn() { return recordedOn; }
    public String getLocation() { return location; }
    public double getUnemploymentRatePct() { return unemploymentRatePct; }
    public int getEmployedCount() { return employedCount; }
    public int getUnemployedCount() { return unemployedCount; }
    public double getGdpGrowthPct() { return gdpGrowthPct; }
    public double getInflationPct() { return inflationPct; }
    public double getFedFundsRatePct() { return fedFundsRatePct; }
    public double getConsumerDebtIndex() { return consumerDebtIndex; }
    public double getCreditStressIndex() { return creditStressIndex; }
    public double getHomePriceIndex() { return homePriceIndex; }
    public double getHousingInventoryIndex() { return housingInventoryIndex; }
    public double getMortgageRatePct() { return mortgageRatePct; }
    public double getForeclosureRatePct() { return foreclosureRatePct; }

    /** Serialize to a single pipe-delimited line for simple file storage. */
    public String toDataLine() {
        return String.join("|",
                String.valueOf(year),
                recordedOn.toString(),
                location,
                String.valueOf(unemploymentRatePct),
                String.valueOf(employedCount),
                String.valueOf(unemployedCount),
                String.valueOf(gdpGrowthPct),
                String.valueOf(inflationPct),
                String.valueOf(fedFundsRatePct),
                String.valueOf(consumerDebtIndex),
                String.valueOf(creditStressIndex),
                String.valueOf(homePriceIndex),
                String.valueOf(housingInventoryIndex),
                String.valueOf(mortgageRatePct),
                String.valueOf(foreclosureRatePct)
        );
    }

    public static EconomicSnapshot fromDataLine(String line) {
        String[] p = line.split("\\|", -1);
        return new Builder()
                .year(Integer.parseInt(p[0]))
                .recordedOn(LocalDate.parse(p[1]))
                .location(p[2])
                .unemploymentRatePct(Double.parseDouble(p[3]))
                .employedCount(Integer.parseInt(p[4]))
                .unemployedCount(Integer.parseInt(p[5]))
                .gdpGrowthPct(Double.parseDouble(p[6]))
                .inflationPct(Double.parseDouble(p[7]))
                .fedFundsRatePct(Double.parseDouble(p[8]))
                .consumerDebtIndex(Double.parseDouble(p[9]))
                .creditStressIndex(Double.parseDouble(p[10]))
                .homePriceIndex(Double.parseDouble(p[11]))
                .housingInventoryIndex(Double.parseDouble(p[12]))
                .mortgageRatePct(Double.parseDouble(p[13]))
                .foreclosureRatePct(Double.parseDouble(p[14]))
                .build();
    }

    public static class Builder {
        private int year;
        private LocalDate recordedOn = LocalDate.now();
        private String location = "Columbus, OH";
        private double unemploymentRatePct;
        private int employedCount;
        private int unemployedCount;
        private double gdpGrowthPct;
        private double inflationPct;
        private double fedFundsRatePct;
        private double consumerDebtIndex;
        private double creditStressIndex;
        private double homePriceIndex;
        private double housingInventoryIndex;
        private double mortgageRatePct;
        private double foreclosureRatePct;

        public Builder year(int v) { this.year = v; return this; }
        public Builder recordedOn(LocalDate v) { this.recordedOn = v; return this; }
        public Builder location(String v) { this.location = v; return this; }
        public Builder unemploymentRatePct(double v) { this.unemploymentRatePct = v; return this; }
        public Builder employedCount(int v) { this.employedCount = v; return this; }
        public Builder unemployedCount(int v) { this.unemployedCount = v; return this; }
        public Builder gdpGrowthPct(double v) { this.gdpGrowthPct = v; return this; }
        public Builder inflationPct(double v) { this.inflationPct = v; return this; }
        public Builder fedFundsRatePct(double v) { this.fedFundsRatePct = v; return this; }
        public Builder consumerDebtIndex(double v) { this.consumerDebtIndex = v; return this; }
        public Builder creditStressIndex(double v) { this.creditStressIndex = v; return this; }
        public Builder homePriceIndex(double v) { this.homePriceIndex = v; return this; }
        public Builder housingInventoryIndex(double v) { this.housingInventoryIndex = v; return this; }
        public Builder mortgageRatePct(double v) { this.mortgageRatePct = v; return this; }
        public Builder foreclosureRatePct(double v) { this.foreclosureRatePct = v; return this; }

        public EconomicSnapshot build() { return new EconomicSnapshot(this); }
    }
}
