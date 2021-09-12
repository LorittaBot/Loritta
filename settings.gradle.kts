pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.perfectdreams.net/")
    }
}

rootProject.name = "cinnamon-parent"

include(":common")

// ===[ PUDDING ]===
// The reason this is not named "common" is because IDEA was getting a bit confusing due to duplicated names
// (errors related to class not found)
include(":pudding:data")
include(":pudding:client")

// ===[ DISCORD ]===
// The reason this is not named "common" is because IDEA was getting a bit confusing due to duplicated names
// (errors related to class not found)
include(":discord:discord-common")
include(":discord:commands")
// include(":discord:gateway")
include(":discord:web-server")