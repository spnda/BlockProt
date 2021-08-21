pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
    }
}

rootProject.name = "blockprot"

include("common")

val subprojects = listOf("spigot", "fabric")
subprojects.forEach {
    include(it)
    project(":$it").name = "${rootProject.name}-$it"
}
