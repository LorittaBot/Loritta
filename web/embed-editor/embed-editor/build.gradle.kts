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
				implementation(project(":common"))
				implementation(project(":loritta-serializable-commons"))
				implementation(project(":loritta-website:embed-renderer"))
				implementation(project(":loritta-website:embed-editor-crosswindow"))
			}
		}
	}
}