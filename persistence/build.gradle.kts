plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.sqlite:sqlite-bundled:2.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    ksp("androidx.room:room-compiler:2.7.0")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    outputs.upToDateWhen { false }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
