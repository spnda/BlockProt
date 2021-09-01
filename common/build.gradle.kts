plugins {
    id("org.gradle.java-library")
}

dependencies {
    api("org.jetbrains:annotations:22.0.0")
}

tasks.compileJava {
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}
