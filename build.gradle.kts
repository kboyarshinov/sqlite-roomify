plugins {
    // lists plugins available to submodules
    alias(libs.plugins.kotlin.jvm).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.kotlin.kapt).apply(false)
    alias(libs.plugins.android.app).apply(false)
    alias(libs.plugins.android.lib).apply(false)
}