plugins {
	id("kotlin2js")
	id("kotlinx-serialization") version "1.3.0" apply true
}

repositories {
    mavenCentral()
	jcenter()
	maven("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    compile(project(":loritta-website:spicy-morenitta"))
}

tasks {
	compileKotlin2Js {
		kotlinOptions {
			outputFile = "${sourceSets.main.get().output.resourcesDir}/SpicyChristmas2019.js"
			sourceMap = true
		}
	}
	val assembleWeb by registering(Copy::class) {
		group = "build"
		description = "Assemble the web application"
		includeEmptyDirs = false
		from(sourceSets.main.get().output) {
			exclude("**/*.kjsm")
		}
		into("$buildDir/web")
	}
	assemble {
		dependsOn(assembleWeb)
	}
}