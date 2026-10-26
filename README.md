# FasMova Lite

Android app for a small retail shop: simple product tiles, piece/weight/volume sales, customer Deni and partial repayments, and readable business reporting in English and Kiswahili.

## Development status

The candidate implementation now includes measured piece/weight/volume stock, proportional and portion pricing, money-based quotes, partial-credit checkout, repayments and account history, favourites and business reporting. **Full verification is in progress. No green Lite APK is claimed at this checkpoint.**

The source snapshot came from MaliMali commit 8e32bb363e3dbeb1a880e54d15fe328ee32d439d. Its historical checks are not verification of this new app. Original applications are not modified by this checkout.

## First review target

- Product-name/price tiles, simple categories and favourites.
- Whole items, loose kg/g and litre/ml sales, presets/custom amounts and money-based quotes.
- Named-customer credit, part payments, repayment history and remaining debt.
- Mama Ntilie R7 style daily/weekly/monthly/yearly business summary, with receipts and credit separate.
- Saved records, encrypted backup/restore and restart verification.

Application ID: com.apenkoder.fasmovalite. Internal Kotlin namespace is retained from the foundation.

## Local checks

Use JDK 21 and the Android SDK specified by the project. Put the local SDK path in ignored local.properties.

Run gradlew codeQualityCheck, gradlew testDebugUnitTest, gradlew lint and gradlew assembleDebug. An APK is only green for review after the new shop workflow also passes emulator acceptance and installed-byte verification.

The inherited Firebase distribution workflow is manual-only. No deployment occurs when source is pushed. Only the public Android development keystore is included; private signing keys, credentials and local caches are excluded.

See docs/lite/SOURCE-ASSESSMENT.md and docs/lite/QUANTITIES.md for the agreed scope.
