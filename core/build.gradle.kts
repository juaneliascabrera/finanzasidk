import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }

    outputs.upToDateWhen { false }
}

kotlin {
    jvmToolchain(21)
}
