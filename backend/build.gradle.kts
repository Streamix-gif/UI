plugins {
    id("com.android.library")
}

group = "streamix"
version = "1.0.0"

android {
    namespace = "streamix.backend"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        getByName("main") {
            java.srcDir("sources/oce/BaseProvider/src/main/kotlin")
            java.srcDir("sources/oce/ProviderAnichin/src/main/kotlin")
            java.srcDir("sources/oce/ProviderAnimasu/src/main/kotlin")
            java.srcDir("sources/oce/ProviderAnimexin/src/main/kotlin")
            java.srcDir("sources/oce/ProviderSamehadaku/src/main/kotlin")
            java.srcDir("sources/animex/NimegamiProvider/src/main/kotlin")
            java.srcDir("sources/animex/WinbuProvider/src/main/kotlin")
            java.srcDir("sources/hatsune/AlqanimeProvider/main/kotlin")
            java.srcDir("sources/hatsune/AnimeSailProvider/main/kotlin")
            java.srcDir("sources/hatsune/AnoboyProvider/main/kotlin")
            java.srcDir("sources/hatsune/KuramanimeProvider/main/kotlin")
            java.srcDir("sources/hatsune/KuronimeProvider/main/kotlin")
            java.srcDir("sources/hatsune/NontonAnimeIDProvider/main/kotlin")
            java.srcDir("sources/hatsune/OtakudesuProvider/main/kotlin")
            resources.srcDir("sources/oce/BaseProvider/src/main/kotlin/com/baseprovider/config")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

val cloudstream3Jar: String = System.getenv("CLOUDSTREAM3_JAR")?.takeIf { it.isNotBlank() }
    ?: error("CLOUDSTREAM3_JAR must point to the CloudStream runtime classes.jar")

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.Blatzar:NiceHttp:0.4.18")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.mozilla:rhino:1.8.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("app.cash.quickjs:quickjs-android:0.9.2")
    implementation("dev.whyoleg.cryptography:cryptography-core:0.6.0")
    implementation("dev.whyoleg.cryptography:cryptography-provider-optimal:0.6.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.conscrypt:conscrypt-android:2.5.2")
    implementation(files(cloudstream3Jar))
    testImplementation(files(cloudstream3Jar))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("org.robolectric:robolectric:4.14.1")
}
