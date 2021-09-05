plugins {
    kotlin("jvm")
}

dependencies {
    // API = We want to allow dependencies to access those classes
    api(kotlin("stdlib-jdk8"))
    api(project(":common"))
    implementation("net.perfectdreams.loritta.pudding:client:1.1.0-SNAPSHOT")
}