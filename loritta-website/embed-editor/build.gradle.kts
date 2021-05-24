plugins {
	kotlin("js")
	kotlin("plugin.serialization") version Versions.KOTLIN
}

kotlin {
	js {
		browser()
	}

	sourceSets {
		js().compilations["main"].defaultSourceSet {
			dependencies {
				implementation(project(":common-legacy"))
				implementation(project(":loritta-serializable-commons"))
				implementation(project(":loritta-website:embed-renderer"))
				implementation(project(":loritta-website:embed-editor-crosswindow"))
			}
		}
	}
}