rootProject.name = "blockprot"

include("common")

val subprojects = listOf("spigot")
subprojects.forEach {
    include(it)
    project(":$it").name = "${rootProject.name}-$it"
}
