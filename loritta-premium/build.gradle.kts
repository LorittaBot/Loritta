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
	compile(project(":loritta-api-jvm"))
	compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
	compile("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.0-M1")
	compile("com.google.guava:guava:28.0-jre")
	compile(kotlin("stdlib-jdk8"))
	compile(kotlin("script-util"))
	compile(kotlin("compiler"))
	compile(kotlin("scripting-compiler"))
	compile("org.slf4j:slf4j-api:1.7.26")
	compile("ch.qos.logback:logback-core:1.2.3")
	compile("ch.qos.logback:logback-classic:1.2.3")
	compile("net.perfectdreams.commands:command-framework-core:0.0.8")
	compile("com.github.ben-manes.caffeine:caffeine:2.7.0")
	compile("net.dv8tion:JDA:$jdaVersion")
	compile("org.mongodb:mongodb-driver:3.10.2")
	compile("org.postgresql:postgresql:42.2.5")
	compile("org.jetbrains.exposed:exposed:0.14.4")
	compile("com.zaxxer:HikariCP:3.3.1")
	compile("io.github.microutils:kotlin-logging:1.6.26")
	compile("com.fasterxml.jackson.core:jackson-databind:2.9.9")
	compile("com.fasterxml.jackson.core:jackson-annotations:2.9.9")
	compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9")
	compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
	compile("com.fasterxml.jackson.module:jackson-module-parameter-names:2.9.9")
	compile("org.honton.chas.hocon:jackson-dataformat-hocon:1.1.1")
	compile("com.typesafe:config:1.3.4")
	compile("com.github.salomonbrys.kotson:kotson:2.5.0")
	compile("io.ktor:ktor-server-core:$ktorVersion")
	compile("io.ktor:ktor-server-netty:$ktorVersion")
	compile("io.ktor:ktor-websockets:$ktorVersion")
	compile("io.ktor:ktor-client-core:$ktorVersion")
	compile("io.ktor:ktor-client-cio:$ktorVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
	testImplementation("io.mockk:mockk:1.9.3")
	testImplementation("org.assertj:assertj-core:3.12.2")
}

val fatJar = (extra["fat-jar-stuff"] as (String, Map<String, String>) -> (Task)).invoke(
		"net.perfectdreams.loritta.premium.LorittaPremiumLauncher",
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