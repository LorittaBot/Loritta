pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
    }
}

rootProject.name = "loritta-parent"

// ===[ PUDDING ]===
include(":pudding:client")

// ===[ COMMON ]===
include(":common")
include(":loritta-serializable-commons")
include(":loritta-placeholders")
include(":discord-oauth2")
include(":ssr-svg-icon-manager")

// ===[ LUNA ]===
include(":luna:bliss")
include(":luna:bliss-standalone")
include(":luna:toast-common")
include(":luna:toast-manager-frontend")
include(":luna:modal-common")
include(":luna:modal-manager-frontend")

// ===[ LORITTA ]===
include(":loritta-bot-discord")

// ===[ SPICY MORENITTA ]===
include(":web:spicy-morenitta")

// ===[ LORITTA'S WEBSITE ]===
include(":loritta-website:web-common")
include(":loritta-website:loritta-website-backend")
include(":loritta-website:loritta-website-frontend")

// ===[ DASHBOARD ]===
include(":loritta-dashboard-proxy")
include(":loritta-dashboard:message-renderer")
include(":loritta-dashboard:loritta-shimeji-common")
include(":loritta-dashboard:dashboard-common")
include(":loritta-dashboard:frontend")

// ===[ DORA ]===
include(":dora:backend")
include(":dora:frontend")

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