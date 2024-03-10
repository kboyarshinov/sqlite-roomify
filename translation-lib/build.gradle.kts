plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(19)
    explicitApi()
}

dependencies {
    implementation(libs.kotlin.common)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinpoet)
    implementation(libs.jsqlparser)

    testImplementation(platform(libs.testing.junit))
    testImplementation(libs.testing.kotlin.test.common)
    testImplementation(libs.testing.kotlin.test.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}