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
        // Falls back to the effective rate alone if the range is unavailable.
        String fedValue;
        String fedNote;

        try {
            double upper = FredApi.getLatestValue("DFEDTARU");
            double lower = FredApi.getLatestValue("DFEDTARL");

            fedValue = String.format(
                    Locale.US, "%.2f%% &ndash; %.2f%%", lower, upper);

            fedNote = String.format(
                    Locale.US,
                    "Target range. Effective rate: %.2f%%",
                    fedFunds);

        } catch (Exception e) {
            fedValue = String.format(Locale.US, "%.2f%%", fedFunds);
            fedNote = "Daily effective rate";
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

        // DATA AS OF: the date of the newest observation FRED has for each series.
        String hpiAsOf = asOf("ATNHPIUS18140Q", "quarter");

        String asOfLine =
                "Columbus unemployment: " + asOf("COLU139URN", "month")
                        + " &bull; Columbus Home Price Index: " + hpiAsOf
                        + " &bull; U.S. GDP growth: "
                        + asOf("A191RL1Q225SBEA", "quarter")
                        + " &bull; U.S. CPI inflation: "
                        + asOf("CPIAUCSL", "month")
                        + " &bull; U.S. 30-year mortgage rate: "
                        + asOf("MORTGAGE30US", "day")
                        + " &bull; Federal funds rate: "
                        + asOf("DFF", "day");

        String unemploymentStatus =
                unemployment < 5.0
                        ? "Low / ትሑት"
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

        StringBuilder html = new StringBuilder();

        html.append("""
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Economic Watch - Columbus, Ohio</title>
<style>
* { box-sizing: border-box; }
body { margin: 0; padding: 0; background: #f4f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #1e293b; line-height: 1.6; }
.container { max-width: 920px; margin: 32px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08); border: 1px solid #e2e8f0; }
.hero { background: linear-gradient(135deg, #0f172a, #1e3a8a 60%, #2563eb); color: white; padding: 40px 36px 36px; }
.hero-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 20px; }
.logo { font-size: 12px; font-weight: 800; letter-spacing: 2.5px; color: #93c5fd; margin-bottom: 10px; text-transform: uppercase; }
.hero h1 { margin: 0; font-size: 30px; font-weight: 700; }
.hero-subtitle { margin-top: 8px; color: #cbd5e1; font-size: 15px; }
.hero-tigrinya { margin-top: 4px; color: #93c5fd; font-size: 14px; font-weight: 500; }
.report-pill { background: rgba(255, 255, 255, 0.12); border: 1px solid rgba(255, 255, 255, 0.2); color: #ffffff; border-radius: 20px; padding: 8px 14px; font-size: 12px; font-weight: 700; }
.hero-date { margin-top: 24px; color: #93c5fd; font-size: 13px; font-weight: 500; }
.content { padding: 32px 36px 40px; }
.section-title { display: flex; align-items: center; gap: 12px; margin: 36px 0 18px; }
.section-line { height: 4px; width: 36px; background: #2563eb; border-radius: 4px; }
.section-title h2 { margin: 0; color: #0f172a; font-size: 18px; font-weight: 700; }
.metrics { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin: 24px 0 32px; }
.metric-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; position: relative; }
.metric-card::before { content: ""; position: absolute; top: 0; left: 0; width: 100%; height: 4px; background: #2563eb; }
.metric-card.green::before { background: #16a34a; }
.metric-card.orange::before { background: #ea580c; }
.metric-label { color: #64748b; font-size: 11px; font-weight: 700; text-transform: uppercase; }
.metric-value { margin-top: 8px; font-size: 26px; font-weight: 800; color: #0f172a; }
.metric-note { margin-top: 4px; font-size: 12px; color: #64748b; }
.updated { background: #f1f5f9; border: 1px solid #cbd5e1; border-left: 4px solid #0f172a; border-radius: 8px; padding: 12px 16px; margin-bottom: 28px; font-size: 13px; display: flex; justify-content: space-between; }
.updated-title { font-weight: 700; color: #334155; }
.table-wrap { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff; }
table { border-collapse: collapse; width: 100%; min-width: 650px; font-size: 14px; }
th { background: #0f172a; color: white; padding: 14px 16px; text-align: left; font-size: 12px; font-weight: 600; }
td { padding: 13px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
tr:nth-child(even) td { background: #fafafa; }
.value { font-weight: 700; color: #0f172a; }
.src { display: block; margin-top: 3px; font-size: 11px; font-weight: normal; color: #64748b; }
.status { display: inline-block; padding: 5px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; }
.low, .good { background: #dcfce7; color: #166534; }
.improving { background: #e0f2fe; color: #0369a1; }
.caution { background: #fef9c3; color: #854d0e; }
.high { background: #fee2e2; color: #991b1b; }
.orange { background: #ffedd5; color: #9a3412; }
.neutral { background: #f1f5f9; color: #475569; }
.pulse { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-top: 20px; }
.pulse-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 14px 10px; text-align: center; }
.pulse-arrow { font-size: 20px; font-weight: bold; color: #2563eb; }
.pulse-label { margin-top: 6px; font-size: 11px; color: #475569; font-weight: 700; }
.pulse-word { margin-top: 3px; font-size: 11px; color: #475569; }
.pulse-note { margin-top: 10px; font-size: 12px; color: #64748b; }
.meaning-box { display: grid; gap: 10px; margin-top: 18px; }
.meaning-row { border-radius: 10px; padding: 12px 16px; font-size: 14px; }
.meaning-green { background: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; }
.meaning-yellow { background: #fefce8; border: 1px solid #fef08a; color: #854d0e; }
.meaning-red { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; }
.meaning-orange { background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; }
.bottom-line { margin-top: 20px; background: linear-gradient(135deg, #f0fdf4, #f8fafc); border: 1px solid #bbf7d0; border-left: 5px solid #16a34a; border-radius: 12px; padding: 24px; }
.bottom-line-title { color: #166534; font-size: 17px; font-weight: 800; margin-bottom: 8px; text-transform: uppercase; }
.note { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-top: 20px; font-size: 13px; color: #475569; }
.note p { margin: 0 0 12px; }
.footer { background: #f8fafc; border-top: 1px solid #e2e8f0; text-align: center; padding: 28px 20px; color: #64748b; font-size: 12px; }
.footer-brand { color: #0f172a; font-size: 13px; font-weight: 800; letter-spacing: 1.5px; text-transform: uppercase; }
</style>
</head>
<body>
<div class="container">
<div class="hero">
    <div class="hero-top">
        <div>
            <div class="logo">ECONOMIC WATCH</div>
            <h1>Columbus Economic Snapshot</h1>
            <div class="hero-subtitle">Columbus, Ohio — Local Economic & Housing Monitor</div>
            <div class="hero-tigrinya">ኮሎምበስ፣ ኦሃዮ — ኢኮኖሚያዊን ናይ ገዛን ምልከታ</div>
        </div>
        <div class="report-pill">● WEEKLY REPORT / ሰሙናዊ ጸብጻብ</div>
    </div>
    <div class="hero-date">Weekly report, latest available data / ሰሙናዊ ጸብጻብ፣ ናይ ሕጂ ዝርከብ ሓበሬታ</div>
</div>

<div class="content">
<div class="updated">
    <div class="updated-title">Updated / ዝተሓደሰ</div>
    <div class="updated-date">""");

        html.append(dateText);

        html.append("""
    </div>
</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>Key Indicators / ቀንዲ መለክዒታት</h2>
</div>

<div class="metrics">
    <div class="metric-card green">
        <div class="metric-label">Columbus Unemployment</div>
        <div class="metric-value">""");

        html.append(String.format(Locale.US, "%.1f%%", unemployment));

        html.append("""
        </div>
        <div class="metric-note">Columbus / ኮሎምበስ</div>
    </div>
    <div class="metric-card orange">
        <div class="metric-label">U.S. Mortgage Rate</div>
        <div class="metric-value">""");

        html.append(String.format(Locale.US, "%.2f%%", mortgage));

        html.append("""
        </div>
        <div class="metric-note">30-year fixed, U.S. / ኣመሪካ</div>
    </div>
    <div class="metric-card">
        <div class="metric-label">Home Price Index</div>
        <div class="metric-value">""");

        html.append(String.format(Locale.US, "%.2f", homePriceIndex));

        html.append("""
        </div>
        <div class="metric-note">Columbus HPI (quarterly index)</div>
    </div>
</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>Economic Snapshot / ኢኮኖሚያዊ ሓፈሻ</h2>
</div>

<div class="table-wrap">
<table>
<tr>
    <th>Indicator / መለክዒ</th>
    <th>Current / ሕጂ</th>
    <th>Status / ኩነታት</th>
</tr>
<tr>
    <td>Economic Risk Score / ነጥቢ ኢኮኖሚያዊ ሓደጋ<span class="src">Economic Watch indicator, not an official statistic</span></td>
    <td class="value">""");

        html.append(economicRisk).append("/100");

        html.append("""
    </td>
    <td><span class="status low">Low / ትሑት</span></td>
</tr>
<tr>
    <td>Buyer Leverage Score / ነጥቢ ሓይሊ ገዛእቲ ገዛ<span class="src">Economic Watch indicator, not an official statistic</span></td>
    <td class="value">""");

        html.append(buyerLeverage).append("/100");

        html.append("""
    </td>
    <td><span class="status improving">Improving / ይመሓየሽ ኣሎ</span></td>
</tr>
<tr>
    <td>Affordability Score / ነጥቢ ዓቕሚ ክፍሊት<span class="src">Economic Watch indicator, not an official statistic</span></td>
    <td class="value">""");

        html.append(affordability).append("/100");

        html.append("""
    </td>
    <td><span class="status orange">Challenging / ኣሸጋሪ</span></td>
</tr>
<tr>
    <td>Columbus Unemployment Rate / ስራሕ ኣልቦነት ኮሎምበስ</td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.1f%%", unemployment));

        html.append("""
    </td>
    <td><span class="status low">""");

        html.append(unemploymentStatus);

        html.append("""
    </span></td>
</tr>
<tr>
    <td>Columbus Home Price / ዋጋ ገዛ ኮሎምበስ<span class="src">Median sale price, Central Ohio, July 2026 (Columbus REALTORS&reg;)</span></td>
    <td class="value">""");

        html.append(homePriceReference);

        html.append("""
    </td>
    <td><span class="status neutral">Stable / ዝተረጋጋ</span></td>
</tr>
<tr>
    <td>Central Ohio Inventory / ብዝሒ ዘሎ ገዛውቲ<span class="src">Central Ohio MLS reference figure, not live</span></td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%,d", centralOhioInventory));

        html.append("""
    </td>
    <td><span class="status improving">Improving / ይውስኽ ኣሎ</span></td>
</tr>
<tr>
    <td>Months Supply / ናይ ወርሒ ኣቕርቦት<span class="src">Central Ohio MLS reference figure, not live</span></td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.1f months", monthsSupply));

        html.append("""
    </td>
    <td><span class="status caution">Low supply / ውሑድ ኣቕርቦት</span></td>
</tr>
<tr>
    <td>Days on Market / ኣብ ዕዳጋ ዝጸንሓሉ መዓልታት<span class="src">Central Ohio MLS reference figure, not live</span></td>
    <td class="value">""");

        html.append(daysOnMarket);

        html.append("""
    </td>
    <td><span class="status improving">Improving / ይመሓየሽ ኣሎ</span></td>
</tr>
<tr>
    <td>U.S. 30-Year Mortgage Rate / ወለድ ሞርጌጅ ኣመሪካ</td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.2f%%", mortgage));

        html.append("""
    </td>
    <td><span class="status high">""");

        html.append(mortgageStatus);

        html.append("""
    </span></td>
</tr>
<tr>
    <td>Median Rent / ማእከላይ ክራይ<span class="src">Columbus city median, August 2026 (Apartment List)</span></td>
    <td class="value">""");

        html.append(medianRent);

        html.append("""
    </td>
    <td><span class="status neutral">Reference</span></td>
</tr>
<tr>
    <td>Columbus Home Price Index / መዐቀኒ ዋጋ ገዛ</td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.2f", homePriceIndex));

        html.append("""
    </td>
    <td><span class="status neutral">Latest quarter:&nbsp;""");

        html.append(hpiAsOf);

        html.append("""
    </span></td>
</tr>
<tr>
    <td>U.S. Inflation / ዕቤት ዋጋ ኣመሪካ</td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.1f%%", inflation));

        html.append("""
    </td>
    <td><span class="status caution">""");

        html.append(inflationStatus);

        html.append("""
    </span></td>
</tr>
<tr>
    <td>U.S. GDP Growth / ዕቤት GDP ኣመሪካ</td>
    <td class="value">""");

        html.append(String.format(Locale.US, "%.1f%%", gdp));

        html.append("""
    </td>
    <td><span class="status good">""");

        html.append(gdpStatus);

        html.append("""
    </span></td>
</tr>
<tr>
    <td>Federal Funds Target Rate / ወለድ Federal Funds</td>
    <td class="value">""");

        html.append(fedValue);

        html.append("<span class=\"src\">" + fedNote + "</span>");

        html.append("""
    </td>
    <td><span class="status neutral">National / ሃገራዊ</span></td>
</tr>
</table>
</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>Market Direction / ኣንፈት ዕዳጋ</h2>
</div>
<div class="pulse">
    <div class="pulse-item"><div class="pulse-arrow">↗</div><div class="pulse-label">Inventory / ክምችት</div><div class="pulse-word">Rising / ይውስኽ ኣሎ</div></div>
    <div class="pulse-item"><div class="pulse-arrow">↗</div><div class="pulse-label">Buyer Leverage / ሓይሊ</div><div class="pulse-word">Improving / ይመሓየሽ ኣሎ</div></div>
    <div class="pulse-item"><div class="pulse-arrow">→</div><div class="pulse-label">Prices / ዋጋታት</div><div class="pulse-word">Stable / ዝተረጋጋ</div></div>
    <div class="pulse-item"><div class="pulse-arrow">""");

        html.append(mortgageArrow);

        html.append("""
</div><div class="pulse-label">Mortgage Rates / ወለድ</div><div class="pulse-word">""");

        html.append(mortgageWord);

        html.append("""
</div></div>
    <div class="pulse-item"><div class="pulse-arrow">↘</div><div class="pulse-label">Affordability / ተመጣጣንነት</div><div class="pulse-word">Challenging / ኣሸጋሪ</div></div>
</div>
<div class="pulse-note">Arrows show the direction of change, not whether it is good or bad for buyers. Rising mortgage rates are worse for buyers. / ምልክታት ኣንፈት ለውጢ እዮም፣ ንገዛእቲ ጽቡቕ ወይ ሕማቕ ምዃኑ ኣየርኣዩን።</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>What It Means / ትርጉሙ እንታይ እዩ?</h2>
</div>
<div class="meaning-box">
    <div class="meaning-row meaning-green">🟢 <strong>Jobs: Healthy / ስራሕ፦ ጥዑይ</strong></div>
    <div class="meaning-row meaning-green">🟢 <strong>Inventory: Improving / ኣቕርቦት፦ ይመሓየሽ ኣሎ</strong></div>
    <div class="meaning-row meaning-green">🟢 <strong>Buyer leverage: Improving / ሓይሊ ገዛእቲ፦ ይመሓየሽ ኣሎ</strong></div>
    <div class="meaning-row meaning-yellow">🟡 <strong>Prices: Mostly stable / ዋጋ፦ ብዙሕ ኣይተቐየረን</strong></div>
    <div class="meaning-row meaning-red">🔴 <strong>Mortgage rates: High / ወለድ ሞርጌጅ፦ ልዑል</strong></div>
    <div class="meaning-row meaning-orange">🟠 <strong>Affordability: Challenging / ዓቕሚ ክፍሊት፦ ኣሸጋሪ</strong></div>
</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>🏠 Bottom Line / ቀንዲ ነጥቢ</h2>
</div>
<div class="bottom-line">
    <div class="bottom-line-title">Overall Outlook / ሓፈሻዊ ኣንፈት</div>
    <p><strong>Conditions are improving for buyers, but high mortgage rates are still the biggest obstacle.</strong></p>
    <p>ኩነታት ንገዛእቲ ገዛ በብቑሩብ ይሓይሽ ኣሎ፣ ግን ልዑል ወለድ ሞርጌጅ ገና ዓብዪ ዕንቅፋት እዩ።</p>
</div>

<div class="section-title">
    <div class="section-line"></div>
    <h2>Data Notes / ሓበሬታ ብዛዕባ ዳታ</h2>
</div>
<div class="note">
    <p><strong>LIVE DATA:</strong> Economic indicators connected to the application are retrieved from FRED when the report is generated. Unemployment is for the Columbus, OH metro area (BLS, not seasonally adjusted). Inflation, GDP growth, the 30-year mortgage rate (Freddie Mac) and the federal funds rate are U.S. national figures.</p>
    <p><strong>ቀጥታ ሓበሬታ:</strong> እቶም ናብ መተግበሪ ዝተኣሳሰሩ ኢኮኖሚያዊ መለክዒታት እቲ ሪፖርት ክፍጠር ከሎ ካብ FRED ይውሰዱ። ስራሕ ኣልቦነት ናይ ኮሎምበስን ከባብያን እዩ፣ ዕቤት ዋጋ፣ GDP፣ ወለድ ሞርጌጅን Federal Fundsን ሃገራዊ (ኣመሪካ) እዮም።</p>
    <p><strong>CENTRAL OHIO MLS:</strong> The 6,193 inventory, 2.4 months supply and approximately 42-44 days on market are Central Ohio/local reference figures. They are not currently live-connected to FRED and may lag. The ~$350K home price is the Central Ohio median sales price for July 2026 (Columbus REALTORS&reg;). The ~$1,308 median rent is the Columbus city median for August 2026 (Apartment List).</p>
    <p><strong>ምንጪ:</strong> ዋጋ ገዛ፣ ክራይ፣ ብዝሒ ገዛውቲ፣ ናይ ወርሒ ኣቕርቦትን መዓልታት ኣብ ዕዳጋን ካብ ማእከላይ ኦሃዮ ዝተወስዱ መወከሲ ሓበሬታ እዮም፣ ቀጥታ ካብ FRED ኣይኮኑን።</p>
    <p><strong>ኣገዳሲ:</strong> Columbus city/area data and Central Ohio MLS data are kept separate. The Columbus Home Price Index is an index value, not a dollar home price.</p>
    <p><strong>DATA AS OF / ዕለት ናይ ሓበሬታ:</strong> """);

        html.append(asOfLine);

        html.append("""
</p>
    <p>Some indicators are updated monthly or quarterly, so their latest observation can be weeks or months old. This report shows the latest available data, not real-time data.</p>
    <p><strong>SCORES:</strong> The Economic Risk, Buyer Leverage and Affordability scores are Economic Watch indicators. They are not official government statistics.</p>
    <p><strong>ነጥቢታት:</strong> እዞም ነጥቢታት ናይ Economic Watch መርእያታት እዮም፣ ወግዓዊ ስታቲስቲክስ ኣይኮኑን።</p>
</div>
</div>

<div class="footer">
    <div class="footer-brand">ECONOMIC WATCH</div>
    <div class="footer-small">Columbus, Ohio • Automated Weekly Economic Report</div>
    <div class="footer-small">ኢኮኖሚያዊ ምልከታ • ኮሎምበስ፣ ኦሃዮ • ሰሙናዊ ጸብጻብ</div>
</div>
</div>
</body>
</html>
""");

        return html.toString();
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
}
