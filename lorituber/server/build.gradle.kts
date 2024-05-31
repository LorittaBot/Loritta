kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.JVM_TARGET))
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version libs.versions.jib
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":pudding:client"))
    implementation(project(":loritta-serializable-commons"))
    implementation("io.ktor:ktor-server-core:${Versions.KTOR}")
    implementation("io.ktor:ktor-server-netty:${Versions.KTOR}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLIN_COROUTINES}")
}

tasks.test {
    useJUnitPlatform()
}

jib {
    container {
        mainClass = "net.perfectdreams.loritta.lorituber.server.LoriTuberServerLauncher"
        environment = environment.toMutableMap().apply {
            fun setIfPresent(propName: String, envName: String) {
                val propValue = System.getProperty(propName, null)
                // Only set if it is not null, because if it is, Jib complains
                if (propValue != null)
                    this[envName] = propValue
            }

            setIfPresent("commit.hash", "COMMIT_HASH")
            setIfPresent("build.id", "BUILD_ID")
        }
    }

    to {
        image = "ghcr.io/lorittabot/loritta-morenitta"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "tar://${File(rootDir, "docker/image.tar").absoluteFile}"
    }
}