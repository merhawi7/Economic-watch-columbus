import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmailReportGenerator {

    public static String generate(EconomicSnapshot snapshot) {

        LocalDate date = snapshot.getRecordedOn();

        // LIVE VALUES (from FRED)
        // unemployment = Columbus, OH metro area (not seasonally adjusted)
        // gdp, inflation, fedFunds, mortgage = U.S. national
        double unemployment = snapshot.getUnemploymentRatePct();
        double gdp = snapshot.getGdpGrowthPct();
        double inflation = snapshot.getInflationPct();
        double fedFunds = snapshot.getFedFundsRatePct();
        double mortgage = snapshot.getMortgageRatePct();
        double homePriceIndex = snapshot.getHomePriceIndex();

        // U.S. NATIONAL UNEMPLOYMENT (FRED series UNRATE), shown separately
        // so it can't be confused with the Columbus metro rate.
        boolean usUnempOk = false;
        double usUnemployment = 0.0;

        try {
            usUnemployment = FredApi.getLatestValue("UNRATE");
            usUnempOk = true;
        } catch (Exception e) {
            // show n/a below
        }

        // CENTRAL OHIO REFERENCE VALUES (not live; update by hand and check the source)
        // Home price: Columbus REALTORS(R) Central Ohio median sales price, July 2026
        // Median rent: Apartment List Rent Index, Columbus city, August 2026
        // Inventory, months supply, days on market: Central Ohio MLS reference figures
        int centralOhioInventory = 6193;
        double monthsSupply = 2.4;
        String daysOnMarket = "~42-44 days";
        String medianRent = "~$1,308";
        String homePriceReference = "~$350K";

        // APP SCORES (set by hand; not official statistics)
        int economicRisk = 18;
        int buyerLeverage = 60;
        int affordability = 75;

        // FEDERAL FUNDS: show the target range, plus the effective rate.
        String fedValue;
        String fedNoteEn;
        String fedNoteTi;

        try {
            double upper = FredApi.getLatestValue("DFEDTARU");
            double lower = FredApi.getLatestValue("DFEDTARL");

            fedValue = String.format(
                    Locale.US, "%.2f%% &ndash; %.2f%%", lower, upper);

            fedNoteEn = String.format(
                    Locale.US,
                    "Target range. Effective rate: %.2f%%",
                    fedFunds);
            fedNoteTi = String.format(
                    Locale.US,
                    "ዕላማ ወሰን። ውጽኢታዊ መጠን: %.2f%%",
                    fedFunds);

        } catch (Exception e) {
            fedValue = String.format(Locale.US, "%.2f%%", fedFunds);
            fedNoteEn = "Daily effective rate";
            fedNoteTi = "ዕለታዊ ውጽኢታዊ መጠን";
        }

        // MORTGAGE DIRECTION: compare the two newest weekly readings.
        // The arrow shows direction of change, not good or bad.
        String mortgageArrow = "&rarr;";
        String mortgageWord = "Stable / ዝተረጋጋ";

        try {
            double[] recent =
                    FredApi.getRecentValues("MORTGAGE30US", 2);

            if (recent.length == 2) {

                double change = recent[0] - recent[1];

                if (change > 0.02) {
                    mortgageArrow = "&#8599;";
                    mortgageWord = "Rising / ይውስኽ ኣሎ";
                } else if (change < -0.02) {
                    mortgageArrow = "&#8600;";
                    mortgageWord = "Falling / ይንከይ ኣሎ";
                }
            }

        } catch (Exception e) {
            // keep the neutral default
        }

        // DATA AS OF: newest observation date FRED has for each series.
        String hpiAsOf = asOf("ATNHPIUS18140Q", "quarter");

        String unemploymentStatus =
                unemployment < 5.0
                        ? "Low / ትሑት"
                        : "Elevated / ልዑል";

        String usUnemploymentStatus =
                usUnemployment < 4.5
                        ? "Moderate / ማእከላይ"
                        : "Elevated / ልዑል";

        String gdpStatus =
                gdp >= 1.0
                        ? "Good / ጽቡቕ"
                        : "Weak / ድኹም";

        String mortgageStatus =
                mortgage >= 6.5
                        ? "High / ልዑል"
                        : "Moderate / ማእከላይ";

        String inflationStatus =
                inflation <= 3.0
                        ? "Good / ጽቡቕ"
                        : "Challenging / ኣሸጋሪ";

        String dateText =
                date.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM d, yyyy",
                                Locale.ENGLISH
                        )
                );

        String unempText = String.format(Locale.US, "%.1f%%", unemployment);
        String usUnempText = usUnempOk
                ? String.format(Locale.US, "%.1f%%", usUnemployment)
                : "n/a";
        String mortgageText = String.format(Locale.US, "%.2f%%", mortgage);
        String hpiText = String.format(Locale.US, "%.2f", homePriceIndex);

        StringBuilder h = new StringBuilder();

        h.append(HEAD);

        // ---------- HEADER ----------
        h.append("<tr><td class='hero'>");
        h.append("<div class='logo'>ECONOMIC WATCH</div>");
        h.append("<div class='h1'>Columbus Economic Snapshot</div>");
        h.append("<div class='sub'>Columbus, Ohio &mdash; Local Economic &amp; Housing Monitor</div>");
        h.append("<div class='sub ti-light'>ኮሎምበስ፣ ኦሃዮ — ኢኮኖሚያዊን ናይ ገዛን ምልከታ</div>");
        h.append("<div class='heroline'><span class='badge'>WEEKLY REPORT / ሰሙናዊ ጸብጻብ</span>");
        h.append("<span class='when'>Updated / ዝተሓደሰ: <b>").append(dateText).append("</b></span></div>");
        h.append("<div class='sub small'>Latest available data, not real-time / ናይ ሕጂ ዝርከብ ሓበሬታ፣ ቀጥታ ኣይኮነን</div>");
        h.append("</td></tr>");

        h.append("<tr><td class='body'>");

        // ---------- KEY INDICATORS ----------
        h.append(section("Key Indicators", "ቀንዲ መለክዒታት"));
        h.append("<table class='grid' role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        h.append(card("Columbus Metro Unemployment", "ስራሕ ኣልቦነት ኮሎምበስን ከባብያን",
                unempText, "Columbus metro area &bull; " + asOf("COLU139URN", "month"), "#0f766e"));
        h.append(card("U.S. Unemployment", "ስራሕ ኣልቦነት ኣመሪካ",
                usUnempText, "National &bull; " + asOf("UNRATE", "month"), "#2563eb"));
        h.append("</tr><tr>");
        h.append(card("U.S. Mortgage Rate", "ወለድ ሞርጌጅ ኣመሪካ",
                mortgageText, "30-year fixed &bull; " + asOf("MORTGAGE30US", "day"), "#c2410c"));
        h.append(card("Home Price Index", "መዐቀኒ ዋጋ ገዛ",
                hpiText, "Columbus HPI &bull; " + hpiAsOf, "#7c3aed"));
        h.append("</tr></table>");

        // ---------- JOBS & ECONOMY ----------
        h.append(section("Jobs &amp; Economy", "ስራሕን ኢኮኖሚን"));
        h.append(tableOpen());
        h.append(row("Columbus Metro Unemployment", "ስራሕ ኣልቦነት ኮሎምበስን ከባብያን",
                "Columbus, OH metro area, BLS, not seasonally adjusted",
                "ኮሎምበስን ከባብያን፣ BLS፣ ብወቕቲ ዘይተመዓረየ",
                unempText, "good", unemploymentStatus));
        h.append(row("U.S. Unemployment", "ስራሕ ኣልቦነት ኣመሪካ",
                "National rate, BLS", "ሃገራዊ መጠን፣ BLS",
                usUnempText, "info", usUnempOk ? usUnemploymentStatus : "n/a"));
        h.append(row("U.S. Inflation", "ዕቤት ዋጋ ኣመሪካ",
                "CPI, " + asOf("CPIAUCSL", "month"), "CPI፣ ሃገራዊ",
                String.format(Locale.US, "%.1f%%", inflation), "warn", inflationStatus));
        h.append(row("U.S. GDP Growth", "ዕቤት GDP ኣመሪካ",
                "Real GDP, " + asOf("A191RL1Q225SBEA", "quarter"), "ሓቀኛ GDP፣ ሃገራዊ",
                String.format(Locale.US, "%.1f%%", gdp), "good", gdpStatus));
        h.append(row("U.S. 30-Year Mortgage Rate", "ወለድ ሞርጌጅ ኣመሪካ",
                "Freddie Mac, " + asOf("MORTGAGE30US", "day"), "Freddie Mac፣ ሃገራዊ",
                mortgageText, "bad", mortgageStatus));
        h.append(row("Federal Funds Target Rate", "ወለድ Federal Funds",
                fedNoteEn, fedNoteTi,
                fedValue, "neutral", "National / ሃገራዊ"));
        h.append("</table>");

        // ---------- HOUSING ----------
        h.append(section("Housing Market", "ዕዳጋ ገዛ"));
        h.append(tableOpen());
        h.append(row("Columbus Home Price Index", "መዐቀኒ ዋጋ ገዛ ኮሎምበስ",
                "Index value, not a dollar price", "መዐቀኒ እዩ፣ ዶላር ዋጋ ኣይኮነን",
                hpiText, "neutral", "Latest: " + hpiAsOf + " / ሓድሽ"));
        h.append(row("Home Price", "ዋጋ ገዛ",
                "Median sale price, Central Ohio, July 2026 (Columbus REALTORS&reg;)",
                "ማእከላይ ዋጋ መሸጣ፣ ማእከላይ ኦሃዮ፣ ሓምለ 2026",
                homePriceReference, "neutral", "Stable / ዝተረጋጋ"));
        h.append(row("Median Rent", "ማእከላይ ክራይ",
                "Columbus city median, August 2026 (Apartment List)",
                "ማእከላይ ክራይ ከተማ ኮሎምበስ፣ ነሓሰ 2026",
                medianRent, "neutral", "Reference / መወከሲ"));
        h.append(row("Central Ohio Inventory", "ብዝሒ ዘሎ ገዛውቲ",
                "Central Ohio MLS reference figure, not live",
                "ናይ ማእከላይ ኦሃዮ MLS መወከሲ ቁጽሪ፣ ቀጥታ ኣይኮነን",
                String.format(Locale.US, "%,d", centralOhioInventory), "info", "Improving / ይውስኽ ኣሎ"));
        h.append(row("Months Supply", "ናይ ወርሒ ኣቕርቦት",
                "Central Ohio MLS reference figure, not live",
                "ናይ ማእከላይ ኦሃዮ MLS መወከሲ ቁጽሪ፣ ቀጥታ ኣይኮነን",
                String.format(Locale.US, "%.1f months", monthsSupply), "warn", "Low supply / ውሑድ ኣቕርቦት"));
        h.append(row("Days on Market", "ኣብ ዕዳጋ ዝጸንሓሉ መዓልታት",
                "Central Ohio MLS reference figure, not live",
                "ናይ ማእከላይ ኦሃዮ MLS መወከሲ ቁጽሪ፣ ቀጥታ ኣይኮነን",
                daysOnMarket, "info", "Improving / ይመሓየሽ ኣሎ"));
        h.append("</table>");

        // ---------- SCORES ----------
        h.append(section("Economic Watch Scores", "ነጥቢታት Economic Watch"));
        h.append(tableOpen());
        h.append(row("Economic Risk Score", "ነጥቢ ኢኮኖሚያዊ ሓደጋ",
                "Economic Watch indicator, not an official statistic",
                "ናይ Economic Watch መርኣዪ፣ ወግዓዊ ስታቲስቲክስ ኣይኮነን",
                economicRisk + "/100", "good", "Low / ትሑት"));
        h.append(row("Buyer Leverage Score", "ነጥቢ ሓይሊ ገዛእቲ ገዛ",
                "Economic Watch indicator, not an official statistic",
                "ናይ Economic Watch መርኣዪ፣ ወግዓዊ ስታቲስቲክስ ኣይኮነን",
                buyerLeverage + "/100", "info", "Improving / ይመሓየሽ ኣሎ"));
        h.append(row("Affordability Score", "ነጥቢ ዓቕሚ ክፍሊት",
                "Economic Watch indicator, not an official statistic",
                "ናይ Economic Watch መርኣዪ፣ ወግዓዊ ስታቲስቲክስ ኣይኮነን",
                affordability + "/100", "orange", "Challenging / ኣሸጋሪ"));
        h.append("</table>");

        // ---------- MARKET DIRECTION ----------
        h.append(section("Market Direction", "ኣንፈት ዕዳጋ"));
        h.append("<table class='pulse' role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        h.append(pulse("&#8599;", "Inventory", "ክምችት", "Rising / ይውስኽ ኣሎ"));
        h.append(pulse("&#8599;", "Buyer Leverage", "ሓይሊ ገዛእቲ", "Improving / ይመሓየሽ ኣሎ"));
        h.append(pulse("&rarr;", "Prices", "ዋጋታት", "Stable / ዝተረጋጋ"));
        h.append(pulse(mortgageArrow, "Mortgage Rates", "ወለድ ሞርጌጅ", mortgageWord));
        h.append(pulse("&#8600;", "Affordability", "ተመጣጣንነት", "Challenging / ኣሸጋሪ"));
        h.append("</tr></table>");
        h.append("<div class='fine'>Arrows show the direction of change, not whether it is good or bad for buyers. Rising mortgage rates are worse for buyers.<br>");
        h.append("ምልክታት ኣንፈት ለውጢ እዮም፣ ንገዛእቲ ጽቡቕ ወይ ሕማቕ ምዃኑ ኣየርኣዩን።</div>");

        // ---------- WHAT IT MEANS ----------
        h.append(section("What It Means", "ትርጉሙ እንታይ እዩ?"));
        h.append(meaning("good", "🟢", "Jobs: Healthy", "ስራሕ፦ ጥዑይ"));
        h.append(meaning("good", "🟢", "Inventory: Improving", "ኣቕርቦት፦ ይመሓየሽ ኣሎ"));
        h.append(meaning("good", "🟢", "Buyer leverage: Improving", "ሓይሊ ገዛእቲ፦ ይመሓየሽ ኣሎ"));
        h.append(meaning("warn", "🟡", "Prices: Mostly stable", "ዋጋ፦ ብዙሕ ኣይተቐየረን"));
        h.append(meaning("bad", "🔴", "Mortgage rates: High", "ወለድ ሞርጌጅ፦ ልዑል"));
        h.append(meaning("orange", "🟠", "Affordability: Challenging", "ዓቕሚ ክፍሊት፦ ኣሸጋሪ"));

        // ---------- BOTTOM LINE ----------
        h.append(section("Bottom Line", "ቀንዲ ነጥቢ"));
        h.append("<div class='bottom'>");
        h.append("<div class='bl-title'>Overall Outlook / ሓፈሻዊ ኣንፈት</div>");
        h.append("<div class='bl-en'>Conditions are improving for buyers, but high mortgage rates are still the biggest obstacle.</div>");
        h.append("<div class='bl-ti'>ኩነታት ንገዛእቲ ገዛ በብቑሩብ ይሓይሽ ኣሎ፣ ግን ልዑል ወለድ ሞርጌጅ ገና ዓብዪ ዕንቅፋት እዩ።</div>");
        h.append("</div>");

        // ---------- DATA NOTES ----------
        h.append(section("Data Notes", "ሓበሬታ ብዛዕባ ዳታ"));
        h.append("<div class='notes'>");

        h.append(note("LIVE DATA",
                "Indicators connected to the application are retrieved from FRED when the report is generated. "
                        + "Columbus unemployment is for the Columbus, OH metro area (BLS, not seasonally adjusted), "
                        + "not only the city. U.S. unemployment, inflation, GDP growth, the 30-year mortgage rate "
                        + "(Freddie Mac) and the federal funds rate are national figures.",
                "ቀጥታ ሓበሬታ",
                "ናብ መተግበሪ ዝተኣሳሰሩ መለክዒታት እቲ ጸብጻብ ክፍጠር ከሎ ካብ FRED ይውሰዱ። ስራሕ ኣልቦነት ኮሎምበስ ናይ ኮሎምበስን ከባብያን (BLS፣ ብወቕቲ ዘይተመዓረየ) እዩ፣ ከተማ ጥራይ ኣይኮነን። "
                        + "ስራሕ ኣልቦነት ኣመሪካ፣ ዕቤት ዋጋ፣ GDP፣ ወለድ ሞርጌጅን Federal Fundsን ሃገራዊ (ኣመሪካ) እዮም።"));

        h.append(note("CENTRAL OHIO REFERENCE FIGURES",
                "The 6,193 inventory, 2.4 months supply and approximately 42-44 days on market are Central Ohio MLS "
                        + "reference figures. They are not live-connected to FRED and may lag. The ~$350K home price is the "
                        + "Central Ohio median sales price for July 2026 (Columbus REALTORS&reg;). The ~$1,308 median rent "
                        + "is the Columbus city median for August 2026 (Apartment List).",
                "ናይ ማእከላይ ኦሃዮ መወከሲ ቁጽርታት",
                "ዋጋ ገዛ፣ ክራይ፣ ብዝሒ ገዛውቲ፣ ናይ ወርሒ ኣቕርቦትን መዓልታት ኣብ ዕዳጋን ካብ ማእከላይ ኦሃዮ ዝተወስዱ መወከሲ ሓበሬታ እዮም፣ ቀጥታ ካብ FRED ኣይኮኑን፣ ክደንጉዩ ይኽእሉ።"));

        h.append(note("IMPORTANT",
                "Columbus metro data and Central Ohio MLS data are kept separate. "
                        + "The Columbus Home Price Index is an index value, not a dollar home price.",
                "ኣገዳሲ",
                "ሓበሬታ ኮሎምበስን ከባብያን ን MLS ማእከላይ ኦሃዮን ፈላዪ እዩ። መዐቀኒ ዋጋ ገዛ ኮሎምበስ መዐቀኒ እዩ፣ ዶላር ዋጋ ገዛ ኣይኮነን።"));

        h.append(note("DATA AS OF",
                "Columbus unemployment: " + asOf("COLU139URN", "month")
                        + " &bull; U.S. unemployment: " + asOf("UNRATE", "month")
                        + " &bull; Columbus HPI: " + hpiAsOf
                        + " &bull; U.S. GDP growth: " + asOf("A191RL1Q225SBEA", "quarter")
                        + " &bull; U.S. CPI inflation: " + asOf("CPIAUCSL", "month")
                        + " &bull; U.S. 30-year mortgage rate: " + asOf("MORTGAGE30US", "day")
                        + " &bull; Federal funds rate: " + asOf("DFF", "day"),
                "ዕለት ናይ ሓበሬታ",
                "ኣብ ላዕሊ ዘሎ ዕለት ናይ ነፍሲ ወከፍ መለክዒ ናይ መወዳእታ ዝርከብ ምስትንታን እዩ።"));

        h.append(note("LATEST AVAILABLE, NOT REAL-TIME",
                "Some indicators are updated monthly or quarterly, so their latest observation can be weeks or months old.",
                "ናይ ሕጂ ዝርከብ፣ ቀጥታ ኣይኮነን",
                "ገለ መለክዒታት ብወርሒ ወይ ብርብዒ ዓመት እዮም ዝሕደሱ፣ ስለዚ ናይ መወዳእታ ሓበሬታ ሳምንታት ወይ ወሩኻት ዝገደመ ክኸውን ይኽእል።"));

        h.append(note("SCORES",
                "The Economic Risk, Buyer Leverage and Affordability scores are Economic Watch indicators. "
                        + "They are not official government statistics.",
                "ነጥቢታት",
                "እዞም ነጥቢታት ናይ Economic Watch መርእያታት እዮም፣ ወግዓዊ ስታቲስቲክስ ኣይኮኑን።"));

        h.append("</div>");

        h.append("</td></tr>");

        // ---------- FOOTER ----------
        h.append("<tr><td class='foot'>");
        h.append("<div class='brand'>ECONOMIC WATCH</div>");
        h.append("<div>Columbus, Ohio &bull; Automated Weekly Economic Report</div>");
        h.append("<div>ኢኮኖሚያዊ ምልከታ • ኮሎምበስ፣ ኦሃዮ • ሰሙናዊ ጸብጻብ</div>");
        h.append("</td></tr>");

        h.append("</table></td></tr></table></body></html>");

        return h.toString();
    }

    // ------------------------------------------------------------------
    // HTML helpers
    // ------------------------------------------------------------------

    private static String section(String en, String ti) {
        return "<div class='sec'><span class='sec-en'>" + en
                + "</span><span class='sec-ti'>" + ti + "</span></div>";
    }

    private static String card(String en, String ti, String value,
                               String note, String color) {
        return "<td class='col' width='50%' valign='top'>"
                + "<div class='card'>"
                + "<div class='c-en'>" + en + "</div>"
                + "<div class='c-ti'>" + ti + "</div>"
                + "<div class='c-val'>" + value + "</div>"
                + "<div class='c-note'>" + note + "</div>"
                + "</div></td>";
    }

    private static String tableOpen() {
        return "<table class='data' role='presentation' width='100%' cellpadding='0' cellspacing='0'>";
    }

    private static String row(String en, String ti, String srcEn, String srcTi,
                              String value, String cls, String status) {
        return "<tr>"
                + "<td class='lbl'><div class='l-en'>" + en + "</div>"
                + "<div class='l-ti'>" + ti + "</div>"
                + "<div class='src'>" + srcEn + "<br>" + srcTi + "</div></td>"
                + "<td class='val'>" + value + "</td>"
                + "<td class='st'><span class='pill " + cls + "'>" + status + "</span></td>"
                + "</tr>";
    }

    private static String pulse(String arrow, String en, String ti, String word) {
        return "<td class='pcell' width='20%' valign='top'>"
                + "<div class='p-arrow'>" + arrow + "</div>"
                + "<div class='p-en'>" + en + "</div>"
                + "<div class='p-ti'>" + ti + "</div>"
                + "<div class='p-word'>" + word + "</div></td>";
    }

    private static String meaning(String cls, String dot, String en, String ti) {
        return "<div class='mean " + cls + "'><b>" + en + "</b>"
                + "<span class='m-ti'>" + ti + "</span></div>";
    }

    private static String note(String titleEn, String bodyEn,
                               String titleTi, String bodyTi) {
        return "<div class='n-block'>"
                + "<div class='n-en'><b>" + titleEn + ":</b> " + bodyEn + "</div>"
                + "<div class='n-ti'><b>" + titleTi + ":</b> " + bodyTi + "</div>"
                + "</div>";
    }

    /**
     * Date of the newest observation FRED has for a series, as text.
     * kind: "month" (Aug 2026), "quarter" (Q2 2026) or "day" (Oct 1, 2026).
     * Returns "n/a" if the date can't be retrieved.
     */
    private static String asOf(String seriesId, String kind) {

        try {

            LocalDate d = LocalDate.parse(FredApi.getLatestDate(seriesId));

            if (kind.equals("quarter")) {
                return "Q" + ((d.getMonthValue() - 1) / 3 + 1)
                        + " " + d.getYear();
            }

            if (kind.equals("month")) {
                return d.format(
                        DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
            }

            return d.format(
                    DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));

        } catch (Exception e) {
            return "n/a";
        }
    }

    // ------------------------------------------------------------------
    // Page head and styles
    // ------------------------------------------------------------------

    private static final String HEAD = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Economic Watch - Columbus, Ohio</title>
