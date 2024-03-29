plugins {
    kotlin("jvm") version "1.7.20"
    application
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

group = "gifts.givin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClassName = project.findProperty("mainClass").toString()
}

val exposedVersion: String by project
val junitJupiterVersion: String by project
dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.30")

    implementation("com.uchuhimo:konf:1.1.2")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.test {
    useJUnitPlatform()
}
