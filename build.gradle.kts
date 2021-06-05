import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GitHub

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:1.130")
    }

    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("org.ajoberstar.grgit") version "4.1.0"
    id("com.diffplug.spotless") version "5.8.2"
}

val env: MutableMap<String, String> = System.getenv()

fun gitBranchName(): String {
    val env = System.getenv()
    if (env["GITHUB_REF"] != null) {
        val branch = env["GITHUB_REF"]!!
        return branch.substring(branch.lastIndexOf("/") + 1)
    }

    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

group = "de.sean"
version = "0.2.2"
base.archivesBaseName = "${project.name}-$version-${gitBranchName()}"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "Spigot"
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "CodeMC"
        content {
            includeGroup("de.tr7zw")
            includeGroup("org.bstats")
            includeGroup("net.wesjd")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("de.tr7zw:item-nbt-api:2.7.1") // item-nbt-api

    // Use anvils as inventories. They're stupid and require NMS, making renaming much easier.
    implementation("net.wesjd:anvilgui:1.5.0-SNAPSHOT")

    api("org.bstats:bstats-bukkit:2.2.1")

    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT") // Spigot
}

spotless {
    encoding("UTF-8")

    java {
        licenseHeaderFile(project.file("HEADER.txt")).yearSeparator(", ")
    }

    kotlin {
        // We can start using ktlint through spotless as soon as
        // https://github.com/diffplug/spotless/issues/142 is fixed.
        // ktlint()

        licenseHeaderFile(project.file("HEADER.txt")).yearSeparator(", ")
    }
}

tasks.processResources {
    expand("version" to project.version)
}

tasks.compileJava {
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.jar {
    archiveFileName.set("${base.archivesBaseName}.jar")
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.shaded.nbtapi")
    relocate("net.wesjd.anvilgui", "de.sean.blockprot.shaded.anvilgui")
    relocate("org.bstats", "de.sean.blockprot.metrics")
    minimize()

    val classifier: String? = null
    archiveClassifier.set(classifier)

    archiveFileName.set("${base.archivesBaseName}-all.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

/**
 * This task is currently unused, as CurseGradle does not support uploading
 * Bukkit plugins to CurseForge.
 */
/*tasks.register("curseforge") {
    if (env["CURSEFORGE_API_KEY"]) {
        apiKey = env["CURSEFORGE_API_KEY"]
    }

    project {
        id = "440797"
        changelog = "A changelog can be found at https://github.com/spnda/BlockProt/commits"
        releaseType = "release"
        addGameVersion "1.16"
        addGameVersion "1.15"
        addGameVersion "1.14"

        mainArtifact(file("${project.buildDir}/libs/${archivesBaseName}-all.jar")) {
            displayName = project.version
        }
    }
}*/

tasks.register("github") {
    onlyIf {
        env["GITHUB_TOKEN"] != null
    }

    doLast {
        val github = GitHub.connectUsingOAuth(env["GITHUB_TOKEN"] as String)
        val repository = github.getRepository(env["GITHUB_REPOSITORY"])

        val releaseBuilder = GHReleaseBuilder(repository, version as String)
        releaseBuilder.name(version as String)
        releaseBuilder.body("A changelog can be found at https://github.com/spnda/BlockProt/commits")
        releaseBuilder.commitish(gitBranchName())

        val ghRelease = releaseBuilder.create()
        ghRelease.name = "${project.name} $version" // We set the proper name here, as "releaseBuilder.name" is also used for the tag name.
        ghRelease.uploadAsset(file("${project.buildDir}/libs/${base.archivesBaseName}-all.jar"), "application/java-archive")
    }
}
