pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "loritta-parent"

// ===[ PUDDING ]===
include(":pudding:client")

// ===[ COMMON ]===
include(":common")
include(":loritta-serializable-commons")
include(":loritta-placeholders")

// ===[ LORITTA ]===
include(":loritta-bot-discord")

// ===[ SPICY MORENITTA ]===
include(":web:spicy-morenitta")

// ===[ LORITTA'S WEBSITE ]===
include(":loritta-website:web-common")
include(":loritta-website:loritta-website-backend")
include(":loritta-website:loritta-website-frontend")

// ===[ BLISS ]===
include(":loritta-dashboard:bliss-common")
include(":loritta-dashboard:bliss")
include(":loritta-dashboard:bliss-standalone")

// ===[ DASHBOARD ]===
include(":loritta-dashboard-proxy")
include(":loritta-dashboard:message-renderer")
include(":loritta-dashboard:loritta-shimeji-common")
include(":loritta-dashboard:dashboard-common")
include(":loritta-dashboard:frontend")

// ===[ MISC ]===
include(":loritta-helper")
include(":switch-twitch")
include(":loricoolcards-generator")
include(":discord-chat-markdown-parser")
include(":discord-chat-markdown-parser-samples")
include(":discord-chat-message-renderer-entities")
include(":discord-chat-message-renderer-server")
include(":lori-api-proxy")
include(":lori-public-http-api-common")
include(":yokye")