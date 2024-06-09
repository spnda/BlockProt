buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("maven-publish")
    // Since the original shadow is currently not being updated, we need to use this fork instead.
    // See https://plugins.gradle.org/plugin/io.github.goooler.shadow
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.kyori.blossom") version "1.3.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

val nbtApiVersion: String by project
val anvilGuiVersion: String by project
val townyVersion: String by project
val papiVersion: String by project
val worldGuardVersion: String by project

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "CodeMC"
        content {
            includeGroup("de.tr7zw")
            includeGroup("net.wesjd")
        }
    }
}

dependencies {
    implementation(project(":common"))

    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")

    // bStats
    api("org.bstats:bstats-bukkit:3.0.2")

    // Dependencies
    implementation("de.tr7zw:item-nbt-api:$nbtApiVersion")
    implementation("net.wesjd:anvilgui:$anvilGuiVersion") // Allows us to use anvils as inventories without using NMS.

    // Integrations
    implementation("com.github.TownyAdvanced:Towny:$townyVersion")
    implementation("me.clip:placeholderapi:$papiVersion")
    implementation("com.sk89q.worldguard:worldguard-bukkit:$worldGuardVersion")
    implementation("com.github.angeschossen:LandsAPI:6.28.11")
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

tasks.javadoc {
    options {
        source = "17"
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PACKAGE
        (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    this.isFailOnError = false
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.bukkit.shaded.nbtapi")
    relocate("net.wesjd.anvilgui", "de.sean.blockprot.bukkit.shaded.anvilgui")
    relocate("org.bstats", "de.sean.blockprot.bukkit.metrics")
    // minimize()

    dependencies {
        this.include(project(":common"))
        this.include(dependency("org.jetbrains:annotations"))
        this.include(dependency("de.tr7zw:item-nbt-api"))
        this.include(dependency("net.wesjd:anvilgui"))
        this.include(dependency("org.bstats:bstats-base"))
        this.include(dependency("org.bstats:bstats-bukkit"))
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

tasks.runServer {
    minecraftVersion("1.20.6")
    downloadPlugins {
        url("https://download.luckperms.net/1544/bukkit/loader/LuckPerms-Bukkit-5.4.131.jar")
    }
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
