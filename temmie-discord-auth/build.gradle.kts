kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
	}
}

plugins {
	java
	kotlin("jvm")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0-M1")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.3.0-M1")
	api("org.slf4j:slf4j-api:1.7.26")
	api("ch.qos.logback:logback-core:1.4.11")
	api("ch.qos.logback:logback-classic:1.4.11")
	api("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING}")
	api("com.google.code.gson:gson:2.8.6")
	api("com.github.salomonbrys.kotson:kotson:2.5.0")
	api("io.ktor:ktor-client-core:${Versions.KTOR}")
	api("io.ktor:ktor-client-cio:${Versions.KTOR}")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.0-M1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.0-M1")
	testImplementation("io.mockk:mockk:1.9.3")
	testImplementation("org.assertj:assertj-core:3.12.2")
}

tasks.test {
	useJUnitPlatform()
}
