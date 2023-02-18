plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider().get() apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.kotlin.kover)
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlin.kover.get().pluginId)
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
            ktlint()
            licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint()
        }
    }
}

koverMerged {
    enable()
    filters {
        projects {
            excludes += listOf("example")
        }
    }
    xmlReport {
        onCheck.set(true)
        reportFile.set(layout.buildDirectory.file("coverageReport/coverage.xml"))
    }

    htmlReport {
        onCheck.set(true)
        reportDir.set(layout.buildDirectory.dir("coverageReport/html"))
    }
}