# Economic Watch — Columbus, Ohio

A Java console dashboard that tracks economic and housing conditions for
Columbus, Ohio, calculates a 0–100 economic risk indicator, compares current
conditions against four historical recessions, and gives a "buying outlook"
signal for home purchases — all framed as **indicators, not guarantees**.

## How to run

```bash
javac *.java
java -Dfile.encoding=UTF-8 EconomicWatchApp
```

(The `-Dfile.encoding=UTF-8` flag ensures the emoji and box-drawing
characters in the dashboard render correctly in your terminal.)

On first run, it seeds one sample 2026 snapshot so you have something to
look at immediately — replace it with real figures via option 2.

## Important limitation — no live data feed

This app does **not** pull live data from BLS, FRED, Zillow, or any other
source. All figures are entered manually (or start from the seeded sample).
The architecture is built so real API calls can be wired in later:

- **Jobs & inflation**: BLS API (free) — https://www.bls.gov/developers/
- **GDP, interest rates, consumer debt**: FRED API (free) — https://fred.stlouisfed.org/docs/api/fred/
- **Home prices, inventory**: Zillow Research data or a paid real estate API
- **Mortgage rates**: FRED — Freddie Mac PMMS series — https://fred.stlouisfed.org/series/MORTGAGE30US

To integrate any of these, replace the manual prompts in
`EconomicWatchApp.enterNewSnapshot()` with an HTTP call that builds an
`EconomicSnapshot` automatically.

## Features
- Console dashboard: jobs, housing, economy, risk score, recession
  comparison, buying outlook, and a 5-year illustrative outlook
- 0–100 economic risk score with category (Normal / Elevated / Severe /
  Depression-level), computed from unemployment, inflation, interest rates,
  consumer debt, credit stress, foreclosures, and GDP growth
- Recession comparison against four historical benchmarks (1990–91, 2001,
  2007–09, 2020) — finds the closest historical match
- Buying outlook indicator based on mortgage rates, inventory, home prices,
  and foreclosure rates
- Historical data store: each year is saved permanently; re-entering data
  for the current year updates it without erasing prior years
- Alert stub for sound/email/SMS notifications, triggered automatically
  whenever the risk category changes (currently prints what it would send —
  see class docs for how to wire in real notifications)

## Class structure
- `EconomicSnapshot` — one year's full set of economic/housing data (builder pattern)
- `RiskScoreCalculator` — converts a snapshot into a 0–100 score + category
- `RecessionComparator` — historical benchmarks + closest-match logic
- `BuyingOutlookAdvisor` — housing buy/wait indicator
- `DataStore` — file-based historical persistence (never overwrites other years)
- `AlertService` — stub notification dispatcher (sound/email/SMS)
- `Dashboard` — renders the full console dashboard
- `EconomicWatchApp` — main class, console menu

## Roadmap / next steps
- Wire in real data feeds (BLS, FRED, Zillow) instead of manual entry
- Replace the alert stub with real email (JavaMail) and SMS (e.g. Twilio) integration
- Add a real forecasting model for the 2027–2030 outlook instead of a placeholder
- Add a JavaFX or web GUI
- Add JUnit tests for the risk score and recession comparison math
