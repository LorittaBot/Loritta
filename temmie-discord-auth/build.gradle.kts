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
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.0-M1")
	api("org.slf4j:slf4j-api:1.7.26")
	api("ch.qos.logback:logback-core:1.2.3")
	api("ch.qos.logback:logback-classic:1.2.3")
	api("io.github.microutils:kotlin-logging:1.6.26")
	api("com.google.code.gson:gson:2.8.6")
	api("com.github.salomonbrys.kotson:kotson:2.5.0")
	api("io.ktor:ktor-client-core:$ktorVersion")
	api("io.ktor:ktor-client-cio:$ktorVersion")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
	testImplementation("io.mockk:mockk:1.9.3")
	testImplementation("org.assertj:assertj-core:3.12.2")
}

tasks.test {
	useJUnitPlatform()
}