<style>
body { margin:0; padding:0; background:#f1f3f6; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; color:#1f2937; line-height:1.5; }
table { border-collapse:collapse; }
.wrap { max-width:720px; width:100%; background:#ffffff; border:1px solid #d5dbe3; }
.hero { background:#14233a; color:#ffffff; padding:30px 30px 24px; border-bottom:3px solid #b8924a; }
.logo { font-size:11px; font-weight:700; letter-spacing:3px; color:#c9b27c; }
.h1 { margin-top:10px; font-family:Georgia,'Times New Roman',serif; font-size:28px; font-weight:700; line-height:1.2; }
.sub { margin-top:6px; font-size:13px; color:#cbd5e1; }
.ti-light { color:#b6c2d4; }
.small { font-size:11px; color:#94a3b8; margin-top:14px; }
.heroline { margin-top:18px; }
.badge { display:inline-block; border:1px solid #c9b27c; color:#e8d9b0; font-size:10px; font-weight:700; letter-spacing:1px; padding:4px 10px; margin-right:12px; }
.when { font-size:12px; color:#cbd5e1; }
.when b { color:#ffffff; }
.body { padding:4px 28px 28px; }
.sec { margin:30px 0 14px; padding-bottom:6px; border-bottom:1px solid #14233a; }
.sec-en { font-family:Georgia,'Times New Roman',serif; font-size:17px; font-weight:700; color:#14233a; }
.sec-ti { font-size:13px; font-weight:600; color:#64748b; margin-left:10px; }
.grid { table-layout:fixed; }
.col { padding:0 6px 12px; }
.card { background:#ffffff; border:1px solid #d5dbe3; border-left:3px solid #14233a; padding:14px 16px; }
.c-en { font-size:10px; font-weight:700; text-transform:uppercase; letter-spacing:0.8px; color:#475569; }
.c-ti { font-size:12px; color:#64748b; margin-top:2px; }
.c-val { font-family:Georgia,'Times New Roman',serif; font-size:32px; font-weight:700; color:#14233a; margin-top:8px; line-height:1.1; }
.c-note { font-size:11px; color:#64748b; margin-top:6px; }
.data { border:1px solid #d5dbe3; }
.data td { padding:11px 14px; border-bottom:1px solid #e5e9ef; vertical-align:middle; }
.data tr:last-child td { border-bottom:none; }
.data tr:nth-child(even) td { background:#f8fafc; }
.lbl { width:52%; }
.l-en { font-size:14px; font-weight:600; color:#1f2937; }
.l-ti { font-size:12px; color:#64748b; }
.src { font-size:10px; color:#8794a6; margin-top:4px; line-height:1.4; }
.val { font-family:Georgia,'Times New Roman',serif; font-size:17px; font-weight:700; color:#14233a; white-space:nowrap; }
.st { text-align:right; }
.pill { display:inline-block; padding:3px 9px; font-size:10px; font-weight:700; letter-spacing:0.2px; border-radius:2px; }
.good { background:#e7f1ec; color:#276749; }
.info { background:#e6eef7; color:#2c5282; }
.warn { background:#fbf3e0; color:#8a6116; }
.bad { background:#f8e7e6; color:#9b2c2c; }
.orange { background:#faece0; color:#9c4a14; }
.neutral { background:#eef0f3; color:#4b5563; }
.pulse td { text-align:center; padding:12px 4px; background:#ffffff; border:1px solid #d5dbe3; }
.p-arrow { font-size:20px; font-weight:700; color:#14233a; }
.p-en { font-size:11px; font-weight:700; color:#1f2937; margin-top:4px; }
.p-ti { font-size:10px; color:#64748b; }
.p-word { font-size:10px; color:#64748b; margin-top:4px; }
.fine { font-size:11px; color:#8794a6; margin-top:10px; }
.mean { padding:10px 14px; margin-bottom:6px; font-size:14px; background:#f8fafc; border:1px solid #e5e9ef; border-left-width:4px; color:#1f2937; }
.m-ti { display:block; font-size:12px; color:#64748b; }
.mean.good { border-left-color:#276749; }
.mean.warn { border-left-color:#b7791f; }
.mean.bad { border-left-color:#9b2c2c; }
.mean.orange { border-left-color:#c05621; }
.bottom { background:#f8fafc; border:1px solid #d5dbe3; border-left:4px solid #b8924a; padding:20px 22px; }
.bl-title { font-size:10px; font-weight:700; letter-spacing:1.5px; text-transform:uppercase; color:#8a6d2f; }
.bl-en { font-family:Georgia,'Times New Roman',serif; font-size:17px; font-weight:700; color:#14233a; margin-top:8px; }
.bl-ti { font-size:13px; margin-top:8px; color:#475569; }
.notes { border-top:1px solid #d5dbe3; padding-top:6px; }
.n-block { margin-bottom:12px; padding-bottom:12px; border-bottom:1px solid #e5e9ef; font-size:11px; color:#475569; }
.n-block:last-child { margin-bottom:0; padding-bottom:0; border-bottom:none; }
.n-ti { margin-top:5px; color:#64748b; }
.foot { background:#14233a; text-align:center; padding:22px 16px; font-size:11px; color:#94a3b8; }
.brand { font-size:12px; font-weight:700; letter-spacing:3px; color:#c9b27c; margin-bottom:6px; }
@media (max-width:600px) {
  .col { display:block; width:100% !important; padding:0 0 12px; }
  .body { padding:2px 14px 24px; }
  .hero { padding:22px 18px 18px; }
  .h1 { font-size:23px; }
  .lbl { width:auto; }
  .st { display:block; text-align:left; padding-top:0 !important; }
  .sec-ti { display:block; margin-left:0; }
  .pulse td { display:inline-block; width:46% !important; margin:2px; }
}
</style>
</head>
<body>
<table role="presentation" width="100%" cellpadding="0" cellspacing="0"><tr><td align="center" style="padding:24px 10px;">
<table role="presentation" class="wrap" cellpadding="0" cellspacing="0">
""";
}