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
				api("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
				api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
			}
		}
	}
}