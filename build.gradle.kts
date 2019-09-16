import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    'build-scan'
    `maven-publish`
    kotlin("jvm") version "1.3.50"
}

group = "com.github"
version = "1.0"

repositories {
    mavenCentral()
}

buildScan{
    termsOfServiceAgree = "yes"
    publishAlways()
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(dokkaJar)
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repository")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("net.sf.opencsv:opencsv:2.3")
    testImplementation ("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}