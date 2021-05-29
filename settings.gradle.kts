pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin2js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
        eachPlugin {
            if (requested.id.id == "kotlin-multiplatform") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

rootProject.name = "loritta-parent"

include(":common")
include(":common-legacy")
include(":commands")
include(":loritta-serializable-commons")
include(":platforms:discord:db-tables")
include(":platforms:discord:legacy")

// Plugins
include(":loritta-plugins")
include(":loritta-plugins:artsy-joy-lori")
include(":loritta-plugins:minecraft-stuff")
include(":loritta-plugins:quirky-stuff")
include(":loritta-plugins:fortnite-stuff")
include(":loritta-plugins:profile-designs")
include(":loritta-plugins:rosbife")
include(":loritta-plugins:funfunfun")
include(":loritta-plugins:donators-ostentation")
include(":loritta-plugins:staff-lorittaban")
include(":loritta-plugins:auto-banner-changer")
include(":loritta-plugins:loritta-birthday-2020-event")
include(":loritta-plugins:helping-hands")
include(":loritta-plugins:mal-commands")
include(":loritta-plugins:lori-guild-stuff")
include(":loritta-plugins:lori-broker")
include(":loritta-plugins:html-provider")

// Website
include(":loritta-website")
include(":loritta-website:spicy-morenitta")
include(":loritta-website:embed-renderer")
include(":loritta-website:embed-editor-crosswindow")
include(":loritta-website:embed-editor")

// Misc
include(":temmie-discord-auth")
include(":shard-controller")