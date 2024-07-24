package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeUtil
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
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
import net.perfectdreams.loritta.morenitta.messageverify.SavedMessage
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import java.util.*

class VerifyMessageCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Verifymessage
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Url.Label, I18N_PREFIX.Url.Description) {
            executor = VerifyMessageURLExecutor(m, this@VerifyMessageCommand)
        }

        subcommand(I18N_PREFIX.File.Label, I18N_PREFIX.File.Description) {
            executor = VerifyMessageFileExecutor(m, this@VerifyMessageCommand)
        }
    }

    suspend fun stuff(context: UnleashedContext, imageByteArray: ByteArray) {
        val chunks = readChunksFromPng(imageByteArray)

        val textChunks = chunks.filter { it.type == "tEXt" }
        val textChunksAsStrings = textChunks.map { String(it.data, Charsets.US_ASCII) }
        val loriMessageDataAsString = textChunksAsStrings.firstOrNull { it.startsWith("LORIMESSAGEDATA:") }
        context.reply(false) {
            if (loriMessageDataAsString == null) {
                content = "A imagem não é uma imagem criada pela Loritta"
            } else {
                val json = Json.decodeFromString<SavedMessage>(Base64.getDecoder().decode(loriMessageDataAsString.substringAfter("LORIMESSAGEDATA:")).toString(Charsets.UTF_8))

                embed {
                    title = "${Emotes.CheckMark} Mensagem válida!"

                    field("${Emotes.LoriId} IDs", "**Usuário:** `${json.author.id}`\n**Servidor:** `${json.guildId}`\n**Canal:** `${json.channelId}`\n**Mensagem:** `${json.id}`", true)

                    val userAvatarId = json.author.avatarId
                    val avatarUrl = if (userAvatarId != null) {
                        val extension = if (userAvatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
                            "gif"
                        } else { "png" }

                        "https://cdn.discordapp.com/avatars/${json.author.id}/${userAvatarId}.${extension}?size=256"
                    } else {
                        val avatarId = (json.author.id shr 22) % 6

                        "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
                    }

                    if (json.guild != null) {
                        field("Nome do Servidor", json.guild.name)
                    }

                    val guildId = json.guildId
                    if (guildId != null) {
                        field("Servidor criado em", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(TimeUtil.getTimeCreated(guildId)))
                    }

                    thumbnail = avatarUrl

                    footer("Cuidado! Não se esqueça de verificar se o usuário é realmente quem ele é! Uma mensagem válida não significa que o autor da mensagem não pode ser uma conta fake tentando se passar por outro usuário.")
                }
                actionRow(
                    m.interactivityManager.buttonForUser(
                        context.user,
                        ButtonStyle.SECONDARY,
                        "Enviar Cópia da Mensagem"
                    ) { context ->
                        context.reply(true) {
                            this.content = json.content

                            for (embed in json.embeds) {
                                embed {
                                    this.title = embed.title
                                    this.url = embed.url
                                    this.description = embed.description
                                    this.color = embed.color
                                    this.image = embed.image?.url
                                    this.thumbnail = embed.thumbnail?.url

                                    for (field in embed.fields) {
                                        field(field.name ?: "", field.value ?: "", field.isInline)
                                    }

                                    if (embed.author != null) {
                                        author(embed.author.name, embed.author.url, embed.author.iconUrl)
                                    }

                                    if (embed.footer != null) {
                                        this.footer(embed.footer.text ?: "", embed.footer.iconUrl)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    class VerifyMessageURLExecutor(val m: LorittaBot, val verifyMessageCommand: VerifyMessageCommand) : LorittaSlashCommandExecutor() {
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

            verifyMessageCommand.stuff(context, imageByteArray)
        }
    }

    class VerifyMessageFileExecutor(val m: LorittaBot, val verifyMessageCommand: VerifyMessageCommand) : LorittaSlashCommandExecutor() {
        class Options : ApplicationCommandOptions() {
            val file = attachment("file", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val file = args[options.file]

            val imageByteArray = m.http.get(file.url).readBytes()

            verifyMessageCommand.stuff(context, imageByteArray)
        }
    }
}