plugins {
    kotlin("jvm") version libs.versions.kotlin.asProvider().get() apply false
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint()
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
    }
}