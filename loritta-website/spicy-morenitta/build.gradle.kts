plugins {
	kotlin("js")
	kotlin("plugin.serialization") version Versions.KOTLIN
}

kotlin {
	// We need to use the Legacy compiler, see https://github.com/Kotlin/kotlinx.serialization/issues/1369
	js(LEGACY) {
		browser {
			dceTask {
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

		// ===[ KOTLIN 1.5.0 WORKAROUNDS ]===
		// Because Kotlin 1.5.0 has too many issues omg
		// Workaround for https://kotlinlang.slack.com/archives/C0B8L3U69/p1621863081132900
		// :kotlin_intensifies:
		api("io.ktor:ktor-client-js:1.4.3") {
			version {
				strictly("1.4.3")
			}
		}
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.3") {
			version {
				strictly("1.4.3")
			}
		}

		// Yes, deprecated... but we need this because if we don't add this, DCE will fail :(
		api(npm("text-encoding", "0.7.0"))
	}
}