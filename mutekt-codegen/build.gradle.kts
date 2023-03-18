plugins {
    kotlin("multiplatform")
    id(libs.plugins.mavenPublish.get().pluginId)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        testRuns.getByName("test").executionTask.configure {
            // Temporarily disabled because 'https://github.com/tschuchortdev/kotlin-compile-testing'
            // is not yet supporting latest KSP and Kotlin version
            useJUnitPlatform()
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mutekt-core"))

                implementation(libs.ksp.symbol.processing.api)
                implementation(libs.kotlinPoet.ksp)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotlin.compile.testing)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.kotlin.jvmTarget.get()
}
