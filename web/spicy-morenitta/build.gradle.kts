plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
	id("org.jetbrains.compose")
	id("org.jetbrains.kotlin.plugin.compose")
}

repositories {
	maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
	google()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile> {
	compilerOptions {
		// Jetpack Compose doesn't support Kotlin 1.7.10 yet, but the latest version seems to compile just fine under Kotlin 1.7.10
        freeCompilerArgs.addAll(
            listOf(
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
				implementation("net.perfectdreams.compose.htmldreams:html-core:1.7.3")
				implementation(compose.runtime)
				implementation(kotlin("stdlib-common"))
				implementation(project(":common"))
				implementation(project(":loritta-serializable-commons"))
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
				implementation(project(":discord-chat-markdown-parser"))
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.js)
				implementation("app.softwork:kotlinx-uuid-core:0.0.17")
				// api(npm("htmx.org", "2.0.2"))
				api(npm("hyperscript.org", "0.9.12"))
			}
		}
	}
}