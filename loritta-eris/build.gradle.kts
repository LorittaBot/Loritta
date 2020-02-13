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
    compile(kotlin("stdlib-js"))
	implementation(project(":loritta-api"))
}

tasks {
	compileKotlinJs {
		this.kotlinOptions.moduleKind = "commonjs"
	}

	val unpackKotlinJsStdlib by registering {
		group = "build"
		description = "Unpack the Kotlin JavaScript standard library"
		val outputDir = file("$buildDir/$name")
		inputs.property("compileClasspath", configurations.compileClasspath.get())
		outputs.dir(outputDir)

		doLast {
			configurations.compileClasspath.get().all {
				copy {
					includeEmptyDirs = false
					from(zipTree(it))
					into(outputDir)
					include("**/*.js")
					exclude("META-INF/**")
				}
				true
			}
		}
	}
	val assembleWeb by registering(Copy::class) {
		group = "build"
		description = "Assemble the web application"
		includeEmptyDirs = false
		from(unpackKotlinJsStdlib)
		into("$buildDir/web")
	}
	assemble {
		dependsOn(assembleWeb)
	}
}