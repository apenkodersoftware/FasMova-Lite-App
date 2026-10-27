# FasMova Lite

Android app for a small retail shop: simple name/price tiles, piece/weight/volume sales, customer Deni and partial repayments, and readable business reporting in English and Kiswahili.

## Build and review delivery

The 0.1.0 debug APK is built. Local codeQualityCheck, testDebugUnitTest, lint, assembleDebug and assembleDebugAndroidTest pass: **143 tests across 36 suites, zero failures/errors/skips; lint has 0 errors and 90 warnings**. Implementation commit 3fc90b7 also passed [GitHub CI](https://github.com/apenkodersoftware/FasMova-Lite-App/actions/runs/40443793330).

The APK, source archive, final device-acceptance report, screenshots and checksums are delivered through this repository's [review Releases](https://github.com/apenkodersoftware/FasMova-Lite-App/releases). Read the matching release's verification report for the exact device results and acceptance status. This is a debug-signed owner-review build; release signing and physical-phone acceptance are separate milestones.

## Shop workflow

- Sell / Uza: two-column name/price tiles, favourites, search and simple categories. Piece goods use whole counts; loose goods use kg/g or litre/ml, quarter/half presets, custom quantities such as 1.75 kg and money-based quotes.
- Stock / Bidhaa: create products, choose units and rates, set optional quarter/half prices, replenish the same stock balance and pin favourites.
- Today / Leo: saved sales, recorded expenses, a qualified estimate, payments received, new credit, debt collections and outstanding Deni.
- More / Zaidi: daily/weekly/monthly/yearly business reports, named customers, partial repayments and dated debt history, expenses, encrypted backup/restore, shop settings and English/Kiswahili.

For a KES 30 rice budget at KES 180/kg, the app quotes 166 g for KES 29.88. It shows the whole-gram rounding and charges the actual quote. Completed lines preserve their quantity, unit, pricing basis and price.

A KES 280 sale can record Cash 100 plus Deni 180 for a named customer. A later repayment of 50 reduces debt to 130 without becoming another sale. Customer phone numbers are optional.

New installations create their own shop and owner PIN; training products are opt-in. The automated review fixture is not packaged as live business data in the APK.

## Scope and limits

One shop on one device, with locally saved Room records. Mobile-money payments and references are manually recorded; no provider confirmation or cloud synchronization is configured. The report's estimate is sales less recorded expenses, excludes historical cost of goods sold and is not verified retail profit. Net receipts are not an exact cash or mobile-wallet balance. Returns/cancellations, supplier receiving, a stock-movement ledger and opening-debt import are outside this first review.

Application ID: `com.apenkoder.fasmovalite`; version 0.1.0 / code 1; minimum API 24. Internal Kotlin names are retained from the foundation. Source was adapted in a separate checkout from MaliMali commit `8e32bb363e3dbeb1a880e54d15fe328ee32d439d`; the original apps are untouched.

## Build and tests

Use JDK 21 and the project Android SDK. Put the local SDK path in ignored `local.properties`.

`./gradlew codeQualityCheck testDebugUnitTest lint assembleDebug assembleDebugAndroidTest`

Device acceptance requires a **fresh disposable Lite installation**. Run AppContextInstrumentedTest and LiteShopAcceptanceTest first, then LiteLargeTextAcceptanceTest at 320 dpi / 1.5 font scale and restore the device profile afterwards. Never run fixture tests against a configured real shop: the normal suite deliberately refuses existing business data.

The inherited Firebase distribution workflow is manual-only; pushes do not deploy. Private signing keys, credentials, SDK paths and local caches are excluded. The included debug keystore is the public Android development key.

See [quantity decisions](docs/lite/QUANTITIES.md) and the historical [source assessment](docs/lite/SOURCE-ASSESSMENT.md).
