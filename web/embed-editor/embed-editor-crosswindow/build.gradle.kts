plugins {
	kotlin("js")
	kotlin("plugin.serialization")
}

kotlin {
	js {
		browser()
	}

	sourceSets {
		js().compilations["main"].defaultSourceSet {
			dependencies {
				api(project(":web:embed-editor:embed-renderer"))
			}
		}
	}
}