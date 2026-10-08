import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmailReportGenerator {

    public static String generate(EconomicSnapshot snapshot) {

        LocalDate date = snapshot.getRecordedOn();

        // LIVE VALUES (from FRED)
        double unemployment = snapshot.getUnemploymentRatePct();
        double gdp = snapshot.getGdpGrowthPct();
        double inflation = snapshot.getInflationPct();
        double fedFunds = snapshot.getFedFundsRatePct();
        double mortgage = snapshot.getMortgageRatePct();
        double homePriceIndex = snapshot.getHomePriceIndex();

        // CENTRAL OHIO REFERENCE VALUES
        int centralOhioInventory = 6193;
        double monthsSupply = 2.4;
        String daysOnMarket = "~42-44 days";
        String medianRent = "~$1,308";
        String homePriceReference = "~$350K";

        // APP-CALCULATED SCORES
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
    background: #f4f7fa;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    color: #1e293b;
    line-height: 1.6;
}

.container {
    max-width: 920px;
    margin: 32px auto;
    background: #ffffff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
    border: 1px solid #e2e8f0;
}

/* =========================
   HERO HEADER
   ========================= */

.hero {
    background: linear-gradient(135deg, #0f172a, #1e3a8a 60%, #2563eb);
    color: white;
    padding: 40px 36px 36px;
    position: relative;
}

.hero-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 20px;
}

.logo {
    font-size: 12px;
    font-weight: 800;
    letter-spacing: 2.5px;
    color: #93c5fd;
    margin-bottom: 10px;
    text-transform: uppercase;
}

.hero h1 {
    margin: 0;
    font-size: 30px;
    line-height: 1.2;
    letter-spacing: -0.5px;
    font-weight: 700;
}

.hero-subtitle {
    margin-top: 8px;
    color: #cbd5e1;
    font-size: 15px;
}

.hero-tigrinya {
    margin-top: 4px;
    color: #93c5fd;
    font-size: 14px;
    font-weight: 500;
}

.report-pill {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #ffffff;
    border-radius: 20px;
    padding: 8px 14px;
    font-size: 12px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0.5px;
}

.hero-date {
    margin-top: 24px;
    color: #93c5fd;
    font-size: 13px;
    font-weight: 500;
}

/* =========================
   CONTENT
   ========================= */

.content {
    padding: 32px 36px 40px;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 36px 0 18px;
}

.section-title:first-child {
    margin-top: 0;
}

.section-line {
    height: 4px;
    width: 36px;
    background: #2563eb;
    border-radius: 4px;
}

.section-title h2 {
    margin: 0;
    color: #0f172a;
    font-size: 18px;
    letter-spacing: -0.2px;
    font-weight: 700;
}

/* =========================
   KEY METRIC CARDS
   ========================= */

.metrics {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin: 24px 0 32px;
}

.metric-card {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 20px;
    position: relative;
    overflow: hidden;
    transition: transform 0.2s ease;
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
    background: #ea580c;
}

.metric-label {
    color: #64748b;
    font-size: 11px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.8px;
}

.metric-value {
    margin-top: 8px;
    font-size: 26px;
    font-weight: 800;
    color: #0f172a;
    letter-spacing: -0.5px;
}

.metric-note {
    margin-top: 4px;
    font-size: 12px;
    color: #64748b;
}

/* =========================
   UPDATED BOX
   ========================= */

.updated {
    background: #f1f5f9;
    border: 1px solid #cbd5e1;
    border-left: 4px solid #0f172a;
    border-radius: 8px;
    padding: 12px 16px;
    margin-bottom: 28px;
    font-size: 13px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.updated-title {
    font-weight: 700;
    color: #334155;
}

.updated-date {
    color: #475569;
    font-weight: 600;
}

/* =========================
   TABLE
   ========================= */

.table-wrap {
    overflow-x: auto;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    background: #ffffff;
}

table {
    border-collapse: collapse;
    width: 100%;
    min-width: 650px;
    font-size: 14px;
}

th {
    background: #0f172a;
    color: white;
    padding: 14px 16px;
    text-align: left;
    font-size: 12px;
    letter-spacing: 0.5px;
    font-weight: 600;
}

td {
    padding: 13px 16px;
    border-bottom: 1px solid #f1f5f9;
    vertical-align: middle;
}

tr:last-child td {
    border-bottom: none;
}

tr:nth-child(even) td {
    background: #fafafa;
}

.value {
    font-weight: 700;
    color: #0f172a;
}

/* =========================
   STATUS BADGES
   ========================= */

.status {
    display: inline-block;
    padding: 5px 10px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 700;
    white-space: nowrap;
    letter-spacing: 0.3px;
}

.low,
.good {
    background: #dcfce7;
    color: #166534;
}

.improving {
    background: #e0f2fe;
    color: #0369a1;
}

.caution {
    background: #fef9c3;
    color: #854d0e;
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
   MARKET DIRECTION
   ========================= */

.pulse {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
    margin-top: 20px;
}

.pulse-item {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 14px 10px;
    text-align: center;
}

.pulse-arrow {
    font-size: 20px;
    font-weight: bold;
    color: #2563eb;
}

.pulse-label {
    margin-top: 6px;
    font-size: 11px;
    color: #475569;
    font-weight: 700;
}

/* =========================
   MEANING SECTION
   ========================= */

.meaning-box {
    display: grid;
    gap: 10px;
    margin-top: 18px;
}

.meaning-row {
    border-radius: 10px;
    padding: 12px 16px;
    border: 1px solid transparent;
    font-size: 14px;
}

.meaning-green {
    background: #f0fdf4;
    border-color: #bbf7d0;
    color: #166534;
}

.meaning-yellow {
    background: #fefce8;
    border-color: #fef08a;
    color: #854d0e;
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
    margin-top: 20px;
    background: linear-gradient(135deg, #f0fdf4, #f8fafc);
    border: 1px solid #bbf7d0;
    border-left: 5px solid #16a34a;
    border-radius: 12px;
    padding: 24px;
}

.bottom-line-title {
    color: #166534;
    font-size: 17px;
    font-weight: 800;
    margin-bottom: 8px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.bottom-line p {
    margin: 8px 0;
    font-size: 15px;
}

/* =========================
   DATA NOTES
   ========================= */

.note {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 20px;
    margin-top: 20px;
    font-size: 13px;
    color: #475569;
}

.note p {
    margin: 0 0 12px;
}

.note p:last-child {
    margin-bottom: 0;
}

.note strong {
    color: #0f172a;
}

/* =========================
   FOOTER
   ========================= */

.footer {
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
    text-align: center;
    padding: 28px 20px;
    color: #64748b;
    font-size: 12px;
}

.footer-brand {
    color: #0f172a;
    font-size: 13px;
    font-weight: 800;
    letter-spacing: 1.5px;
    text-transform: uppercase;
}

.footer-small {
    margin-top: 6px;
    font-weight: 500;
}

/* =========================
   MOBILE
   ========================= */

@media screen and (max-width: 700px) {

    .container {
        margin: 0;
        border-radius: 0;
        border: none;
    }

    .hero {
        padding: 30px 20px;
    }

    .hero-top {
        display: block;
    }

    .hero h1 {
        font-size: 24px;
    }

    .report-pill {
        display: inline-block;
        margin-top: 14px;
    }

    .content {
        padding: 24px 16px 30px;
    }

    .metrics {
        grid-template-columns: 1fr;
    }

    .pulse {
        grid-template-columns: repeat(2, 1fr);
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

        <div class="report-pill">
            ● WEEKLY REPORT / ሰሙናዊ ጸብጻብ
        </div>

    </div>

    <div class="hero-date">
        Latest Available Data / ዝተሓደሰ መረዳእታ
    </div>

</div>

<div class="content">

<!-- =========================
      UPDATED
      ========================= -->

<div class="updated">
    <div class="updated-title">Updated / ዝተሓደሰ</div>
    <div class="updated-date">""");

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
            30-year fixed / 30 ዓመት
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
            Columbus HPI (Q2)
        </div>
    </div>

</div>

<!-- =========================
      ECONOMIC SNAPSHOT TABLE
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
<td>Economic Risk Score / ኢኮኖሚያዊ ሓደጋ (ရመ)</td>
<td class="value">""");

        html.append(economicRisk).append("/100");

        html.append("""
</td>
<td><span class="status low">Low / ትሑት</span></td>
</tr>

<!-- Buyer Leverage -->
<tr>
<td>Buyer Leverage Score / ሓይሊ ገዛእቲ ገዛ (ရመ)</td>
<td class="value">""");

        html.append(buyerLeverage).append("/100");

        html.append("""
</td>
<td><span class="status improving">Improving / ይመሓየሽ ኣሎ</span></td>
</tr>

<!-- Affordability -->
<tr>
<td>Affordability Score / ዓቕሚ ክፍሊት (ရመ)</td>
<td class="value">""");

        html.append(affordability).append("/100");

        html.append("""
</td>
<td><span class="status orange">Challenging / ኣሸጋሪ</span></td>
</tr>

<!-- Unemployment -->
<tr>
<td>Columbus Unemployment Rate / ስራሕ ኣልቦነት ኮሎምበስ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        unemployment
                )
        );

        html.append("""
</td>
<td><span class="status low">""");

        html.append(unemploymentStatus);

        html.append("""
</span></td>
</tr>

<!-- Columbus Home Price -->
<tr>
<td>Columbus Home Price / ዋጋ ገዛ ኮሎምበስ</td>
<td class="value">""");

        html.append(homePriceReference);

        html.append("""
</td>
<td><span class="status neutral">Stable / ዝተረጋጋ</span></td>
</tr>

<!-- Central Ohio Inventory -->
<tr>
<td>Central Ohio Inventory / ብዝሒ ዘሎ ገዛውቲ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%,d",
                        centralOhioInventory
                )
        );

        html.append("""
</td>
<td><span class="status improving">Improving / ይውስኽ ኣሎ</span></td>
</tr>

<!-- Months Supply -->
<tr>
<td>Months Supply / ናይ ወርሒ ኣቕርቦት</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f mos",
                        monthsSupply
                )
        );

        html.append("""
</td>
<td><span class="status caution">Low supply / ውሑድ ኣቕርቦት</span></td>
</tr>

<!-- Days on Market -->
<tr>
<td>Days on Market / ኣብ ዕዳጋ ዝጸንሓሉ መዓልታት</td>
<td class="value">""");

        html.append(daysOnMarket);

        html.append("""
</td>
<td><span class="status improving">Improving / ይመሓየሽ ኣሎ</span></td>
</tr>

<!-- Mortgage -->
<tr>
<td>U.S. 30-Year Mortgage Rate / ወለድ ሞርጌጅ ኣመሪካ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f%%",
                        mortgage
                )
        );

        html.append("""
</td>
<td><span class="status high">""");

        html.append(mortgageStatus);

        html.append("""
</span></td>
</tr>

<!-- Median Rent -->
<tr>
<td>Median Rent / ማእከላይ ክራይ</td>
<td class="value">""");

        html.append(medianRent);

        html.append("""
</td>
<td><span class="status neutral">Reference</span></td>
</tr>

<!-- HPI -->
<tr>
<td>Columbus Home Price Index / መዐቀኒ ዋጋ ገዛ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f",
                        homePriceIndex
                )
        );

        html.append("""
</td>
<td><span class="status improving">Latest / ሓድሽ</span></td>
</tr>

<!-- Inflation -->
<tr>
<td>U.S. Inflation / ዕቤት ዋጋ ኣመሪካ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        inflation
                )
        );

        html.append("""
</td>
<td><span class="status caution">""");

        html.append(inflationStatus);

        html.append("""
</span></td>
</tr>

<!-- GDP -->
<tr>
<td>U.S. GDP Growth / ዕቤት GDP ኣመሪካ</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.1f%%",
                        gdp
                )
        );

        html.append("""
</td>
<td><span class="status good">""");

        html.append(gdpStatus);

        html.append("""
</span></td>
</tr>

<!-- Federal Funds Rate -->
<tr>
<td>Federal Funds Target / ዕላማ ወለድ Federal Funds</td>
<td class="value">""");

        html.append(
                String.format(
                        Locale.US,
                        "%.2f%% (Target)",
                        fedFunds
                )
        );

        html.append("""
</td>
<td><span class="status neutral">National / ሃገራዊ</span></td>
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
        <div class="pulse-label">Inventory / ክምችት</div>
    </div>
    <div class="pulse-item">
        <div class="pulse-arrow">↗</div>
        <div class="pulse-label">Buyer Leverage / ሓይሊ</div>
    </div>
    <div class="pulse-item">
        <div class="pulse-arrow">→</div>
        <div class="pulse-label">Prices / ዋጋታት</div>
    </div>
    <div class="pulse-item">
        <div class="pulse-arrow">↗</div>
        <div class="pulse-label">Mortgage Rates / ወለድ</div>
    </div>
    <div class="pulse-item">
        <div class="pulse-arrow">↘</div>
        <div class="pulse-label">Affordability / ተመጣጣንነት</div>
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
        🟢 <strong>Jobs: Healthy / ስራሕ፦ ጥዑይ</strong>
    </div>
    <div class="meaning-row meaning-green">
        🟢 <strong>Inventory: Improving / ኣቕርቦት፦ ይመሓየሽ ኣሎ</strong>
    </div>
    <div class="meaning-row meaning-green">
        🟢 <strong>Buyer leverage: Improving / ሓይሊ ገዛእቲ፦ ይመሓየሽ ኣሎ</strong>
    </div>
    <div class="meaning-row meaning-yellow">
        🟡 <strong>Prices: Mostly stable / ዋጋ፦ ብዙሕ ኣይተቐየረን</strong>
    </div>
    <div class="meaning-row meaning-red">
        🔴 <strong>Mortgage rates: High / ወለድ ሞርጌጅ፦ ልዑል</strong>
    </div>
    <div class="meaning-row meaning-orange">
        🟠 <strong>Affordability: Challenging / ዓቕሚ ክፍሊት፦ ኣሸጋሪ</strong>
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
        Data Notes / መረጋገጺ ሓበሬታ
    </h2>
</div>

<div class="note">
    <p>
        <strong>LIVE DATA (FRED):</strong>
        Economic indicators such as national inflation, GDP growth, U.S. 30-year mortgage rates (Freddie Mac), and Columbus metro unemployment are dynamically retrieved via FRED.
    </p>
    <p>
        <strong>ቀጥታ ሓበሬታ (FRED):</strong>
        እቶም ኢኮኖሚያዊ መለክዒታት ከም ስራሕ ኣልቦነት ኮሎምበስ፣ መጠን ወለድ ሞርጌጅ፣ GDPን ዕቤት ዋጋን ካብ FRED ብቐጥታ ዝተወስዱ እዮም።
    </p>
    <p>
        <strong>CENTRAL OHIO MLS & REFERENCE METRICS:</strong>
        Inventory counts (6,193), months of supply (2.4), and days on market (~42–44 days) are local Central Ohio MLS reference figures. The ~$350K home price reflects the Columbus REALTORS&reg; median sales price (July), and median rent (~$1,308) reflects Apartment List data (August). Risk, Leverage, and Affordability figures are internal application scoring metrics.
    </p>
    <p>
        <strong>ምንጪ ማእከላይ ኦሃዮ:</strong>
        ብዝሒ ገዛውቲ፣ ኣቕርቦትን መዓልታት ዕዳጋን ካብ ማእከላይ ኦሃዮ MLS ዝተወስዱ መወከሲ ሓበሬታ እዮም።
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
