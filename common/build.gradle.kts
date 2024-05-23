plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("net.perfectdreams.i18nhelper.plugin") version libs.versions.i18nhelperplugin
    id("maven-publish")
}

val generateI18nKeys = tasks.register<net.perfectdreams.i18nhelper.plugin.GenerateI18nKeysTask>("generateI18nKeys") {
    generatedPackage.set("net.perfectdreams.loritta.i18n")
    languageSourceFolder.set(file("../resources/languages/pt/"))
    languageTargetFolder.set(file("$buildDir/generated/languages"))
    translationLoadTransform.set { file, map ->
        // Before, all commands locales were split up into different files, based on the category, example:
        // commands-discord.yml
        // commands:
        //   discord:
        //     userinfo:
        //       description: "owo"
        //
        // However, this had a issue that, if we wanted to move commands from a category to another, we would need to move the locales from
        // the file AND change the locale key, so, if we wanted to change a command category, that would also need to change all locale keys
        // to match. I think that was not a great thing to have.
        //
        // I thought that maybe we could remove the category from the command itself and keep it as "command:" or something, like this:
        // commands-discord.yml
        // commands:
        //   command:
        //     userinfo:
        //       description: "owo"
        //
        // This avoids the issue of needing to change the locale keys in the source code, but we still need to move stuff around if a category changes!
        // (due to the file name)
        // This also has a issue that Crowdin "forgets" who did the translation because the file changed, which is very undesirable.
        //
        // I thought that all the command keys could be in the same file and, while that would work, it would become a mess.
        //
        // So I decided to spice things up and split every command locale into different files, so, as an example:
        // userinfo.yml
        // commands:
        //   discord:
        //     userinfo:
        //       description: "owo"
        //
        // But that's boring, let's spice it up even more!
        // userinfo.yml
        // description: "owo"
        //
        // And, when loading the file, the prefix "commands.command.FileNameHere." is automatically appended to the key!
        // This fixes our previous issues:
        // * No need to change the source code on category changes, because the locale key doesn't has any category related stuff
        // * No need to change locales to other files due to category changes
        // * More tidy
        // * If a command is removed from Loritta, removing the locales is a breeze because you just need to delete the locale key related to the command!
        //
        // Very nice :3
        //
        // So, first, we will check if the commands folder exist and, if it is, we are going to load all the files within the folder and apply a
        // auto prefix to it.
        if (file.parentFile.name == "commands") {
            mapOf(
                "commands" to mapOf<String, Any>(
                    "command" to mapOf<String, Any>(
                        file.nameWithoutExtension to map
                    )
                )
            )
        } else {
            map
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = Versions.JVM_TARGET
        }
        withJava()
    }

    js(IR) {
        // Declares that we want to compile for the browser and for nodejs
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {
            // If a task only has one output, you can reference the task itself
            kotlin.srcDir(generateI18nKeys)

            dependencies {
                // API = We want to allow dependencies to access those classes
                api(kotlin("stdlib-common"))
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlin.logging)
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.serialization.json)
                // Used to serialize state on components
                api(libs.kotlinx.serialization.protobuf)
                api(libs.ktor.client.core)
                api("net.perfectdreams.i18nhelper:core:${libs.versions.i18nhelper.get()}")
                api("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.KOTLINX_DATE_TIME}")

                // Used for Math stuff
                api("com.ionspin.kotlin:bignum:0.3.3")
            }
        }

        // jvmMain and jsMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-jdk8"))

                api("ch.qos.logback:logback-classic:1.4.11")

                // Used for caching
                api("com.github.ben-manes.caffeine:caffeine:3.0.1")

                // Used for config
                api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:${Versions.KOTLIN_SERIALIZATION}")

                // Used for the LocaleManager
                implementation("org.yaml:snakeyaml:1.28")

                // Used by Minecraft related commands
                api("net.perfectdreams.minecraftmojangapi:minecraft-mojang-api:0.0.2")

                // Stuff used by the old stuff in this module
                api("com.fasterxml.jackson.core:jackson-databind:2.9.8")
                api("io.ktor:ktor-client-apache:${Versions.KTOR}")

                // Used for caching
                api("com.github.ben-manes.caffeine:caffeine:3.0.1")

                // Used for config
                api(libs.kotlinx.serialization.hocon)

                // Used for the LocaleManager
                implementation("org.yaml:snakeyaml:1.30")

                // Internationalization
                api("net.perfectdreams.i18nhelper.formatters:icu-messageformat-jvm:${libs.versions.i18nhelper.get()}")
                implementation("com.charleskorn.kaml:kaml:0.38.0")
                implementation("com.ibm.icu:icu4j:70.1")

                // Gabriela Image Server
                api("net.perfectdreams.gabrielaimageserver:client:2.0.11")

                // Emoji Stuff
                api("com.vdurmont:emoji-java:5.1.1")

                // Prometheus, for metrics
                api("io.prometheus:simpleclient:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_hotspot:${Versions.PROMETHEUS}")
                api("io.prometheus:simpleclient_common:${Versions.PROMETHEUS}")
            }
        }

        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                // Required for tests, if this is missing then Gradle will throw
                // "No tests found for given includes: [***Test](filter.includeTestsMatching)"
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("org.junit.jupiter:junit-jupiter:5.4.2")
                implementation("org.assertj:assertj-core:3.19.0")
            }
        }

        js().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-js"))
                api(npm("canvas", "2.6.1"))
                api("io.ktor:ktor-client-js:${Versions.KTOR}")

                // Internationalization
                api("net.perfectdreams.i18nhelper.formatters:intl-messageformat-js:${libs.versions.i18nhelper.get()}")
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")
            credentials(PasswordCredentials::class)
        }
    }
}