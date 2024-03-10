// Declare repositories for plugins
pluginManagement {
    repositories {
        // Allow Gradle and Jetbrains plugins from Gradle plugin portal
        exclusiveContent {
            forRepository { gradlePluginPortal() }

            filter {
                includeGroupByRegex("com.gradle.*")
                includeGroupByRegex("org.gradle.*")
                includeGroupByRegex("org.jetbrains.*")
            }
        }

        // Maven central for the rest of plugins
        mavenCentral()
    }
}
