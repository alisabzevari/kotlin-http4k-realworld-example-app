import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val kotlinVersion = "1.3.10"
val http4kVersion = "3.103.2"
val log4jVersion = "2.10.0"
val jacksonVersion = "2.9.6"

plugins {
    kotlin("jvm") version "1.3.10"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = URI("http://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")
    implementation("org.http4k:http4k-client-apache:$http4kVersion")
    implementation("org.jetbrains.exposed:exposed:0.11.2")
    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("com.h2database:h2:1.4.197")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda:$jacksonVersion")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")
    testImplementation("io.mockk:mockk:1.8.13.kotlin13")
}

tasks.test {
    useJUnitPlatform()
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = "1.8"
