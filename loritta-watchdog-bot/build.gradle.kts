import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val loriVersion by lazy { ext["lori-version"] as String }
val kotlinVersion by lazy { ext["kotlin-version"] as String }
val ktorVersion by lazy { ext["ktor-version"] as String }
val jdaVersion by lazy { ext["jda-version"] as String }

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

plugins {
	java
	kotlin("jvm")
	`maven-publish`
}

val sourcesJar by tasks.registering(Jar::class) {
	archiveClassifier.set("sources")
	from(sourceSets.main.get().allSource)
}

publishing {
	repositories {
		mavenLocal()
	}
	publications {
		register("mavenJava", MavenPublication::class) {
			from(components["java"])
			artifact(sourcesJar.get())
		}
	}
}

repositories {
	jcenter()
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	api(project(":loritta-api"))
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.0-M1")
	api("com.google.guava:guava:28.0-jre")
	api("com.google.code.gson:gson:2.8.6")
	api(kotlin("stdlib-jdk8"))
	api(kotlin("script-util"))
	api(kotlin("compiler"))
	api(kotlin("scripting-compiler"))
	api("org.slf4j:slf4j-api:1.7.26")
	api("org.twitter4j:twitter4j-core:[4.0,)")
	api("ch.qos.logback:logback-core:1.2.3")
	api("ch.qos.logback:logback-classic:1.2.3")
	api("net.perfectdreams.commands:command-framework-core:0.0.8")
	api("com.github.ben-manes.caffeine:caffeine:2.7.0")
	api("net.dv8tion:JDA:$jdaVersion")
	api("org.mongodb:mongodb-driver:3.10.2")
	api("org.postgresql:postgresql:42.2.5")
	api("com.zaxxer:HikariCP:3.3.1")
	api("org.jetbrains.exposed:exposed:0.14.4")
	api("io.github.microutils:kotlin-logging:1.6.26")
	api("com.fasterxml.jackson.core:jackson-databind:2.9.9")
	api("com.fasterxml.jackson.core:jackson-annotations:2.9.9")
	api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9")
	api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
	api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.9")
	api("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")
	api("com.typesafe:config:1.3.4")
	api("com.github.salomonbrys.kotson:kotson:2.5.0")
	api("io.ktor:ktor-server-core:$ktorVersion")
	api("io.ktor:ktor-server-netty:$ktorVersion")
	api("io.ktor:ktor-websockets:$ktorVersion")
	api("io.ktor:ktor-client-core:$ktorVersion")
	api("io.ktor:ktor-client-cio:$ktorVersion")
	api("com.rometools:rome:1.12.2")
	api("org.jsoup:jsoup:1.12.1")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
	testImplementation("io.mockk:mockk:1.9.3")
	testImplementation("org.assertj:assertj-core:3.12.2")
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
		"net.perfectdreams.loritta.watchdog.WatchdogBotLauncher",
		mapOf(
				"JDA-Version" to jdaVersion
		)
)

tasks {
	"build" {
		dependsOn(fatJar)
	}
}

tasks.test {
	useJUnitPlatform()
}