plugins {
    id("org.gradle.java-library")
}

dependencies {
    api("org.jetbrains:annotations:24.0.1")
}

tasks.compileJava {
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}
