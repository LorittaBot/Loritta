plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
	id("org.jetbrains.compose") version "1.2.0-alpha01-dev750"
}

repositories {
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	google()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		// Live Literals seems to be only used for hot reloading in dev mode, but Compose Web doesn't support hot reload yet
		freeCompilerArgs += listOf(
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:liveLiterals=false",
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:liveLiteralsEnabled=false"
		)
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile> {
	kotlinOptions {
		// Jetpack Compose doesn't support Kotlin 1.7.10 yet, but the latest version seems to compile just fine under Kotlin 1.7.10
		freeCompilerArgs += listOf(
			"-P",
			"plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true",
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
				implementation(compose.web.core)
				implementation(compose.runtime)
				implementation(kotlin("stdlib-common"))
				implementation(project(":common"))
				implementation(project(":loritta-serializable-commons"))
				implementation(project(":web:embed-editor:embed-renderer"))
				implementation(project(":web:embed-editor:embed-editor-crosswindow"))
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
				api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
				api("io.ktor:ktor-client-js:${Versions.KTOR}")
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")
				implementation("app.softwork:kotlinx-uuid-core:0.0.17")

				// Yes, deprecated... but we need this because if we don't add this, DCE will fail :(
				api(npm("text-encoding", "0.7.0"))
			}
		}
	}
}