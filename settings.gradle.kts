pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        gradlePluginPortal()
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
include(":discord:web-server")
include(":discord:gateway")

// ===[ MICROSERVICES ]===
include(":microservices:broker-tickers-updater")
include(":microservices:stats-collector")
include(":microservices:direct-message-processor")
include(":microservices:daily-tax")
include(":microservices:correios-package-tracker")

// ===[ WEB ]===
include(":web:showtime:web-common")
include(":web:showtime:backend")
include(":web:showtime:frontend")
