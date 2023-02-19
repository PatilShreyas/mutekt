plugins {
    kotlin("multiplatform")
    alias(libs.plugins.ksp)
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        testRuns.getByName("test").executionTask.configure {
            useJUnitPlatform()
        }
        withJava()
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(project(":mutekt-core"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

application {
    mainClass.set("dev.shreyaspatil.mutekt.example.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":mutekt-codegen"))
}
