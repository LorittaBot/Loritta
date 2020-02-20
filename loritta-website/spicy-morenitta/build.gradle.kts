plugins {
	kotlin("js")
	id("kotlinx-serialization") version "1.3.0" apply true
}

repositories {
    mavenCentral()
	jcenter()
	maven("https://kotlin.bintray.com/kotlinx")
}

kotlin {
	target {
		browser {
		}
	}

	sourceSets["main"].dependencies {
		implementation(project(":loritta-api"))
		implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.6.11")
		implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.0.1")
		implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.9.1")
		implementation("io.ktor:ktor-client-js:1.3.0")
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
				exclude("**/*.kjsm")
			}
			// from(runDceKotlinJs)
			into("$buildDir/web")
		}
		assemble {
			dependsOn(assembleWeb)
		}
	}
}