
import org.ec4j.core.Resource
import org.ec4j.core.parser.ErrorHandler
import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GitHub
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

buildscript {
    dependencies {
        classpath("org.kohsuke:github-api:1.132")
        classpath("org.ec4j.core:ec4j-core:0.3.0")
    }

    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("org.gradle.java-library")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("org.ajoberstar.grgit") version "4.1.0"
    id("com.diffplug.spotless") version "5.14.1"
}

val env: MutableMap<String, String> = System.getenv()
val pluginVersion: String by project
val townyVersion: String by project

fun gitBranchName(): String {
    val env = System.getenv()
    if (env["GITHUB_REF"] != null) {
        val branch = env["GITHUB_REF"]!!
        return branch.substring(branch.lastIndexOf("/") + 1)
    }

    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

fun readEditorConfigRules(): Map<String, String> {
    val editorConfigFile = Paths.get(System.getProperty("user.dir") + "/.editorconfig")
    val parser = org.ec4j.core.parser.EditorConfigParser.builder().build()
    val handler = org.ec4j.core.parser.EditorConfigModelHandler(org.ec4j.core.PropertyTypeRegistry.default_(), org.ec4j.core.model.Version.CURRENT)
    parser.parse(Resource.Resources.ofPath(editorConfigFile, StandardCharsets.UTF_8), handler, ErrorHandler.THROW_SYNTAX_ERRORS_IGNORE_OTHERS)
    val editorConfig = handler.editorConfig

    return editorConfig.sections.filter {
        it.glob.source.equals("*.{kt,kts}") || it.glob.source.equals("*")
    }.map { it ->
        it.properties.mapValues {
            it.value.sourceValue
        }
    }.reduce { sum, element -> sum + element }
}

group = "de.sean"
version = pluginVersion
base.archivesName.set("${project.name}-$version-${gitBranchName()}")

repositories {
    mavenLocal()
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
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    mavenCentral()
}

dependencies {
    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.12.0")

    // bStats
    api("org.bstats:bstats-bukkit:2.2.1")

    // Dependencies
    implementation("org.jetbrains:annotations:21.0.1")
    implementation("de.tr7zw:item-nbt-api:2.8.0")
    implementation("net.wesjd:anvilgui:1.5.2-SNAPSHOT") // Allows us to use anvils as inventories without using NMS.
    implementation("com.github.TownyAdvanced:Towny:$townyVersion")
}

spotless {
    encoding("UTF-8")

    val editorConfig = readEditorConfigRules()
    println(editorConfig)

    java {
        importOrder()
        removeUnusedImports()

        licenseHeaderFile(project.file("HEADER.txt")).yearSeparator(", ")
    }

    kotlinGradle {
        // We can start using ktlint through spotless as soon as
        // https://github.com/diffplug/spotless/issues/142 is fixed.
        // For now we use ec4j to read the editorconfig manually and
        // pass the read values to ktlint.
        ktlint("0.41.0").userData(editorConfig)
    }
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

tasks.jar {
    archiveClassifier.set(null as String?)
    archiveFileName.set("${base.archivesName.get()}.jar")
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
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
    archiveFileName.set("${base.archivesName.get()}-${archiveClassifier.get()}.jar")
}

tasks.shadowJar {
    relocate("de.tr7zw.changeme.nbtapi", "de.sean.blockprot.shaded.nbtapi")
    relocate("net.wesjd.anvilgui", "de.sean.blockprot.shaded.anvilgui")
    relocate("org.bstats", "de.sean.blockprot.metrics")
    minimize()

    dependencies {
        this.exclude(dependency("com.github.TownyAdvanced:Towny:$townyVersion"))
    }

    archiveClassifier.set("all")
    archiveFileName.set("${base.archivesName.get()}-${archiveClassifier.get()}.jar")
}

tasks.build {
    dependsOn(tasks.javadoc)
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
        releaseBuilder.body(env["CHANGELOG"] ?: "No changelog.")
        releaseBuilder.commitish(gitBranchName())

        val ghRelease = releaseBuilder.create()
        ghRelease.name = "${project.name} $version" // We set the proper name here, as "releaseBuilder.name" is also used for the tag name.
        ghRelease.uploadAsset(file("${project.buildDir}/libs/${base.archivesName.get()}-all.jar"), "application/java-archive")
    }
}
