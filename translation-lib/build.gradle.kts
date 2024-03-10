plugins {
    alias(libs.plugins.kotlin.jvm)
    // OPTIONAL: remove if kapt is unused
    alias(libs.plugins.kotlin.kapt)
}

kotlin {
    // libraries should have explicit API by default
    explicitApi()
}

dependencies {
    implementation(libs.kotlin.common)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.testing.kotlin.test.common)
    testImplementation(libs.testing.kotlin.test.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}