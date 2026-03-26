import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
}

group = "org.jetbrains.research.refactorinsight"
version = "2026.1-1.0"

subprojects {
    tasks.withType<Test> {
        useJUnit()
        testLogging.exceptionFormat = FULL
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
