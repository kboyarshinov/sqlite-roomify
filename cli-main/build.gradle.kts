plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass = "dev.kboyarshinov.sqlroomify.cli.main.MainKt"
}

kotlin {
    jvmToolchain(19)
}

dependencies {
    implementation(libs.kotlin.common)
    implementation(libs.kotlin.stdlib)
    implementation(libs.clikt)
    implementation(libs.okio)

    implementation(projects.translationLib)

    testImplementation(platform(libs.testing.junit.bom))
    testImplementation(libs.testing.junit.jupiter)
    testRuntimeOnly(libs.testing.junit.launcher)

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