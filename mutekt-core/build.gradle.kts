plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id(libs.plugins.mavenPublish.get().pluginId)
}

repositories {
    mavenCentral()
    google()
}

kotlin {
    android {
        publishLibraryVariants("release", "debug")
    }
    jvm {
        testRuns.getByName("test").executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(BOTH) {
        browser()
        nodejs()
    }
    ios()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }

        val badSourceSets = listOf(
            "androidAndroidTestRelease",
            "androidTestFixtures",
            "androidTestFixturesDebug",
            "androidTestFixturesRelease"
        )
        removeIf { ss -> ss.name in badSourceSets }
    }
}

android {
    namespace = findProperty("GROUP").toString()
    compileSdk = 33
    @Suppress("UnstableApiUsage")
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.kotlin.jvmTarget.get()
}
