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

//// ===[ PLATFORMS ]===
// ===[ CLI ]===
// include(":platforms:cli")

// ===[ DISCORD ]===
include(":platforms:discord:common")
// include(":platforms:discord:gateway")
include(":platforms:discord:commands")
include(":platforms:discord:interactions")

// ===[ TWITTER ]===
// include(":platforms:twitter")