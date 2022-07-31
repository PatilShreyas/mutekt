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

//     val mutektVersion = "1.0.0-alpha02"
//     implementation("dev.shreyaspatil.mutekt:mutekt-core:$mutektVersion")
//     ksp("dev.shreyaspatil.mutekt:mutekt-codegen:$mutektVersion")

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