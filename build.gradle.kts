import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.google.com/") }
}

group = "com.viettel"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<BootJar>("bootJar") {
    mainClassName = "com.viettel.vtag.Application"
}

tasks.getByName<BootRun>("bootRun") {
    main = "com.viettel.vtag.Application"
}

val versions = mapOf("boot" to "2.3.4.RELEASE")

fun spring(project: String, module: String = ""): String {
    return "org.springframework.$project:spring-$project$module:${versions[project]}"
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.18")
    annotationProcessor("org.projectlombok:lombok:1.18.18")

    compileOnly(spring("boot", "-configuration-processor"))
    annotationProcessor(spring("boot", "-configuration-processor"))

    implementation(spring("boot", "-starter"))
    implementation(spring("boot", "-starter-jdbc"))
    implementation(spring("boot", "-starter-webflux"))
    implementation(spring("boot", "-starter-security"))
    implementation(spring("boot", "-starter-actuator"))

    implementation("org.postgresql:postgresql:42.2.19")
    implementation("mysql:mysql-connector-java:5.1.49")

    implementation("com.google.auth:google-auth-library-oauth2-http:0.25.2")
    implementation("com.google.firebase:firebase-admin:7.1.1")

    testImplementation("com.github.javafaker:javafaker:1.0.2")

    testCompileOnly("org.projectlombok:lombok:1.18.18")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.18")
    testImplementation(spring("boot", "-starter-test"))
}

tasks.test {
    useJUnitPlatform()
}
