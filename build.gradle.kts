import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val slf4jVersion: String by project
val smartThingsUserName: String by project
val smartThingsPassword: String by project

val stLibsReleaseVirtual = "https://smartthings.jfrog.io/smartthings/libs-release"
val stLibsSnapshotVirtual = "https://smartthings.jfrog.io/smartthings/libs-snapshot"
val gradlePluginsUrl = "https://plugins.gradle.org/m2/"

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.10"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.suppressWarnings = true
compileKotlin.kotlinOptions.jvmTarget = "11"
compileKotlin.kotlinOptions.apiVersion = "1.5"
compileKotlin.kotlinOptions.languageVersion = "1.5"
compileKotlin.kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
compileKotlin.sourceCompatibility = "11"
compileKotlin.targetCompatibility = "11"

repositories {
    mavenCentral()
    maven {
        credentials {
            username = smartThingsUserName
            password = smartThingsPassword
        }
        url = uri(stLibsReleaseVirtual)
    }
    maven {
        credentials {
            username = smartThingsUserName
            password = smartThingsPassword
        }
        url = uri(stLibsSnapshotVirtual)
    }
    maven { url = uri(gradlePluginsUrl) }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")

    implementation("smartthings:st-slf4j:${slf4jVersion}")
    implementation("org.slf4j:slf4j-api:1.7.31")
    implementation("io.dropwizard.metrics:metrics-core:4.2.2")
    implementation("io.ratpack:ratpack-core:1.8.2")
    implementation("io.ratpack:ratpack-test:1.8.2")
    implementation("smartthings.siam.client:siam-client-ratpack:1.0.3") {
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "io.ratpack", module = "ratpack-dropwizard-metrics")
    }

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application.
    mainClass.set("benjishults.context.AppKt")
}
