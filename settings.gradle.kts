pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.perfectdreams.net/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "cinnamon-parent"

include(":common")

// ===[ WEB ]===
include(":web:web-api-data")
include(":web:web-api")
include(":web:dashboard")

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
include(":discord:web-server")
include(":discord:gateway")