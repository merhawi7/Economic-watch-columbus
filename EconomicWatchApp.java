import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class EconomicWatchApp {

    private static final DataStore dataStore = new DataStore("columbus_oh_history.dat");
    private static final Dashboard dashboard = new Dashboard();
    private static final AlertService alerts = new AlertService();
    private static final RiskScoreCalculator riskCalculator = new RiskScoreCalculator();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Economic Watch: Columbus, Ohio ===");
        seedSampleDataIfEmpty();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showDashboard();
                case "2" -> enterNewSnapshot();
                case "3" -> viewHistory();
                case "4" -> configureAlerts();
                case "0" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("""

                ---------------------------------
                1. View dashboard (most recent data)
                2. Enter/update this year's data
                3. View historical years
                4. Configure alerts
                0. Exit
                ---------------------------------""");
        System.out.print("Choose an option: ");
    }

    private static void showDashboard() {
        EconomicSnapshot latest = dataStore.getMostRecent();
        if (latest == null) {
            System.out.println("No data yet. Choose option 2 to enter this year's data.");
            return;
        }
        dashboard.render(latest, alerts);
    }

    private static void enterNewSnapshot() {
        try {
            System.out.print("Year: ");
            int year = Integer.parseInt(scanner.nextLine().trim());

            EconomicSnapshot previous = dataStore.getMostRecent();
            RiskScoreCalculator.RiskCategory oldCategory = previous == null
                    ? null : riskCalculator.calculate(previous).category;

            EconomicSnapshot.Builder b = new EconomicSnapshot.Builder()
                    .year(year)
                    .recordedOn(LocalDate.now())
                    .location("Columbus, OH");

            b.unemploymentRatePct(promptDouble("Unemployment rate (%)"));
            b.employedCount(promptInt("Employed count"));
            b.unemployedCount(promptInt("Unemployed count"));
            b.gdpGrowthPct(promptDouble("GDP growth (%)"));
            b.inflationPct(promptDouble("Inflation (%)"));
            b.fedFundsRatePct(promptDouble("Fed funds rate (%)"));
            b.consumerDebtIndex(promptDouble("Consumer debt index (100 = baseline)"));
            b.creditStressIndex(promptDouble("Credit/banking stress index (100 = baseline)"));
            b.homePriceIndex(promptDouble("Home price index (100 = baseline)"));
            b.housingInventoryIndex(promptDouble("Housing inventory index (100 = baseline)"));
            b.mortgageRatePct(promptDouble("Mortgage rate (%)"));
            b.foreclosureRatePct(promptDouble("Foreclosure rate (%)"));

            EconomicSnapshot snapshot = b.build();
            dataStore.save(snapshot);

            RiskScoreCalculator.RiskCategory newCategory = riskCalculator.calculate(snapshot).category;
            if (oldCategory != null) {
                alerts.notifyRiskChange(oldCategory, newCategory);
            }

            System.out.println("Saved. Showing updated dashboard:");
            dashboard.render(snapshot, alerts);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewHistory() {
        List<EconomicSnapshot> all = dataStore.loadAll();
        if (all.isEmpty()) {
            System.out.println("No historical data yet.");
            return;
        }
        System.out.println("Historical years on record:");
        for (EconomicSnapshot s : all) {
            RiskScoreCalculator.RiskResult r = riskCalculator.calculate(s);
            System.out.printf("  %d  |  Risk: %s %d/100 (%s)  |  Unemployment: %.1f%%  |  Recorded: %s%n",
                    s.getYear(), r.category.icon, r.score, r.category.label,
                    s.getUnemploymentRatePct(), s.getRecordedOn());
        }
        System.out.print("Enter a year to view its full dashboard, or press Enter to go back: ");
        String input = scanner.nextLine().trim();
        if (!input.isBlank()) {
            try {
                int year = Integer.parseInt(input);
                all.stream().filter(s -> s.getYear() == year).findFirst()
                        .ifPresentOrElse(s -> dashboard.render(s, alerts),
                                () -> System.out.println("No data for that year."));
            } catch (NumberFormatException e) {
                System.out.println("Not a valid year.");
            }
        }
    }

    private static void configureAlerts() {
        System.out.print("Enable sound alerts? (y/n): ");
        alerts.setSoundEnabled(scanner.nextLine().trim().equalsIgnoreCase("y"));
        System.out.print("Enable email alerts? (y/n): ");
        alerts.setEmailEnabled(scanner.nextLine().trim().equalsIgnoreCase("y"));
        if (alerts.isEmailEnabled()) {
            System.out.print("Email address [" + alerts.getEmailAddress() + "]: ");
            String email = scanner.nextLine().trim();
            if (!email.isBlank()) alerts.setEmailAddress(email);
        }
        System.out.print("Enable SMS alerts? (y/n): ");
        alerts.setSmsEnabled(scanner.nextLine().trim().equalsIgnoreCase("y"));
        System.out.println("Alert settings updated.");
    }

    private static double promptDouble(String label) {
        System.out.print(label + ": ");
        return Double.parseDouble(scanner.nextLine().trim());
    }

    private static int promptInt(String label) {
        System.out.print(label + ": ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    /** Seeds one realistic sample snapshot so the dashboard has something to show on first run. */
    private static void seedSampleDataIfEmpty() {
        if (!dataStore.loadAll().isEmpty()) return;

        EconomicSnapshot sample = new EconomicSnapshot.Builder()
                .year(2026)
                .recordedOn(LocalDate.now())
                .location("Columbus, OH")
                .unemploymentRatePct(4.2)
                .employedCount(1_150_000)
                .unemployedCount(50_000)
                .gdpGrowthPct(2.1)
                .inflationPct(2.8)
                .fedFundsRatePct(4.25)
                .consumerDebtIndex(108)
                .creditStressIndex(102)
                .homePriceIndex(112)
                .housingInventoryIndex(95)
                .mortgageRatePct(6.3)
                .foreclosureRatePct(0.6)
                .build();

        dataStore.save(sample);
        System.out.println("(Seeded sample 2026 data for Columbus, OH — replace with real figures via option 2.)");
    }
}
