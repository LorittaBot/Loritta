pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.perfectdreams.net/")
    }
}

rootProject.name = "cinnamon-parent"

include(":common")

include(":services:memory")
include(":services:pudding")

// ===[ COMMANDS ]===
include(":commands")

// ===[ SERVICES ]===
include(":services:memory")
include(":services:pudding")

// ===[ DISCORD ]===
include(":discord:common")
// include(":discord:gateway")
include(":discord:commands")
include(":discord:interactions")