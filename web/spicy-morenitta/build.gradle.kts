plugins {
	kotlin("js")
	kotlin("plugin.serialization")
}

kotlin {
	js(IR) {
		browser()
		binaries.executable()
	}

	sourceSets["main"].dependencies {
		implementation(kotlin("stdlib-common"))
		implementation(project(":common"))
		implementation(project(":loritta-serializable-commons"))
		implementation(project(":web:embed-editor:embed-renderer"))
		implementation(project(":web:embed-editor:embed-editor-crosswindow"))
		implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
		api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLIN_SERIALIZATION}")
		api("io.ktor:ktor-client-js:${Versions.KTOR}")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")

		// Yes, deprecated... but we need this because if we don't add this, DCE will fail :(
		api(npm("text-encoding", "0.7.0"))
	}
}