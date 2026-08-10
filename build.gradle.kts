plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

allprojects {
    group = "com.finanzas"
    version = "0.1.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }
}
