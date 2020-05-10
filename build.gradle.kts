import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val kotlinVersion = "1.3.50"
val http4kVersion = "3.196.0"
val log4jVersion = "2.12.1"
val jacksonVersion = "2.10.0"
val jaxbVersion = "2.3.0"
val kotestVersion = "4.0.5"

plugins {
    kotlin("jvm") version "1.3.50"
    jacoco
    id("com.github.ben-manes.versions") version "0.26.0"
    id("com.adarshr.test-logger") version "2.0.0"
    application
}

application {
    mainClassName = "conduit.MainKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = URI("http://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation("javax.xml.bind:jaxb-api:$jaxbVersion")
    implementation("com.sun.xml.bind:jaxb-core:$jaxbVersion")
    implementation("com.sun.xml.bind:jaxb-impl:$jaxbVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    implementation("org.http4k:http4k-client-apache:$http4kVersion")
    implementation("org.jetbrains.exposed:exposed:0.13.6")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.h2database:h2:1.4.198")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jacksonVersion")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion") // for kotest core jvm assertions
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"
