val kotlinSerialization by lazy { ext["kotlin-serialization"] as String }

plugins {
	kotlin("js")
	kotlin("plugin.serialization") version "1.4.30"
}

kotlin {
	js {
		browser {
			dceTask {
				keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
			}
		}
	}

	sourceSets["main"].dependencies {
		implementation(project(":loritta-api"))
		implementation(project(":loritta-serializable-commons"))
		implementation(project(":loritta-website:embed-renderer"))
		implementation(project(":loritta-website:embed-editor-crosswindow"))
		implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.9")
		api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerialization")
		implementation("io.ktor:ktor-client-js:1.4.1")
	}
}