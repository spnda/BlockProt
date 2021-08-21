plugins {
    id("fabric-loom") version("0.9-SNAPSHOT")
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

repositories {
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://maven.nucleoid.xyz")
}

dependencies {
    implementation(project(":common"))

    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:1.17.1")
    mappings("net.fabricmc:yarn:1.17.1+build.30:v2")
    modImplementation("net.fabricmc:fabric-loader:0.11.6")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.37.1+1.17")

    // sgui library we use for making the server side menu GUIs.
    modImplementation("eu.pb4:sgui:1.0.0-rc4+1.17.1")
}

loom {
    accessWidenerPath.set(file("${project.projectDir}/src/main/resources/blockprot.accesswidener"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.compileJava {
    java.sourceCompatibility = JavaVersion.VERSION_16
    java.targetCompatibility = JavaVersion.VERSION_16
}
