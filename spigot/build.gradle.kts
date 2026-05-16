buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("maven-publish")
    // Updated to com.gradleup.shadow which supports Java 25+ (ASM updated)
    id("com.gradleup.shadow") version "8.3.8"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

val nbtApiVersion: String by project
val townyVersion: String by project
val papiVersion: String by project
val worldGuardVersion: String by project

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
    }
    maven("https://repo.papermc.io/repository/maven-snapshots/") {
        name = "PaperSnapshots"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "CodeMC"
        content {
            includeGroup("de.tr7zw")
        }
    }

}

dependencies {
    implementation(project(":common"))

    // Paper API — compilar contra el mínimo soportado (1.21) garantiza que el JAR
    // funciona en cualquier versión desde 1.21 hasta 26.x y futuros 27.x+.
    // Paper mantiene compatibilidad hacia atrás; APIs específicas de versiones
    // más nuevas se guardan detrás de VersionCompat/MinecraftVersion checks.
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.13.0")

    // bStats
    api("org.bstats:bstats-bukkit:3.0.2")

    // Dependencies
    implementation("de.tr7zw:item-nbt-api:$nbtApiVersion")

    implementation("org.enginehub:squirrelid:0.3.2")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("com.mysql:mysql-connector-j:9.7.0")

    // Integrations (soft-depend — provided at runtime by the server)
    compileOnly("com.github.TownyAdvanced:Towny:$townyVersion")
    compileOnly("me.clip:placeholderapi:$papiVersion")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:$worldGuardVersion")
    compileOnly("com.github.angeschossen:LandsAPI:6.28.11")
}

val targetJavaVersion: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.toInt()))
    }
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
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
            source = targetJavaVersion
        encoding = "UTF-8"
        memberLevel = JavadocMemberLevel.PACKAGE
        (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    this.isFailOnError = false
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.bukkit.shaded.nbtapi")
    relocate("org.bstats", "de.sean.blockprot.bukkit.metrics")
    relocate("org.enginehub.squirrelid", "de.sean.blockprot.bukkit.squirrelid")
    relocate("com.zaxxer.hikari", "de.sean.blockprot.bukkit.shaded.hikari")
    // minimize()

    dependencies {
        this.include(project(":common"))
        this.include(dependency("org.jetbrains:annotations"))
        this.include(dependency("de.tr7zw:item-nbt-api"))
        this.include(dependency("org.bstats:bstats-base"))
        this.include(dependency("org.bstats:bstats-bukkit"))
        this.include(dependency("org.enginehub:squirrelid"))
        this.include(dependency("com.zaxxer:HikariCP"))
        this.include(dependency("com.mysql:mysql-connector-j"))
        this.include(dependency("org.slf4j:slf4j-api"))
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
    downloadPlugins {
        url("https://download.luckperms.net/1561/bukkit/loader/LuckPerms-Bukkit-5.4.146.jar")
    }
    minecraftVersion("26.1.2")
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
