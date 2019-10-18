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
include(":loritta-api")
include(":loritta-api-jvm")
include(":loritta-core")
include(":loritta-cli")
include(":loritta-discord")
include(":loritta-amino")

// Plugins
include(":loritta-plugins")
include(":loritta-plugins:artsy-joy-lori")
include(":loritta-plugins:funny-lori")
include(":loritta-plugins:minecraft-stuff")
include(":loritta-plugins:quirky-stuff")
include(":loritta-plugins:github-issue-sync")
include(":loritta-plugins:fortnite-stuff")
include(":loritta-plugins:profile-designs")

// Website
include(":loritta-website")
include(":loritta-website:sweet-morenitta")
include(":loritta-website:spicy-morenitta")
include(":loritta-website:lotrunfo-server")

// Watchdoggo
include(":loritta-watchdog-bot")

// Misc
include(":temmie-discord-auth")
include(":loritta-premium")