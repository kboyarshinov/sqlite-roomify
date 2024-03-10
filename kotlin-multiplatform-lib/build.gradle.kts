plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    // libraries should have explicit API by default
    explicitApi()

    // targets
    jvm {
        // configure JVM tests to run on JUnit
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // source sets & their configuration
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.testing.kotlin.test.common)
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.testing.kotlin.test.junit)
            }
        }
    }
}