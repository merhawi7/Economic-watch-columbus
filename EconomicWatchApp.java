import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class EconomicWatchApp {

private static final DataStore dataStore =
        new DataStore("columbus_oh_history.dat");

private static final Dashboard dashboard = new Dashboard();
private static final AlertService alerts = new AlertService();
private static final RiskScoreCalculator riskCalculator =
        new RiskScoreCalculator();

private static final Scanner scanner = new Scanner(System.in);

public static void main(String[] args) {

    System.out.println("=== Economic Watch: Columbus, Ohio ===");

    /*
     * Automatic mode for Windows Task Scheduler.
     *
     * When started with:
     * java EconomicWatchApp --automatic
     *
     * the program will:
     * 1. Retrieve fresh FRED data
     * 2. Save the new snapshot
     * 3. Generate the Economic Watch report
     * 4. Send the report through Resend
     *
     * Normal manual mode is unchanged.
     */
    if (args.length > 0 &&
            args[0].equalsIgnoreCase("--automatic")) {

        runAutomaticMode();
        return;
    }

    boolean running = true;

    while (running) {

        printMenu();

        String choice = scanner.nextLine().trim();

        switch (choice) {

            case "1" -> showDashboard();

            case "2" -> updateLiveSnapshot();

            case "3" -> viewHistory();

            case "4" -> configureAlerts();

            case "5" -> sendEmailReport();

            case "0" -> {
                running = false;
                System.out.println("Goodbye!");
            }

            default ->
                    System.out.println("Invalid option, try again.");
        }
    }

    scanner.close();
}

/**
 * Automatic mode used by Windows Task Scheduler.
 */
private static void runAutomaticMode() {

    System.out.println();
    System.out.println(
            "========================================"
    );
    System.out.println(
            " AUTOMATIC ECONOMIC WATCH RUN"
    );
    System.out.println(
            "========================================"
    );

    try {

        System.out.println();
        System.out.println(
                "Step 1: Updating live FRED data..."
        );

        updateLiveSnapshot();

        EconomicSnapshot latest =
                dataStore.getMostRecent();

        if (latest == null) {

            throw new IllegalStateException(
                    "No economic snapshot is available after the update."
            );
        }

        System.out.println();
        System.out.println(
                "Step 2: Generating Economic Watch report..."
        );

        String htmlReport =
                EmailReportGenerator.generate(latest);

        System.out.println(
                "Step 3: Sending report through Resend..."
        );

        EmailSender.sendReport(htmlReport);

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                " AUTOMATIC RUN COMPLETE"
        );
        System.out.println(
                "========================================"
        );

    } catch (Exception e) {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                " AUTOMATIC RUN FAILED"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Error: " + e.getMessage()
        );

        e.printStackTrace();
    }
}

private static void printMenu() {

    System.out.println("""
            
            ---------------------------------
            1. View dashboard (most recent data)
            2. Update with live FRED data
            3. View historical years
            4. Configure alerts
            5. Send Economic Watch report by email
            0. Exit
            ---------------------------------""");

    System.out.print("Choose an option: ");
}

private static void showDashboard() {

    EconomicSnapshot latest = dataStore.getMostRecent();

    if (latest == null) {

        System.out.println(
                "No data yet. Choose option 2 to retrieve live data."
        );

        return;
    }

    dashboard.render(latest, alerts);
}

/**
 * Retrieves the latest available economic data.
 */
