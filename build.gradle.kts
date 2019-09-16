import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
}

group = "com.github"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.codehaus.jackson:jackson-mapper-asl:version:1.9.13")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.11")
    implementation("net.sf.opencsv:opencsv:2.3")
    testCompile ("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}