package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import java.util.*

class VerifyMessageCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Verifymessage
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD) {
        executor = VerifyMessageExecutor(m)
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    class VerifyMessageExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val url = string("url", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            // TODO: If someone copies a image when clicking on the image -> copy from the opened image, the URL is the media.discordapp.com one, and that always copies the image in webp format
            //  We need to convert them to cdn.discordapp.com, which doesn't seem to be impossible

            val url = args[options.url]

            val imageByteArray = m.http.get(url).readBytes()

            val chunks = readChunksFromPng(imageByteArray)

            val textChunks = chunks.filter { it.type == "tEXt" }
            val textChunksAsStrings = textChunks.map { String(it.data, Charsets.US_ASCII) }
            val loriMessageDataAsString = textChunksAsStrings.firstOrNull { it.startsWith("LORIMESSAGEDATA:") }
            context.reply(false) {
                if (loriMessageDataAsString == null) {
                    content = "A imagem não é uma imagem criada pela Loritta"
                } else {
                    val json = Json.decodeFromString<MagicMessage>(Base64.getDecoder().decode(loriMessageDataAsString.substringAfter("LORIMESSAGEDATA:")).toString(Charsets.UTF_8))

                    content = "Mensagem válida!\n\nEnviado por: ${json.userId}\nConteúdo: ${json.content}"
                }
            }
        }
    }
}