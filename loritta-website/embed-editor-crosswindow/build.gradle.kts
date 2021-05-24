plugins {
	kotlin("js")
	kotlin("plugin.serialization") version Versions.KOTLIN_FRONTEND
}

kotlin {
	js {
		browser()
	}

	sourceSets {
		js().compilations["main"].defaultSourceSet {
			dependencies {
				api(project(":loritta-website:embed-renderer"))
			}
		}
	}
}