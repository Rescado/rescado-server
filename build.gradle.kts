import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val revision: String? = System.getenv("GITHUB_RUN_NUMBER")

group = "org.rescado"
version = "1.0.0-${if (revision.isNullOrBlank()) "SNAPSHOT" else "r$revision"}"
java.sourceCompatibility = JavaVersion.VERSION_11

print("Current build version: $version")

// Sources
repositories {
    mavenCentral()
}

// Plugins
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.1.4"
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("plugin.jpa") version "1.5.31"
}

// Dependencies
dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.5.5")
    implementation("org.springframework.boot:spring-boot-starter-security:2.5.5")
    // Hibernate
    implementation("org.hibernate:hibernate-core:5.5.7.Final")
    implementation("org.hibernate.validator:hibernate-validator:7.0.1.Final")
    implementation("org.hibernate.validator:hibernate-validator-annotation-processor:7.0.1.Final")
    runtimeOnly("org.postgresql:postgresql:42.2.24.jre7")
    // Liquibase
    implementation("org.liquibase:liquibase-core:4.5.0")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    // YAUAA
    implementation("nl.basjes.parse.useragent:yauaa:6.0")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.5") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test:5.5.1")
}

// Spring Boot config
springBoot {
    buildInfo() // make available at runtime
}

// Google Jib config
jib {
    from {
        image = "openjdk:11"
    }
    to {
        image = "rescado/rescado-server"
        tags = setOf("$version", "latest")
    }
    container {
        jvmFlags = listOf("-Dspring.profiles.active=prod")
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8282")
    }
}

// Simple task that will copy project Git hooks to the .git directory
tasks.register<Copy>("installGitHooks") {
    from(".github/hooks")
    into(".git/hooks")
}

// Alternative command to run app with "prod" as active Spring profile
tasks.register("bootRunProd") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the prod profile"
    // Running this via IntelliJ is bugged:
    // - You cannot debug this: breakpoints are ignored (maybe related https://youtrack.jetbrains.com/issue/IDEA-119494)
    // - Stopping process outputs process stopped but that's a lie: process just keeps running and you'll need to kill it
    // TODO find another way to define a "bootRunDev" task, so "bootRun" can be used for prod config.
    doFirst {
        tasks.bootRun.configure {
            systemProperty("spring.profiles.active", "prod")
        }
    }
    finalizedBy("bootRun")
}

// Kotlin compiler arguments and chaining installGitHooks task
tasks.withType<KotlinCompile> {
    kotlinOptions {
        apiVersion = "1.5" // Kotlin version
        languageVersion = "1.5" // Kotlin version
        jvmTarget = "11" // JVM version
        freeCompilerArgs = listOf("-Xjsr305=strict") // strict null-safety
    }
    finalizedBy("installGitHooks") // install most recent Git hooks
}

// Use JUnit for tests
tasks.withType<Test> {
    useJUnitPlatform()
}
