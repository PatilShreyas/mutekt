plugins {
    kotlin("multiplatform")
    id(libs.plugins.mavenPublish.get().pluginId)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
    }
    js(IR) {
        browser {
            testTask {
                isEnabled = false
            }
        }
        nodejs {
            testTask {
                isEnabled = false
            }
        }
    }
    ios()
    mingwX64()

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.testing)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.kotlin.jvmTarget.get()
}
