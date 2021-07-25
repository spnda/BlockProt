buildscript {
    dependencies {
        classpath("org.ec4j.core:ec4j-core:0.3.0")
    }

    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.ajoberstar.grgit")
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
    implementation("org.jetbrains:annotations:21.0.1")
    implementation("de.tr7zw:item-nbt-api:2.8.0")
    implementation("net.wesjd:anvilgui:1.5.1-SNAPSHOT") // Allows us to use anvils as inventories without using NMS.
    implementation("com.github.TownyAdvanced:Towny:$townyVersion")
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

tasks.create<Jar>("javadocJar") {
    dependsOn(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.bukkit.shaded.nbtapi")
    relocate("net.wesjd.anvilgui", "de.sean.blockprot.bukkit.shaded.anvilgui")
    relocate("org.bstats", "de.sean.blockprot.bukkit.metrics")
    minimize()

    dependencies {
        this.exclude(dependency("com.github.TownyAdvanced:Towny:$townyVersion"))
    }

    archiveClassifier.set(
        if (ext["gitBranchName"] == "master") "all"
        else "${ext["gitBranchName"]}-all")
    // archiveFileName.set("${base.archivesName.get()}-${archiveClassifier.get()}.jar")
}

tasks.build {
    dependsOn(tasks["javadocJar"])
    dependsOn(tasks.shadowJar)
}
