plugins {
	kotlin("js")
	kotlin("plugin.serialization") version "1.4.10"
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