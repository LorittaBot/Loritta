plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("net.perfectdreams.i18nhelper.plugin") version Versions.I18N_HELPER
}

i18nHelper {
    generatedPackage.set("net.perfectdreams.loritta.cinnamon.i18n")
    languageSourceFolder.set("../resources/languages/en/")
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
            kotlinOptions.jvmTarget = "1.8"
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
            kotlin.srcDir("build/generated/languages")

            dependencies {
                // API = We want to allow dependencies to access those classes
                api(kotlin("stdlib-common"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                api("io.github.microutils:kotlin-logging:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.KOTLINX_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.KOTLINX_SERIALIZATION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.KOTLINX_SERIALIZATION}")
                api("io.ktor:ktor-client-core:1.6.0")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
                api("net.perfectdreams.i18nhelper:core:${Versions.I18N_HELPER}")
                api("net.perfectdreams.gabrielaimageserver:client:2.0.0-SNAPSHOT")

                // Used for Math stuff
                api("com.ionspin.kotlin:bignum:0.3.0")
            }
        }

        // jvmMain and jsMain *should* work but for some reason they don't
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                api(kotlin("stdlib-jdk8"))

                // Async Appender is broken in alpha5
                // https://stackoverflow.com/questions/58742485/logback-error-no-attached-appenders-found
                api("ch.qos.logback:logback-classic:1.3.0-alpha4")

                // Used for caching
                api("com.github.ben-manes.caffeine:caffeine:3.0.1")

                // Used for config
                api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:${Versions.KOTLINX_SERIALIZATION}")

                // Used for the LocaleManager
                implementation("org.yaml:snakeyaml:1.28")

                // Internationalization
                api("net.perfectdreams.i18nhelper.formatters:icu-messageformat-jvm:${Versions.I18N_HELPER}")
                implementation("com.charleskorn.kaml:kaml:0.35.0")
                implementation("com.ibm.icu:icu4j:69.1")

                // Used by Minecraft related commands
                api("net.perfectdreams.minecraftmojangapi:minecraft-mojang-api:0.0.1-SNAPSHOT")
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
            }
        }
    }
}