buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("net.kyori.blossom") version "1.3.0"
}

val townyVersion: String by project

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
    }
}

dependencies {
    implementation(project(":common"))

    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.12.0")

    // bStats
    api("org.bstats:bstats-bukkit:2.2.1")

    // Dependencies
    implementation("de.tr7zw:item-nbt-api:2.8.0")
    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT") // Allows us to use anvils as inventories without using NMS.
    implementation("com.github.TownyAdvanced:Towny:$townyVersion")
}

blossom {
    // We use blossom to dynamically get the list of language files
    // and inject them into the Translator.java source file.
    replaceToken(
        "\$TRANSLATION_FILES",
        // Get the first sourceSet and iterate over all resource files.
        fileTree(sourceSets["main"].resources.srcDirs.first()).files.filter {
            // Use a regex string to only include translations_xx.yml files.
            it.path.contains("translations_[a-z].+?.yml".toRegex())
        }.map {
            it.name
        }.toString(),
        "src/main/java/de/sean/blockprot/bukkit/Translator.java"
    )
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.compileJava {
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.javadoc {
    options {
        source = "8"
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PACKAGE
    }

    this.isFailOnError = false
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.bukkit.shaded.nbtapi")
    relocate("net.wesjd.anvilgui", "de.sean.blockprot.bukkit.shaded.anvilgui")
    relocate("org.bstats", "de.sean.blockprot.bukkit.metrics")
    // minimize()

    dependencies {
        this.exclude(dependency("com.github.TownyAdvanced:Towny:$townyVersion"))
    }

    archiveClassifier.set(
        if (ext["gitBranchName"] == "master" || ext["gitBranchName"] == "HEAD") "all"
        else "${ext["gitBranchName"]}-all")
    // archiveFileName.set("${base.archivesName.get()}-${archiveClassifier.get()}.jar")
}

tasks.build {
    dependsOn(tasks["javadocJar"])
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String

            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}
