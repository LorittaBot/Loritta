plugins {
	kotlin("js")
}

kotlin {
	target {
		nodejs {
		}
	}

	sourceSets["main"].dependencies {
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.3")
		implementation(npm("eris", "0.11.2"))
		implementation(project(":loritta-plugins:rosbife"))
		implementation("io.ktor:ktor-client-js:1.3.1")
	}
}

repositories {
    mavenCentral()
	jcenter()
	maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    api(kotlin("stdlib-js"))
	implementation(project(":loritta-api"))
}

tasks {
	compileKotlinJs {
		this.kotlinOptions.moduleKind = "commonjs"
	}
}