dependencies {
    compile(project(":loritta-api"))
    implementation(kotlin("stdlib-jdk8"))
}

plugins {
    java
    kotlin("jvm")
    `maven-publish`
}