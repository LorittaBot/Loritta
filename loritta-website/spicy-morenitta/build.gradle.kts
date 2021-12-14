plugins {
	kotlin("js")
	kotlin("plugin.serialization") version Versions.KOTLIN
}

kotlin {
	// We need to use the Legacy compiler, see https://github.com/Kotlin/kotlinx.serialization/issues/1369
	js(LEGACY) {
		browser {
			dceTask {
				// The script is throwing some "Uncaught TypeError: e.defineModule is not a function" errors for some random reason that I can't figure out why
				// So let's just disable DCE for now
				// dceOptions.devMode = true
				keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
			}
		}
	}

	sourceSets["main"].dependencies {
		implementation(kotlin("stdlib-common"))
		implementation(project(":common-legacy"))
		implementation(project(":loritta-serializable-commons"))
		implementation(project(":loritta-website:embed-renderer"))
		implementation(project(":loritta-website:embed-editor-crosswindow"))
		implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
		api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
		api("io.ktor:ktor-client-js:${Versions.KTOR}")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")

		// Yes, deprecated... but we need this because if we don't add this, DCE will fail :(
		api(npm("text-encoding", "0.7.0"))
	}
}