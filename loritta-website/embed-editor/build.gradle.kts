plugins {
	kotlin("js")
	kotlin("plugin.serialization") version "1.3.70"
}

repositories {
	mavenCentral()
	jcenter()
	maven("https://kotlin.bintray.com/kotlinx")
}

kotlin {
	target {
		browser {
			dceTask {
				keep("ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io")
				keep("applyChanges")
			}
		}
	}

	sourceSets["main"].dependencies {
		implementation(project(":loritta-api"))
		implementation(project(":loritta-serializable-commons"))
		implementation(project(":loritta-website:embed-renderer"))
		implementation(project(":loritta-website:embed-editor-crosswindow"))
		// Hacky workaround due to "Can't resolve xyz" dependency
		// https://github.com/Kotlin/kotlinx-io/issues/57
		api(npm("text-encoding"))
		api(npm("bufferutil"))
		api(npm("utf-8-validate"))
		api(npm("abort-controller"))
		api(npm("fs"))
		implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.0.1")
		implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
		implementation("io.ktor:ktor-client-js:1.3.1")
	}

	tasks {
		compileKotlinJs {
			kotlinOptions {
				// outputFile = "${sourceSets["main"].resources.outputDir}/SpicyMorenitta.js"
				sourceMap = true
			}
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
						// Hacky workaround because Gradle tries to extract "text-encoder.js"
						if (it.extension == "js")
							from(it)
						else
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
			from(compileKotlinJs.get().destinationDir) {
				include("**/*.js")
				include("**/*.map")
				exclude("**/*.kjsm")
			}
			// from(runDceKotlinJs)
			into("$buildDir/web")
		}

		build.get().finalizedBy(assembleWeb)

		/* assemble {
			dependsOn(compileKotlinJs)
			// dependsOn(assembleWeb)
		} */
	}
}