import okhttp3.OkHttpClient
import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.extras.okhttp3.OkHttpConnector

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("org.kohsuke:github-api:1.132")
        classpath("com.squareup.okhttp3:okhttp:4.9.2")
    }
}

plugins {
    id("org.gradle.java-library")
    id("org.ajoberstar.grgit") version "4.1.0"
    id("org.cadixdev.licenser") version "0.6.1"
}

fun gitBranchName(): String {
    val env = System.getenv()
    if (env["GITHUB_REF"] != null) {
        val branch = env["GITHUB_REF"]!!
        return branch.substring(branch.lastIndexOf("/") + 1)
    }

    val branch = grgit.branch.current().name
    return branch.substring(branch.lastIndexOf("/") + 1)
}

val env: MutableMap<String, String> = System.getenv()
val blockProtVersion: String by project

allprojects {
    apply(plugin = "org.gradle.java-library")
    apply(plugin = "org.cadixdev.licenser")

    group = "de.sean.blockprot"
    version = blockProtVersion

    repositories {
        mavenLocal()
        maven("https://repo.codemc.org/repository/maven-public/") {
            name = "CodeMC"
        }
        maven("https://jitpack.io") {
            name = "JitPack"
        }
        mavenCentral()
    }

    tasks.compileJava {
        java.sourceCompatibility = JavaVersion.VERSION_1_8
        java.targetCompatibility = JavaVersion.VERSION_1_8
    }

    ext["gitBranchName"] = gitBranchName()

    tasks.jar {
        // The default configuration for the archivesName is
        // [baseName]-[appendix]-[version]-[classifier].[extension]
        archiveClassifier.set(
            if (ext["gitBranchName"] == "master" || ext["gitBranchName"] == "HEAD") null
            else (ext["gitBranchName"] as String))
    }

    afterEvaluate {
        // We use license instead of spotless now, as spotless'
        // java formatter had too many issues.
        license {
            header(rootProject.file("HEADER.txt"))
            include("**/*.java")
            properties {
                this["year"] = 2021
            }
        }
    }
}

tasks.register("github") {
    onlyIf {
        env["GITHUB_TOKEN"] != null
    }

    doLast {
        val github = GitHubBuilder
            .fromEnvironment()
            // We have to use OkHttpClient for reflection reasons in JDK 16
            // See https://github.com/hub4j/github-api/issues/1202
            .withConnector(OkHttpConnector(OkHttpClient()))
            .withOAuthToken(env["GITHUB_TOKEN"] as String)
            .build()
        val repository = github.getRepository(env["GITHUB_REPOSITORY"])

        val releaseBuilder = GHReleaseBuilder(repository, version as String)
        releaseBuilder.name(version as String)
        releaseBuilder.body(env["CHANGELOG"] ?: "No changelog.")
        releaseBuilder.commitish(gitBranchName())

        // Get the output JARs for each subproject.
        val files = mutableListOf<File?>()
        subprojects.filter { it.name != "common" }.forEach {
            val dir = "${it.buildDir}/libs/"
            files.add(file(dir).listFiles()?.last { file ->
                file.nameWithoutExtension.endsWith("all")
            })
        }

        val ghRelease = releaseBuilder.create()
        files.forEach {
            ghRelease.uploadAsset(it, "application/java-archive")
        }
        // We set the proper name here, as "releaseBuilder.name" is also used for the tag name.
        ghRelease.update().name("BlockProt $version").update()
    }
}
