package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.kord.common.kColor
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEditBuilder
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.entities.sticker.GuildSticker
import net.dv8tion.jda.api.entities.sticker.StickerSnowflake
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await

object GuildExpressionsManagerExecutor {
    suspend fun createRemoveStickerMessage(
            context: UnleashedContext,
            stickers: MutableList<GuildSticker>
    ): InlineMessage<*>.() -> (Unit) {
        return {
            embed {
                title = context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Sticker.Remove.RemoveEmbedTitle)
                description = context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Sticker.Remove.RemoveEmbedDescription)
                color = 0x5469EC
            }

            val components = mutableListOf<ItemComponent>()

            run {
                components.add(
                        context.loritta.interactivityManager
                                .stringSelectMenuForUser(context.user.idLong, {
                                    stickers.forEach {
                                        addOption(it.name, it.id)
                                        maxValues = 1

                                    }
                                }) { componentContext, strings ->
                                    val globalDefer = componentContext.deferEdit()
                                    val stickerId = strings[0]

                                    val sticker = componentContext.guild.getStickerById(stickerId)

                                    if (sticker == null) {
                                        componentContext.fail(true) {
                                            styled(
                                                componentContext.i18nContext.get(
                                                    I18nKeysData.Commands.Command.Guild.Sticker.Remove.InvalidSticker
                                                ),
                                                Emotes.Error
                                            )
                                        }
                                    } else {
                                        val editedMessage = MessageEditBuilder {
                                            embed {
                                                title = sticker.name
                                                description = sticker.description
                                                image = sticker.iconUrl

                                                field {
                                                    name = componentContext.i18nContext.get(
                                                        I18nKeysData.Commands.Command.Guild.Sticker.Remove.Related
                                                    )
                                                    value = sticker.tags.joinToString(", ")
                                                    inline = true
                                                }

                                                field {
                                                    name = componentContext.i18nContext.get(
                                                        I18nKeysData.Commands.Command.Guild.Sticker.Remove.Related
                                                    )
                                                    value = "<t:${sticker.timeCreated.toInstant().epochSecond}:f> (<t:${sticker.timeCreated.toInstant().epochSecond}:R>)"
                                                    inline = true
                                                }

                                                color = 0x5469EC
                                            }

                                            run {
                                                components.clear()
                                                components.add(
                                                        context.loritta.interactivityManager
                                                                .buttonForUser(context.user.idLong, ButtonStyle.DANGER, componentContext.i18nContext.get(
                                                                    I18nKeysData.Commands.Command.Guild.Sticker.Remove.StickerButton
                                                                )) {
                                                                    val editedMessage2 = MessageEditBuilder {
                                                                        actionRow(
                                                                                Button.success("successfully-removed-sticker", componentContext.i18nContext.get(
                                                                                    I18nKeysData.Commands.Command.Guild.Sticker.Remove.SuccessfullyRemovedStickerButton
                                                                                )).asDisabled()
                                                                        )
                                                                    }

                                                                    componentContext.guild.deleteSticker(StickerSnowflake.fromId(sticker.id)).queue()

                                                                    globalDefer.editOriginal(editedMessage2.build()).await()

                                                                    it.deferAndEditOriginal { editedMessage2.build() }


                                                                    componentContext.reply(false) {
                                                                        styled(
                                                                            componentContext.i18nContext.get(
                                                                                I18nKeysData.Commands.Command.Guild.Sticker.Remove.SuccessfullyRemovedStickerMessage
                                                                            ),
                                                                            Emotes.CheckMark
                                                                        )
                                                                    }
                                                                }
                                                )
                                            }

                                            actionRow(components)
                                        }

                                        globalDefer.editOriginal(editedMessage.build()).await()
                                    }
                                }
                )
            }
            actionRow(components)
        }
    }

    suspend fun createRemoveEmojiMessage(
        context: UnleashedContext,
        emojis: MutableList<RichCustomEmoji>
    ): InlineMessage<*>.() -> (Unit) {
        return {
            embed {
                title = context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Emoji.Remove.RemoveEmbedTitle)
                description = context.i18nContext.get(I18nKeysData.Commands.Command.Guild.Emoji.Remove.RemoveEmbedDescription)
                color = 0x5469EC
            }

            val components = mutableListOf<ItemComponent>()

            run {
                components.add(
                    context.loritta.interactivityManager
                        .stringSelectMenuForUser(context.user.idLong, {
                            emojis.forEach {
                                addOption(it.name, it.id, it)
                                maxValues = 1
                            }
                        }) { componentContext, strings ->
                            val globalDefer = componentContext.deferEdit()
                            val emojiId = strings[0]

                            val emoji = componentContext.guild.getEmojiById(emojiId)

                            if (emoji == null) {
                                componentContext.fail(true) {
                                    styled(
                                        componentContext.i18nContext.get(
                                            I18nKeysData.Commands.Command.Guild.Emoji.Remove.InvalidEmoji
                                        ),
                                        Emotes.Error
                                    )
                                }
                            } else {
                                val editedMessage = MessageEditBuilder {
                                    embed {
                                        title = emoji.name
                                        description = "`${emoji.asMention}`"
                                        image = emoji.imageUrl

                                        field {
                                            name = componentContext.i18nContext.get(
                                                I18nKeysData.Commands.Command.Guild.Emoji.Remove.CreatedAt
                                            )
                                            value = "<t:${emoji.timeCreated.toInstant().epochSecond}:f> (<t:${emoji.timeCreated.toInstant().epochSecond}:R>)"
                                            inline = true
                                        }

                                        color = 0x5469EC
                                    }

                                    run {
                                        components.clear()
                                        components.add(
                                            context.loritta.interactivityManager
                                                .buttonForUser(context.user.idLong, ButtonStyle.DANGER, componentContext.i18nContext.get(
                                                    I18nKeysData.Commands.Command.Guild.Emoji.Remove.EmojiButton
                                                )) {
                                                    val editedMessage2 = MessageEditBuilder {
                                                        actionRow(
                                                            Button.success("successfully-removed-emoji", componentContext.i18nContext.get(
                                                                I18nKeysData.Commands.Command.Guild.Emoji.Remove.SuccessfullyRemovedEmojiButton
                                                            )).asDisabled()
                                                        )
                                                    }

                                                    emoji.delete().await()

                                                    globalDefer.editOriginal(editedMessage2.build()).await()

                                                    it.deferAndEditOriginal { editedMessage2.build() }

                                                    componentContext.reply(false) {
                                                        styled(
                                                            componentContext.i18nContext.get(
                                                                I18nKeysData.Commands.Command.Guild.Emoji.Remove.SuccessfullyRemovedEmojiMessage
                                                            ),
                                                            Emotes.CheckMark
                                                        )
                                                    }
                                                }
                                        )
                                    }

                                    actionRow(components)
                                }

                                globalDefer.editOriginal(editedMessage.build()).await()
                            }
                        }
                )
            }
            actionRow(components)
        }
    }
}