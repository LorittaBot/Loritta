package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.messageCommand
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunkUtils
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.bytesToHex
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SaveMessageCommand(val m: LorittaBot) {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Savemessage
    }

    class SaveMessagePublicCommand(val m: LorittaBot) : MessageCommandDeclarationWrapper {
        override fun command() = messageCommand(I18N_PREFIX.LabelPublic, CommandCategory.DISCORD, SaveMessageExecutor(m, true)) {
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        }
    }

    class SaveMessagePrivateCommand(val m: LorittaBot) : MessageCommandDeclarationWrapper {
        override fun command() = messageCommand(I18N_PREFIX.LabelPrivate, CommandCategory.DISCORD, SaveMessageExecutor(m, false)) {
            this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        }
    }

    class SaveMessageExecutor(val m: LorittaBot, val isPublic: Boolean) : LorittaMessageCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val isEphemeral = !isPublic
            context.deferChannelMessage(isEphemeral)

            val savedMessage = SavedMessage(
                message.idLong,
                if (message.hasGuild())
                    SavedAttachedGuild(message.guild.idLong, message.channelIdLong, message.channel.name, message.channelType, message.guild.name, message.guild.iconId)
                else if (message.channelType == ChannelType.GROUP) {
                    val channel = message.channel as GroupChannel

                    SavedGroupChannel(
                        channel.idLong,
                        channel.name,
                        channel.iconId,
                    )
                } else if (message.channelType == ChannelType.PRIVATE) {
                    val channel = message.channel as PrivateChannel
                    SavedPrivateChannel(channel.idLong)
                } else if (message.isFromGuild) {
                    // If the message is from a guild, but we don't know it, then it must be a user app being used in a guild
                    SavedDetachedGuild(message.guildIdLong, message.channelIdLong, message.channel.name, message.channelType)
                } else error("Unsupported channel type"),
                LoriMessageDataUtils.convertUserToSavedUser(message.author),
                message.member?.let {
                    SavedMember(
                        it.nickname,
                        it.roles.map {
                            SavedRole(
                                it.idLong,
                                it.name,
                                it.colorRaw,
                                it.icon?.let {
                                    val emoji = it.emoji
                                    val iconId = it.iconId
                                    if (emoji != null)
                                        SavedUnicodeRoleIcon(emoji)
                                    else if (iconId != null)
                                        SavedCustomRoleIcon(iconId)
                                    else
                                        null
                                }
                            )
                        }
                    )
                },
                message.timeEdited?.toInstant()?.toKotlinInstant(),
                message.contentRaw,
                message.embeds.map {
                    SavedEmbed(
                        it.type,
                        it.title,
                        it.description,
                        it.url,
                        if (it.colorRaw != Role.DEFAULT_COLOR_RAW) it.colorRaw else null,
                        it.author?.let {
                            SavedEmbed.SavedAuthor(
                                it.name,
                                it.url,
                                it.iconUrl,
                                it.proxyIconUrl
                            )
                        },
                        it.fields.map {
                            SavedEmbed.SavedField(
                                it.name,
                                it.value,
                                it.isInline
                            )
                        },
                        it.footer?.let {
                            SavedEmbed.SavedFooter(
                                it.text,
                                it.iconUrl,
                                it.proxyIconUrl
                            )
                        },
                        it.image?.let {
                            SavedEmbed.SavedImage(
                                it.url,
                                it.proxyUrl,
                                it.width,
                                it.height
                            )
                        },
                        it.thumbnail?.let {
                            SavedEmbed.SavedThumbnail(
                                it.url,
                                it.proxyUrl,
                                it.width,
                                it.height
                            )
                        }
                    )
                },
                message.attachments.map {
                    SavedAttachment(
                        it.idLong,
                        it.fileName,
                        it.description,
                        it.contentType,
                        it.size,
                        it.url,
                        it.proxyUrl,
                        if (it.width != -1) it.width else null,
                        if (it.height != -1) it.height else null,
                        it.isEphemeral,
                        it.duration,
                        it.waveform?.let { Base64.getEncoder().encodeToString(it) }
                    )
                },
                message.stickers.map {
                    SavedSticker(
                        it.idLong,
                        it.formatType,
                        it.name
                    )
                },
                SavedMentions(
                    message.mentions.users.map {
                        LoriMessageDataUtils.convertUserToSavedUser(it)
                    },
                    message.mentions.roles.map {
                        SavedRole(
                            it.idLong,
                            it.name,
                            it.colorRaw,
                            it.icon?.let {
                                val emoji = it.emoji
                                val iconId = it.iconId
                                if (emoji != null)
                                    SavedUnicodeRoleIcon(emoji)
                                else if (iconId != null)
                                    SavedCustomRoleIcon(iconId)
                                else
                                    null
                            }
                        )
                    }
                ),
                message.reactions.map {
                    val emoji = it.emoji
                    val savedEmoji = when (emoji) {
                        is CustomEmoji -> {
                            SavedCustomPartialEmoji(
                                emoji.idLong,
                                emoji.name,
                                emoji.isAnimated
                            )
                        }

                        is UnicodeEmoji -> {
                            SavedUnicodePartialEmoji(
                                emoji.name
                            )
                        }

                        else -> error("I don't know how to handle emoji type $emoji")
                    }

                    SavedReaction(
                        it.getCount(MessageReaction.ReactionType.NORMAL),
                        it.getCount(MessageReaction.ReactionType.SUPER),
                        savedEmoji
                    )
                }
            )

            val screenshot = m.discordMessageRendererManager.renderMessage(savedMessage, null)
            val screenshotPNGChunks = PNGChunkUtils.readChunksFromPNG(screenshot)

            val b64Encoded = Base64.getEncoder().encodeToString(Json.encodeToString(savedMessage).toByteArray(Charsets.UTF_8))

            val hashOfTheImage = LoriMessageDataUtils.createSHA256HashOfImage(screenshotPNGChunks).bytesToHex()
            val signingKey = SecretKeySpec("${m.config.loritta.messageVerification.encryptionKey}:$hashOfTheImage".toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(signingKey)
            val doneFinal = mac.doFinal(b64Encoded.toByteArray(Charsets.UTF_8))
            val output = doneFinal.bytesToHex()

            val finalImage = PNGChunkUtils.addChunkToPNG(screenshot, PNGChunkUtils.createTextPNGChunk("${LoriMessageDataUtils.SUB_CHUNK_ID}:${LoriMessageDataUtils.CURRENT_VERSION}:1:${b64Encoded}:$output"))

            // There's a difference here:
            // - Public Messages: Sent on the current channel, public reply
            // - Private Messages: Sent on the user's DMs, ephemeral reply
            val explanationMessages: InlineMessage<*>.() -> (Unit) = {
                styled(
                    context.i18nContext.get(I18N_PREFIX.YouReceivedACopyOfTheMessage(message.jumpUrl)),
                    Emotes.LoriHi
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.WhatYouCanDoWithIt(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriLurk
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.TheMessageIsASnapshot(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriReading
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.TamperingIsDetected(m.commandMentions.verifyMessageUrl)),
                    Emotes.LoriCoffee
                )
            }

            if (isPublic) {
                context.reply(isEphemeral) {
                    explanationMessages()

                    // This makes the file name be a bit long, but it is useful to quickly check which user/guild/channel/message we are talking about
                    val fileName = buildString {
                        append("message-")
                        append(message.author.idLong)
                        if (message.hasGuild()) {
                            append("-")
                            append(message.guildIdLong)
                        }
                        append("-")
                        append(message.channelIdLong)
                        append("-")
                        append(message.idLong)
                        append(".lxrimsg.png")
                    }

                    files += FileUpload.fromData(finalImage, fileName)
                }
            } else {
                try {
                    val messageSent = context.user
                        .openPrivateChannel()
                        .await()
                        .sendMessage(
                            MessageCreate {
                                explanationMessages()

                                // This makes the file name be a bit long, but it is useful to quickly check which user/guild/channel/message we are talking about
                                val fileName = buildString {
                                    append("message-")
                                    append(message.author.idLong)
                                    if (message.hasGuild()) {
                                        append("-")
                                        append(message.guildIdLong)
                                    }
                                    append("-")
                                    append(message.channelIdLong)
                                    append("-")
                                    append(message.idLong)
                                    append(".lxrimsg.png")
                                }

                                files += FileUpload.fromData(finalImage, fileName)
                            }
                        )
                        .await()

                    // This should NEVER be null
                    val attachmentUrl = messageSent.attachments
                        .first()
                        .url

                    context.reply(isEphemeral) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.TheMessageCopyWasSentInYourDMs(attachmentUrl)),
                            Emotes.LoriHi
                        )
                    }
                } catch (e: ErrorResponseException) {
                    if (e.errorResponse == ErrorResponse.CANNOT_SEND_TO_USER) {
                        context.reply(isEphemeral) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.YourDMsAreClosed),
                                Emotes.LoriSob
                            )
                        }
                    }
                    throw e
                }
            }
        }
    }
}