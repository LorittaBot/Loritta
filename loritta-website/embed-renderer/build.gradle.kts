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
				api("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
				api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
			}
		}
	}
}