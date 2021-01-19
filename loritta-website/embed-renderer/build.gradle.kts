val kotlinSerialization by lazy { ext["kotlin-serialization"] as String }

plugins {
	kotlin("js")
	kotlin("plugin.serialization") version "1.4.21"
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
				api("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")
				api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialization")
			}
		}
	}
}