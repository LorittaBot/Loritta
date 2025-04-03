plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
	id("org.jetbrains.compose") version "1.7.3"
	id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

repositories {
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	google()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile> {
	kotlinOptions {
		// Jetpack Compose doesn't support Kotlin 1.7.10 yet, but the latest version seems to compile just fine under Kotlin 1.7.10
		freeCompilerArgs += listOf(
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
			// Fixes an issue where "java.lang.IllegalStateException: IdSignature clash" when compiling the ReputationRoute's ReputationLeaderboardEntry
			// (probably related to compose x kotlinx.serialization?)
			// https://github.com/JetBrains/compose-multiplatform/issues/3418
			"-Xklib-enable-signature-clash-checks=false",

			// Live Literals seems to be only used for hot reloading in dev mode, but Compose Web doesn't support hot reload yet
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:liveLiterals=false",
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:liveLiteralsEnabled=false"
		)
	}
}

kotlin {
	js(IR) {
		browser()
		binaries.executable()
	}

	sourceSets {
		val jsMain by getting {
			dependencies {
				implementation(compose.html.core)
				implementation(compose.runtime)
				implementation(kotlin("stdlib-common"))
				implementation(project(":common"))
				implementation(project(":loritta-serializable-commons"))
				implementation(project(":web:embed-editor:embed-renderer"))
				implementation(project(":web:embed-editor:embed-editor-crosswindow"))
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
				implementation(project(":discord-chat-markdown-parser"))
				api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
				api("io.ktor:ktor-client-js:${Versions.KTOR}")
				implementation("app.softwork:kotlinx-uuid-core:0.0.17")
				// api(npm("htmx.org", "2.0.2"))
				api(npm("hyperscript.org", "0.9.12"))

				// Yes, deprecated... but we need this because if we don't add this, DCE will fail :(
				api(npm("text-encoding", "0.7.0"))
			}
		}
	}
}