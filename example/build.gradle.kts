plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":mutekt-core"))
    ksp(project(":mutekt-codegen"))

    implementation(libs.kotlinx.coroutines.core)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}