// Enables typesafe project accessors
// https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:type-safe-project-accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// Enable stable configuration cache & report issues
// https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:stable
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

// configure plugin repositories
apply(file("gradle/plugin-repositories.gradle.kts"))

// apply common repository configuration
apply(file("gradle/repositories.gradle.kts"))

rootProject.name = "gradle-kotlin-zero"

// modules to include
include(":kotlin-lib")
include(":kotlin-multiplatform-lib")
include(":kotlin-android-app")
include(":kotlin-android-lib")
