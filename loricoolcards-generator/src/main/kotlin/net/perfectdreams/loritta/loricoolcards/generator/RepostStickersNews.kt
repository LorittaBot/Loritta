package net.perfectdreams.loritta.loricoolcards.generator

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.loricoolcards.generator.utils.config.LoriCoolCardsGeneratorProductionStickersConfig
import net.perfectdreams.loritta.morenitta.utils.readConfigurationFromFile
import java.io.File

fun main() {
    val configurationFile = File(System.getProperty("conf") ?: "./loricoolcards-production-stickers-generator.conf")
    val http = HttpClient {}

    if (!configurationFile.exists()) {
        println("Missing configuration file!")
        System.exit(1)
        return
    }

    val config = readConfigurationFromFile<LoriCoolCardsGeneratorProductionStickersConfig>(configurationFile)

    val jda = JDABuilder.createLight(config.botToken)
        .build()
        .awaitReady()

    val messages = listOf(
        1280170686123474956L,
        1280170746630639719L,
        1280170816079925480L,
        1280170867300499476L,
        1280170918601162792L,
        1280170973286633654L,
        1280171036746448978L,
        1280171095692935208L,
        1280171134578589770L,
        1280171193395318815L
    )

    val channel = jda.getGuildChannelById(1268382385280651336) as GuildMessageChannel
    val targetChannel = jda.getGuildChannelById(513404370931417088L) as GuildMessageChannel
    messages.forEach {
        val message = channel.retrieveMessageById(it).complete()
        println(message)

        val bytes = message.attachments.map {
            runBlocking {
                it.fileName to http.get(it.url).readBytes()
            }
        }

        targetChannel.sendMessage(
            MessageCreateBuilder.fromMessage(message)
                .apply {
                    setContent(message.contentRaw.replace("Temporada 5", "Temporada 6").replace("TEMPORADA 5", "TEMPORADA 6").replace("Minecraft 1.20.6", "Minecraft 1.21.1"))
                }
                .build()
        )
            .apply {
                for (file in bytes) {
                    this.addFiles(FileUpload.fromData(file.second, file.first))
                }
            }
            .complete()
    }
}