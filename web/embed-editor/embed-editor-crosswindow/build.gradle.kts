plugins {
	kotlin("js")
	kotlin("plugin.serialization")
}

kotlin {
	js(IR) {
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