private static void updateLiveSnapshot() {

    try {

        System.out.println();
        System.out.println(
                "Retrieving latest economic data from FRED..."
        );

        EconomicSnapshot previous =
                dataStore.getMostRecent();

        RiskScoreCalculator.RiskCategory oldCategory =
                previous == null
                        ? null
                        : riskCalculator.calculate(previous).category;

        int year = LocalDate.now().getYear();

        double unemployment =
                FredApi.getLatestValue("COLU139UR");

        System.out.printf(
                "Columbus unemployment: %.1f%%%n",
                unemployment
        );

        double laborForce =
                FredApi.getLatestValue("COLU139LF");

        double nonfarmEmployment =
                FredApi.getLatestValue("COLU139NA");

        int laborForceRounded =
                (int) Math.round(laborForce);

        int unemployed =
                (int) Math.round(
                        laborForce * unemployment / 100.0
                );

        int employed =
                laborForceRounded - unemployed;

        System.out.println(
                "Columbus labor force: "
                        + laborForceRounded
        );

        System.out.println(
                "Estimated unemployed residents: "
                        + unemployed
        );

        System.out.println(
                "Estimated employed residents: "
                        + employed
        );

        System.out.println(
                "Columbus nonfarm employment: "
                        + String.format(
                        "%.1f thousand",
                        nonfarmEmployment)
        );

        double gdpGrowth =
                FredApi.getLatestValue("A191RL1Q225SBEA");

        System.out.printf(
                "U.S. real GDP growth: %.1f%%%n",
                gdpGrowth
        );

        double inflation =
                getLatestCpiYearOverYear();

        System.out.printf(
                "U.S. CPI inflation: %.1f%%%n",
                inflation
        );

        double fedFunds =
                FredApi.getLatestValue("FEDFUNDS");

        System.out.printf(
                "Federal funds rate: %.2f%%%n",
                fedFunds
        );

        double homePriceIndex =
                FredApi.getLatestValue("ATNHPIUS18140Q");

        System.out.printf(
                "Columbus house price index: %.2f%n",
                homePriceIndex
        );

        double mortgageRate =
                FredApi.getLatestValue("MORTGAGE30US");

        System.out.printf(
                "30-year mortgage rate: %.2f%%%n",
                mortgageRate
        );

        /*
         * These fields do not yet have connected live sources.
         * Keep neutral values rather than inventing data.
         */
        double consumerDebtIndex = 100.0;
        double creditStressIndex = 100.0;
        double housingInventoryIndex = 100.0;
        double foreclosureRate = 0.0;

        EconomicSnapshot snapshot =
                new EconomicSnapshot.Builder()
                        .year(year)
                        .recordedOn(LocalDate.now())
                        .location("Columbus, OH")
                        .unemploymentRatePct(unemployment)
                        .employedCount(employed)
                        .unemployedCount(unemployed)
                        .gdpGrowthPct(gdpGrowth)
                        .inflationPct(inflation)
                        .fedFundsRatePct(fedFunds)
                        .consumerDebtIndex(consumerDebtIndex)
                        .creditStressIndex(creditStressIndex)
                        .homePriceIndex(homePriceIndex)
                        .housingInventoryIndex(housingInventoryIndex)
                        .mortgageRatePct(mortgageRate)
                        .foreclosureRatePct(foreclosureRate)
                        .build();

        dataStore.save(snapshot);

        RiskScoreCalculator.RiskCategory newCategory =
                riskCalculator.calculate(snapshot).category;

        if (oldCategory != null) {

            alerts.notifyRiskChange(
                    oldCategory,
                    newCategory
            );
        }

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                " LIVE DATA UPDATE COMPLETE"
        );
        System.out.println(
                "========================================"
        );

        System.out.println(
                "Saved snapshot for "
                        + LocalDate.now()
        );

        System.out.println();
        System.out.println(
                "Showing updated dashboard:"
        );

        dashboard.render(
                snapshot,
                alerts
        );

    } catch (Exception e) {

        System.out.println();
        System.out.println(
                "Could not retrieve live economic data."
        );

        System.out.println(
                "Error: " + e.getMessage()
        );
    }
}

/**
 * Sends the most recent Economic Watch snapshot by email.
 */
private static void sendEmailReport() {

    try {

        EconomicSnapshot latest =
                dataStore.getMostRecent();

        if (latest == null) {

            System.out.println();
            System.out.println(
                    "No economic data is available yet."
            );

            System.out.println(
                    "Choose option 2 first to retrieve live FRED data."
            );

            return;
        }

        System.out.println();
        System.out.println(
                "Generating Economic Watch email report..."
        );

        String htmlReport =
                EmailReportGenerator.generate(latest);

        System.out.println(
                "Sending report through Resend..."
        );

        EmailSender.sendReport(htmlReport);

    } catch (Exception e) {

        System.out.println();
        System.out.println(
                "Could not send the email report."
        );

        System.out.println(
                "Error: " + e.getMessage()
        );
    }
}

/**
 * Gets the latest U.S. CPI year-over-year inflation rate.
 *
 * This makes two FRED requests:
 * 1. Get the latest CPI observation.
 * 2. Get the CPI observation from the same month one year earlier.
 */
