import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmailReportGenerator {

    public static String generate(EconomicSnapshot snapshot) {

        LocalDate date = snapshot.getRecordedOn();

        // LIVE VALUES (from FRED)
        // unemployment = Columbus, OH metro area (not seasonally adjusted)
        // gdp, inflation, fedFunds, mortgage = U.S. national
        // homePriceIndex = Columbus house price index
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

        // ORIGINAL REPORT SCORES — unchanged
        int economicRisk = 18;
        int buyerLeverage = 60;
        int affordability = 75;

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

* {
    box-sizing: border-box;
}

body {
    margin: 0;
    padding: 0;
    background: #eef3f8;
    font-family: Arial, Helvetica, sans-serif;
    color: #1f2937;
    line-height: 1.55;
}

.container {
    max-width: 920px;
    margin: 28px auto;
    background: #ffffff;
    border-radius: 18px;
    overflow: hidden;
    box-shadow: 0 10px 35px rgba(15, 23, 42, 0.12);
}

/* =========================
   HERO HEADER
   ========================= */

.hero {
    background: linear-gradient(135deg, #172554, #1e3a8a 55%, #2563eb);
    color: white;
    padding: 38px 34px 34px;
    position: relative;
}

.hero-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
}

.logo {
    font-size: 13px;
    font-weight: bold;
    letter-spacing: 2px;
    color: #bfdbfe;
    margin-bottom: 9px;
}

.hero h1 {
    margin: 0;
    font-size: 32px;
    line-height: 1.15;
    letter-spacing: -0.5px;
}

.hero-subtitle {
    margin-top: 10px;
    color: #dbeafe;
    font-size: 16px;
}

.hero-tigrinya {
    margin-top: 4px;
    color: #dbeafe;
    font-size: 14px;
}

.live-pill {
    background: #dcfce7;
    color: #166534;
    border-radius: 20px;
    padding: 8px 13px;
    font-size: 12px;
    font-weight: bold;
    white-space: nowrap;
}

.hero-date {
    margin-top: 25px;
    color: #bfdbfe;
    font-size: 13px;
}

/* =========================
   CONTENT
   ========================= */

.content {
    padding: 30px 34px 35px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 32px 0 16px;
}

.section-title:first-child {
    margin-top: 0;
}

.section-line {
    height: 3px;
    width: 42px;
    background: #2563eb;
    border-radius: 4px;
}

.section-title h2 {
    margin: 0;
    color: #172554;
    font-size: 19px;
    letter-spacing: 0.2px;
}

/* =========================
   KEY METRIC CARDS
   ========================= */

.metrics {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 14px;
    margin: 24px 0 28px;
}

.metric-card {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 13px;
    padding: 18px;
    position: relative;
    overflow: hidden;
}

.metric-card::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: #2563eb;
}

.metric-card.green::before {
    background: #16a34a;
}

.metric-card.orange::before {
    background: #f97316;
}

.metric-label {
    color: #64748b;
    font-size: 12px;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.metric-value {
    margin-top: 6px;
    font-size: 27px;
    font-weight: bold;
    color: #172554;
}

.metric-note {
    margin-top: 3px;
    font-size: 12px;
    color: #64748b;
}

/* =========================
   UPDATED BOX
   ========================= */

.updated {
    background: #eff6ff;
    border: 1px solid #bfdbfe;
    border-left: 5px solid #2563eb;
    border-radius: 10px;
    padding: 14px 17px;
    margin-bottom: 25px;
}

.updated-title {
    font-weight: bold;
    color: #1e3a8a;
}

.updated-date {
    color: #475569;
}

/* =========================
   TABLE
   ========================= */

.table-wrap {
    overflow-x: auto;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
}

table {
    border-collapse: collapse;
    width: 100%;
    min-width: 650px;
    font-size: 14px;
}

th {
    background: #1e3a8a;
    color: white;
    padding: 13px 12px;
    text-align: left;
    font-size: 12px;
    letter-spacing: 0.3px;
}

td {
    padding: 12px;
    border-bottom: 1px solid #e5e7eb;
    vertical-align: middle;
}

tr:last-child td {
    border-bottom: none;
}

tr:nth-child(even) td {
    background: #f8fafc;
}

tr:hover td {
    background: #eff6ff;
}

.value {
    font-weight: bold;
    color: #172554;
}

/* =========================
   STATUS BADGES
   ========================= */

.status {
    display: inline-block;
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: bold;
    white-space: nowrap;
}

.low,
.good {
    background: #dcfce7;
    color: #166534;
}

.improving {
    background: #dbeafe;
    color: #1e40af;
}

.caution {
    background: #fef3c7;
    color: #92400e;
}

.high {
    background: #fee2e2;
    color: #991b1b;
}

.orange {
    background: #ffedd5;
    color: #9a3412;
}

.neutral {
    background: #f1f5f9;
    color: #475569;
}

/* =========================
   MARKET PULSE
   ========================= */

.pulse {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
    margin-top: 18px;
}

.pulse-item {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 13px 8px;
    text-align: center;
}

.pulse-arrow {
    font-size: 21px;
    font-weight: bold;
    color: #2563eb;
}

.pulse-label {
    margin-top: 4px;
    font-size: 11px;
    color: #64748b;
    font-weight: bold;
}

/* =========================
   MEANING SECTION
   ========================= */

.meaning-box {
    display: grid;
    gap: 9px;
    margin-top: 15px;
}

.meaning-row {
    border-radius: 9px;
    padding: 13px 15px;
    border: 1px solid transparent;
}

.meaning-green {
    background: #f0fdf4;
    border-color: #bbf7d0;
    color: #166534;
}

.meaning-yellow {
    background: #fffbeb;
    border-color: #fde68a;
    color: #92400e;
}

.meaning-red {
    background: #fef2f2;
    border-color: #fecaca;
    color: #991b1b;
}

.meaning-orange {
    background: #fff7ed;
    border-color: #fed7aa;
    color: #9a3412;
}

/* =========================
   BOTTOM LINE
   ========================= */

.bottom-line {
    margin-top: 17px;
    background: linear-gradient(135deg, #eff6ff, #f8fbff);
    border: 1px solid #bfdbfe;
    border-left: 6px solid #2563eb;
    border-radius: 12px;
    padding: 22px;
}

.bottom-line-title {
    color: #1e40af;
    font-size: 19px;
    font-weight: bold;
    margin-bottom: 8px;
}

.bottom-line p {
    margin: 8px 0;
}

/* =========================
   DATA NOTES
   ========================= */

.note {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 11px;
    padding: 17px 18px;
    margin-top: 15px;
    font-size: 13px;
    color: #475569;
}

.note p {
    margin: 0 0 11px;
}

.note p:last-child {
    margin-bottom: 0;
}

.note strong {
    color: #334155;
}

/* =========================
   FOOTER
   ========================= */

.footer {
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
    text-align: center;
    padding: 25px 20px;
    color: #64748b;
    font-size: 12px;
}

.footer-brand {
    color: #1e3a8a;
    font-size: 14px;
    font-weight: bold;
    letter-spacing: 1px;
}

.footer-small {
    margin-top: 6px;
}

/* =========================
   MOBILE
   ========================= */

@media screen and (max-width: 700px) {

    .container {
        margin: 0;
        border-radius: 0;
    }

    .hero {
        padding: 28px 20px;
    }

    .hero-top {
        display: block;
    }

    .hero h1 {
        font-size: 25px;
    }

    .live-pill {
        display: inline-block;
        margin-top: 15px;
    }

    .content {
        padding: 23px 15px 28px;
    }

    .metrics {
        grid-template-columns: 1fr;
    }

    .pulse {
        grid-template-columns: repeat(2, 1fr);
    }

    .section-title h2 {
        font-size: 17px;
    }

    table {
        font-size: 12px;
    }

    th,
    td {
        padding: 9px 7px;
    }
}

</style>

</head>

<body>

<div class="container">

<!-- =========================
     HERO
     ========================= -->

<div class="hero">

    <div class="hero-top">

        <div>

            <div class="logo">
                ECONOMIC WATCH
            </div>

            <h1>
                Columbus Economic Snapshot
            </h1>

            <div class="hero-subtitle">
                Columbus, Ohio — Local Economic & Housing Monitor
            </div>

            <div class="hero-tigrinya">
                ኮሎምበስ፣ ኦሃዮ — ኢኮኖሚያዊን ናይ ገዛን ምልከታ
            </div>

        </div>

        <div class="live-pill">
            ● WEEKLY REPORT / ሰሙናዊ ጸብጻብ
        </div>

    </div>

    <div class="hero-date">
        Weekly economic intelligence report / ሰሙናዊ ኢኮኖሚያዊ ጸብጻብ
    </div>

</div>

<div class="content">

<!-- =========================
     UPDATED
     ========================= -->

<div class="updated">

    <div class="updated-title">
        Updated / ዝተሓደሰ
    </div>

    <div class="updated-date">
        """);

        html.append(dateText);

        html.append("""
    </div>

</div>

<!-- =========================
     KEY METRICS
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        Key Indicators / ቀንዲ መለክዒታት
    </h2>

</div>

<div class="metrics">

    <div class="metric-card green">

        <div class="metric-label">
            Columbus Unemployment
        </div>

        <div class="metric-value">
            """);

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        unemployment
                )
        );

        html.append("""
        </div>

        <div class="metric-note">
            Columbus / ኮሎምበስ
        </div>

    </div>

    <div class="metric-card orange">

        <div class="metric-label">
            U.S. Mortgage Rate
        </div>

        <div class="metric-value">
            """);

        html.append(
                String.format(
                        Locale.US,
                        "%.2f%%",
                        mortgage
                )
        );

        html.append("""
        </div>

        <div class="metric-note">
            30-year rate, U.S. / ኣመሪካ
        </div>

    </div>

    <div class="metric-card">

        <div class="metric-label">
            Home Price Index
        </div>

        <div class="metric-value">
            """);

        html.append(
                String.format(
                        Locale.US,
                        "%.2f",
                        homePriceIndex
                )
        );

        html.append("""
        </div>

        <div class="metric-note">
            Columbus HPI
        </div>

    </div>

</div>

<!-- =========================
     ECONOMIC SNAPSHOT
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        Economic Snapshot / ኢኮኖሚያዊ ሓፈሻ
    </h2>

</div>

<div class="table-wrap">

<table>

<tr>
    <th>Indicator / መለክዒ</th>
    <th>Current / ሕጂ</th>
    <th>Status / ኩነታት</th>
</tr>

<!-- Economic Risk -->

<tr>

<td>
Economic Risk / ኢኮኖሚያዊ ሓደጋ
</td>

<td class="value">
""");

        html.append(economicRisk).append("/100");

        html.append("""
</td>

<td>
<span class="status low">
Low / ትሑት
</span>
</td>

</tr>

<!-- Buyer Leverage -->

<tr>

<td>
Buyer Leverage / ሓይሊ ገዛእቲ ገዛ
</td>

<td class="value">
""");

        html.append(buyerLeverage).append("/100");

        html.append("""
</td>

<td>
<span class="status improving">
Improving / ይመሓየሽ ኣሎ
</span>
</td>

</tr>

<!-- Affordability -->

<tr>

<td>
Affordability / ዓቕሚ ክፍሊት
</td>

<td class="value">
""");

        html.append(affordability).append("/100");

        html.append("""
</td>

<td>
<span class="status orange">
Challenging / ኣሸጋሪ
</span>
</td>

</tr>

<!-- Unemployment (Columbus) -->

<tr>

<td>
Columbus Unemployment Rate / ስራሕ ኣልቦነት ኮሎምበስ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        unemployment
                )
        );

        html.append("""
</td>

<td>
<span class="status low">
""");

        html.append(unemploymentStatus);

        html.append("""
</span>
</td>

</tr>

<!-- Columbus Home Price -->

<tr>

<td>
Columbus Home Price / ዋጋ ገዛ ኮሎምበስ
</td>

<td class="value">
""");

        html.append(homePriceReference);

        html.append("""
</td>

<td>
<span class="status neutral">
Stable / ዝተረጋጋ
</span>
</td>

</tr>

<!-- Central Ohio Inventory -->

<tr>

<td>
Central Ohio Inventory / ብዝሒ ዘሎ ገዛውቲ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%,d",
                        centralOhioInventory
                )
        );

        html.append("""
</td>

<td>
<span class="status improving">
Improving / ይውስኽ ኣሎ
</span>
</td>

</tr>

<!-- Months Supply -->

<tr>

<td>
Months Supply / ናይ ወርሒ ኣቕርቦት
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f months",
                        monthsSupply
                )
        );

        html.append("""
</td>

<td>
<span class="status caution">
Low supply / ውሑድ ኣቕርቦት
</span>
</td>

</tr>

<!-- Days on Market -->

<tr>

<td>
Days on Market / ኣብ ዕዳጋ ዝጸንሓሉ መዓልታት
</td>

<td class="value">
""");

        html.append(daysOnMarket);

        html.append("""
</td>

<td>
<span class="status improving">
Improving / ይመሓየሽ ኣሎ
</span>
</td>

</tr>

<!-- Mortgage (U.S.) -->

<tr>

<td>
U.S. 30-Year Mortgage Rate / ወለድ ሞርጌጅ ኣመሪካ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f%%",
                        mortgage
                )
        );

        html.append("""
</td>

<td>
<span class="status high">
""");

        html.append(mortgageStatus);

        html.append("""
</span>
</td>

</tr>

<!-- Median Rent -->

<tr>

<td>
Median Rent / ማእከላይ ክራይ
</td>

<td class="value">
""");

        html.append(medianRent);

        html.append("""
</td>

<td>
<span class="status neutral">
Reference
</span>
</td>

</tr>

<!-- HPI -->

<tr>

<td>
Columbus Home Price Index / መዐቀኒ ዋጋ ገዛ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f",
                        homePriceIndex
                )
        );

        html.append("""
</td>

<td>
<span class="status improving">
Latest / ሓድሽ
</span>
</td>

</tr>

<!-- Inflation (U.S.) -->

<tr>

<td>
U.S. Inflation / ዕቤት ዋጋ ኣመሪካ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        inflation
                )
        );

        html.append("""
</td>

<td>
<span class="status caution">
""");

        html.append(inflationStatus);

        html.append("""
</span>
</td>

</tr>

<!-- GDP (U.S.) -->

<tr>

<td>
U.S. GDP Growth / ዕቤት GDP ኣመሪካ
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        gdp
                )
        );

        html.append("""
</td>

<td>
<span class="status good">
""");

        html.append(gdpStatus);

        html.append("""
</span>
</td>

</tr>

<!-- Federal Funds (U.S.) -->

<tr>

<td>
Federal Funds Rate / ወለድ Federal Funds
</td>

<td class="value">
""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f%%",
                        fedFunds
                )
        );

        html.append("""
</td>

<td>
<span class="status neutral">
National / ሃገራዊ
</span>
</td>

</tr>

</table>

</div>

<!-- =========================
     MARKET DIRECTION
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        Market Direction / ኣንፈት ዕዳጋ
    </h2>

</div>

<div class="pulse">

    <div class="pulse-item">

        <div class="pulse-arrow">↗</div>

        <div class="pulse-label">
            Inventory / ክምችት ቤት
        </div>

    </div>

    <div class="pulse-item">

        <div class="pulse-arrow">↗</div>

        <div class="pulse-label">
            Buyer Leverage / ሓይሊ ዕዳጋ ገዛእቲ
        </div>

    </div>

    <div class="pulse-item">

        <div class="pulse-arrow">→</div>

        <div class="pulse-label">
            Prices / ዋጋታት
        </div>

    </div>

    <div class="pulse-item">

        <div class="pulse-arrow">↗</div>

        <div class="pulse-label">
            Mortgage Rates / መጠን ወለድ ሞርጌጅ
        </div>

    </div>

    <div class="pulse-item">

        <div class="pulse-arrow">↘</div>

        <div class="pulse-label">
            Affordability / ተመጣጣንነት ዋጋ
        </div>

    </div>

</div>

<!-- =========================
     WHAT IT MEANS
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        What It Means / ትርጉሙ እንታይ እዩ?
    </h2>

</div>

<div class="meaning-box">

    <div class="meaning-row meaning-green">
        🟢 <strong>
        Jobs: Healthy / ስራሕ፦ ጥዑይ
        </strong>
    </div>

    <div class="meaning-row meaning-green">
        🟢 <strong>
        Inventory: Improving / ኣቕርቦት፦ ይመሓየሽ ኣሎ
        </strong>
    </div>

    <div class="meaning-row meaning-green">
        🟢 <strong>
        Buyer leverage: Improving / ሓይሊ ገዛእቲ፦ ይመሓየሽ ኣሎ
        </strong>
    </div>

    <div class="meaning-row meaning-yellow">
        🟡 <strong>
        Prices: Mostly stable / ዋጋ፦ ብዙሕ ኣይተቐየረን
        </strong>
    </div>

    <div class="meaning-row meaning-red">
        🔴 <strong>
        Mortgage rates: High / ወለድ ሞርጌጅ፦ ልዑል
        </strong>
    </div>

    <div class="meaning-row meaning-orange">
        🟠 <strong>
        Affordability: Challenging / ዓቕሚ ክፍሊት፦ ኣሸጋሪ
        </strong>
    </div>

</div>

<!-- =========================
     BOTTOM LINE
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        🏠 Bottom Line / ቀንዲ ነጥቢ
    </h2>

</div>

<div class="bottom-line">

    <div class="bottom-line-title">
        Overall Outlook / ሓፈሻዊ ኣንፈት
    </div>

    <p>
        <strong>
        Conditions are improving for buyers,
        but high mortgage rates are still the
        biggest obstacle.
        </strong>
    </p>

    <p>
        ኩነታት ንገዛእቲ ገዛ በብቑሩብ ይሓይሽ ኣሎ፣
        ግን ልዑል ወለድ ሞርጌጅ ገና
        ዓብዪ ዕንቅፋት እዩ።
    </p>

</div>

<!-- =========================
     DATA NOTES
     ========================= -->

<div class="section-title">

    <div class="section-line"></div>

    <h2>
        Data Notes / ሓበሬታ ብዛዕባ ዳታ
    </h2>

</div>

<div class="note">

    <p>
        <strong>LIVE DATA:</strong>
        Economic indicators connected to the application
        are retrieved from FRED when the report is generated.
        Unemployment is for the Columbus, OH metro area
        (BLS, not seasonally adjusted). Inflation, GDP growth,
        the 30-year mortgage rate (Freddie Mac) and the
        federal funds rate (daily effective rate) are
        U.S. national figures.
    </p>

    <p>
        <strong>ቀጥታ ሓበሬታ:</strong>
        እቶም ናብ መተግበሪ ዝተኣሳሰሩ
        ኢኮኖሚያዊ መለክዒታት እቲ ሪፖርት
        ክፍጠር ከሎ ካብ FRED ይውሰዱ።
        ስራሕ ኣልቦነት ናይ ኮሎምበስን ከባብያን እዩ፣
        ዕቤት ዋጋ፣ GDP፣ ወለድ ሞርጌጅን Federal Fundsን
        ሃገራዊ (ኣመሪካ) እዮም።
    </p>

    <p>
        <strong>CENTRAL OHIO MLS:</strong>
        The 6,193 inventory, 2.4 months supply and
        approximately 42-44 days on market are
        Central Ohio/local reference figures. They are not
        currently live-connected to FRED and may lag.
        The ~$350K home price is the Central Ohio median
        sales price for July 2026 (Columbus REALTORS&reg;).
        The ~$1,308 median rent is the Columbus city median
        for August 2026 (Apartment List).
    </p>

    <p>
        <strong>ምንጪ:</strong>
        ዋጋ ገዛ፣ ክራይ፣ ብዝሒ ገዛውቲ፣ ናይ ወርሒ ኣቕርቦትን
        መዓልታት ኣብ ዕዳጋን ካብ ማእከላይ ኦሃዮ ዝተወስዱ
        መወከሲ ሓበሬታ እዮም፣ ቀጥታ ካብ FRED ኣይኮኑን።
    </p>

    <p>
        <strong>ኣገዳሲ:</strong>
        Columbus city/area data and Central Ohio
        MLS data are kept separate.
    </p>

    <p>
        The Columbus Home Price Index is an index value,
        not a dollar home price.
    </p>

    <p>
        Some source observations are from July/August 2026
        because those are the latest published observations
        available to the application.
    </p>

</div>

</div>

<!-- =========================
     FOOTER
     ========================= -->

<div class="footer">

    <div class="footer-brand">
        ECONOMIC WATCH
    </div>

    <div class="footer-small">
        Columbus, Ohio • Automated Weekly Economic Report
    </div>

    <div class="footer-small">
      ኢኮኖሚያዊ ምልከታ • ኮሎምበስ፣ ኦሃዮ • ሰሙናዊ ጸብጻብ
    </div>

</div>

</div>

</body>
</html>
""");

        return html.toString();
    }
}
