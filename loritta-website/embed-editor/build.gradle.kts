val kotlinSerialization by lazy { ext["kotlin-serialization"] as String }

plugins {
	kotlin("js")
	kotlin("plugin.serialization") version "1.4.30"
}

kotlin {
	js {
		browser()
	}

	sourceSets {
		js().compilations["main"].defaultSourceSet {
			dependencies {
				implementation(project(":loritta-api"))
				implementation(project(":loritta-serializable-commons"))
				implementation(project(":loritta-website:embed-renderer"))
				implementation(project(":loritta-website:embed-editor-crosswindow"))
			}
		}
	}
}