private static double getLatestCpiYearOverYear()
        throws Exception {

    String apiKey = System.getenv("FRED_API_KEY");

    if (apiKey == null || apiKey.isBlank()) {
        throw new IllegalStateException(
                "FRED_API_KEY environment variable is not set."
        );
    }

    java.net.http.HttpClient client =
            java.net.http.HttpClient.newHttpClient();

    java.util.regex.Pattern datePattern =
            java.util.regex.Pattern.compile(
                    "\"date\":\"([^\"]+)\""
            );

    java.util.regex.Pattern valuePattern =
            java.util.regex.Pattern.compile(
                    "\"value\":\"([^\"]+)\""
            );

    /*
     * First request:
     * Get the latest available CPI observation.
     */
    String latestUrl =
            "https://api.stlouisfed.org/fred/series/observations"
                    + "?series_id=CPIAUCSL"
                    + "&api_key=" + apiKey
                    + "&file_type=json"
                    + "&sort_order=desc"
                    + "&limit=1";

    java.net.http.HttpRequest latestRequest =
            java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(latestUrl))
                    .GET()
                    .build();

    java.net.http.HttpResponse<String> latestResponse =
            client.send(
                    latestRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

    if (latestResponse.statusCode() != 200) {
        throw new Exception(
                "Latest CPI request returned HTTP "
                        + latestResponse.statusCode()
        );
    }

    java.util.regex.Matcher dateMatcher =
            datePattern.matcher(latestResponse.body());

    java.util.regex.Matcher valueMatcher =
            valuePattern.matcher(latestResponse.body());

    if (!dateMatcher.find() || !valueMatcher.find()) {
        throw new Exception(
                "Could not read latest CPI observation."
        );
    }

    String latestDate =
            dateMatcher.group(1);

    double latestCpi =
            Double.parseDouble(valueMatcher.group(1));

    /*
     * Calculate the same month one year earlier.
     */
    java.time.LocalDate latest =
            java.time.LocalDate.parse(latestDate);

    java.time.LocalDate yearAgo =
            latest.minusYears(1);

    String yearAgoDate =
            yearAgo.toString();

    /*
     * Second request:
     * Ask FRED for the CPI around that date.
     */
    String yearAgoUrl =
            "https://api.stlouisfed.org/fred/series/observations"
                    + "?series_id=CPIAUCSL"
                    + "&api_key=" + apiKey
                    + "&file_type=json"
                    + "&observation_start=" + yearAgoDate
                    + "&observation_end=" + yearAgoDate
                    + "&sort_order=asc"
                    + "&limit=1";

    java.net.http.HttpRequest yearAgoRequest =
            java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(yearAgoUrl))
                    .GET()
                    .build();

    java.net.http.HttpResponse<String> yearAgoResponse =
            client.send(
                    yearAgoRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString()
            );

    if (yearAgoResponse.statusCode() != 200) {
        throw new Exception(
                "Year-ago CPI request returned HTTP "
                        + yearAgoResponse.statusCode()
        );
    }

    java.util.regex.Matcher yearAgoValueMatcher =
            valuePattern.matcher(yearAgoResponse.body());

    if (!yearAgoValueMatcher.find()) {
        throw new Exception(
                "Could not read CPI from one year earlier."
        );
    }

    double yearAgoCpi =
            Double.parseDouble(
                    yearAgoValueMatcher.group(1)
            );

    /*
     * Calculate year-over-year inflation.
     */
    return ((latestCpi / yearAgoCpi) - 1.0) * 100.0;
}

private static void viewHistory() {

    List<EconomicSnapshot> all =
            dataStore.loadAll();

    if (all.isEmpty()) {

        System.out.println(
                "No historical data yet."
        );

        return;
    }

    System.out.println(
            "Historical years on record:"
    );

    for (EconomicSnapshot s : all) {

        RiskScoreCalculator.RiskResult r =
                riskCalculator.calculate(s);

        System.out.printf(
                "  %d  |  Risk: %s %d/100 (%s)  |  Unemployment: %.1f%%  |  Recorded: %s%n",
                s.getYear(),
                r.category.icon,
                r.score,
                r.category.label,
                s.getUnemploymentRatePct(),
                s.getRecordedOn()
        );
    }

    System.out.print(
            "Enter a year to view its full dashboard, "
                    + "or press Enter to go back: "
    );

    String input =
            scanner.nextLine().trim();

    if (!input.isBlank()) {

        try {

            int year =
                    Integer.parseInt(input);

            all.stream()
                    .filter(s -> s.getYear() == year)
                    .findFirst()
                    .ifPresentOrElse(
                            s -> dashboard.render(s, alerts),
                            () -> System.out.println(
                                    "No data for that year."
                            )
                    );

        } catch (NumberFormatException e) {

            System.out.println(
                    "Not a valid year."
            );
        }
    }
}

private static void configureAlerts() {

    System.out.print(
            "Enable sound alerts? (y/n): "
    );

    alerts.setSoundEnabled(
            scanner.nextLine()
                    .trim()
                    .equalsIgnoreCase("y")
    );

    System.out.print(
            "Enable email alerts? (y/n): "
    );

    alerts.setEmailEnabled(
            scanner.nextLine()
                    .trim()
                    .equalsIgnoreCase("y")
    );

    if (alerts.isEmailEnabled()) {

        System.out.print(
                "Email address ["
                        + alerts.getEmailAddress()
                        + "]: "
        );

        String email =
                scanner.nextLine().trim();

        if (!email.isBlank()) {

            alerts.setEmailAddress(email);
        }
    }

    System.out.print(
            "Enable SMS alerts? (y/n): "
    );

    alerts.setSmsEnabled(
            scanner.nextLine()
                    .trim()
                    .equalsIgnoreCase("y")
    );

    System.out.println(
            "Alert settings updated."
    );
}

}
