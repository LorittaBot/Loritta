plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
}

kotlin {
	js(IR) {
		browser()
	}

	sourceSets {
		jsMain {
			dependencies {
				api(project(":web:embed-editor:embed-renderer"))
			}
		}
	}
}