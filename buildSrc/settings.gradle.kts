dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

apply(file("../gradle/repositories.gradle.kts"))

rootProject.name = "sqlite-roomify"