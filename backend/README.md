# Streamix Backend

Standalone backend and stream-level E2E test harness.

This snapshot excludes the Saikou reference app and unrelated research archives. It keeps the OCE infrastructure/providers and selected Hatsune CloudStream-native providers used by the current host.

## Included
- OCE BaseProvider
- OCE Anichin, Animasu, Animexin, Samehadaku
- Hatsune Alqanime, AnimeSail, Anoboy, Kuramanime, Kuronime, NontonAnimeID, Otakudesu
- Streamix runtime, routing, identity, API, and E2E tests
- Gradle wrapper

CloudStream runtime is fetched by CI via CLOUDSTREAM3_JAR and is not vendored here.

## Test
Set CLOUDSTREAM3_JAR to a CloudStream classes.jar, then run:
./gradlew :testDebugUnitTest --tests streamix.StreamLevelE2ETest --tests streamix.RuntimeContractTest --stacktrace
