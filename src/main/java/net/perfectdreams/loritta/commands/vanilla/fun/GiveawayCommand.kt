package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager

class GiveawayCommand : LorittaCommand(arrayOf("giveaway", "sorteio"), CommandCategory.FUN) {
    override val discordPermissions = listOf(
            Permission.MESSAGE_MANAGE
    )

    override val canUseInPrivateChannel = false

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.fun.giveaway.description"]
    }

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale, args: Array<String>) {
        var customGiveawayMessage: String? = null

        if (args.isNotEmpty()) {
            val customMessage = args.joinToString(" ")

            val message = MessageUtils.generateMessage(args.joinToString(" "), listOf(), context.discordGuild, mapOf(), true)

            if (message != null) {
                context.reply(
                        LoriReply(
                                message = locale["commands.fun.giveaway.giveawayValidCustomMessage"],
                                prefix = Emotes.LORI_TEMMIE
                        )
                )

                val giveawayMessage = GiveawayManager.createGiveawayMessage(
                        context.locale,
                        "Exemplo de Giveaway",
                        "Apenas um exemplo!",
                        "\uD83C\uDF89",
                        System.currentTimeMillis() + 120_000,
                        context.discordGuild!!,
                        customMessage
                )

                context.sendMessage(giveawayMessage)
                customGiveawayMessage = customMessage
            }
        }

        val createGiveaway = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayName"],
                        prefix = "\uD83E\uDD14"
                )
        )

        addCancelOption(context, createGiveaway)

        createGiveaway.onResponseByAuthor(context) {
            val reason = it.message.contentRaw

            createGiveaway.invalidateInteraction()
            createGiveaway.delete()

            val giveawayDescription = context.reply(
                    LoriReply(
                            message = locale["commands.fun.giveaway.giveawayDescription"],
                            prefix = "\uD83E\uDD14"
                    )
            )

            addCancelOption(context, giveawayDescription)

            giveawayDescription.onResponseByAuthor(context) {
                val description = it.message.contentRaw

                giveawayDescription.invalidateInteraction()
                giveawayDescription.delete()

                val giveawayTime = context.reply(
                        LoriReply(
                                message = locale["commands.fun.giveaway.giveawayDuration"],
                                prefix = "\uD83E\uDD14"
                        )
                )

                addCancelOption(context, giveawayTime)

                giveawayTime.onResponseByAuthor(context) {
                    val time = it.message.contentRaw

                    giveawayTime.invalidateInteraction()
                    giveawayTime.delete()

                    val giveawayReaction = context.reply(
                            LoriReply(
                                    message = locale["commands.fun.giveaway.giveawayReaction"],
                                    prefix = "\uD83E\uDD14"
                            )
                    )

                    addCancelOption(context, giveawayReaction)

                    giveawayReaction.onResponseByAuthor(context) {
                        var reaction = it.message.emotes.firstOrNull()?.id ?: it.message.contentRaw

                        giveawayReaction.invalidateInteraction()
                        giveawayReaction.delete()

                        val giveawayWhere = context.reply(
                                LoriReply(
                                        message = locale["commands.fun.giveaway.giveawayChannel"],
                                        prefix = "\uD83E\uDD14"
                                )
                        )

                        addCancelOption(context, giveawayWhere)

                        giveawayWhere.onResponseByAuthor(context) {
                            val pop = it.message.contentRaw
                            var channel: TextChannel? = null

                            val channels = context.discordGuild!!.getTextChannelsByName(pop, false)

                            if (channels.isNotEmpty()) {
                                channel = channels[0]
                            } else {
                                val id = pop
                                        .replace("<", "")
                                        .replace("#", "")
                                        .replace(">", "")

                                if (id.isValidSnowflake()) {
                                    channel = context.discordGuild.getTextChannelById(id)
                                }
                            }

                            if (channel == null) {
                                context.reply(
                                        LoriReply(
                                                "Canal inválido!",
                                                Constants.ERROR
                                        )
                                )
                                return@onResponseByAuthor
                            }

                            if (!channel.canTalk()) {
                                context.reply(
                                        LoriReply(
                                                "Eu não posso falar no canal de texto!",
                                                Constants.ERROR
                                        )
                                )
                                return@onResponseByAuthor
                            }

                            if (!channel.canTalk(context.handle)) {
                                context.reply(
                                        LoriReply(
                                                "Você não pode falar no canal de texto!",
                                                Constants.ERROR
                                        )
                                )
                                return@onResponseByAuthor
                            }

                            giveawayWhere.invalidateInteraction()
                            giveawayWhere.delete()

                            val giveawayCount = context.reply(
                                    LoriReply(
                                            message = locale["commands.fun.giveaway.giveawayWinnerCount"],
                                            prefix = "\uD83E\uDD14"
                                    )
                            )

                            addCancelOption(context, giveawayCount)


                            giveawayCount.onResponseByAuthor(context) {
                                val numberOfWinners = it.message.contentRaw.toIntOrNull()

                                if (numberOfWinners == null) {
                                    context.reply(
                                            LoriReply(
                                                    "Eu não sei o que você colocou aí, mas tenho certeza que não é um número.",
                                                    Constants.ERROR
                                            )
                                    )
                                    return@onResponseByAuthor
                                }

                                if (numberOfWinners !in 1..20) {
                                    context.reply(
                                            LoriReply(
                                                    "Precisa ter, no mínimo, um ganhador e, no máximo, vinte ganhadores!",
                                                    Constants.ERROR
                                            )
                                    )
                                    return@onResponseByAuthor
                                }

                                val epoch = time.convertToEpochMillisRelativeToNow()

                                try {
                                    // Testar se é possível usar o emoticon atual
                                    val emoteId = reaction.toLongOrNull()
                                    if (emoteId != null) {
                                        val emote = lorittaShards.getEmoteById(emoteId.toString())

                                        if (lorittaShards.getEmoteById(emoteId.toString()) == null) {
                                            reaction = "\uD83C\uDF89"
                                        } else {
                                            giveawayCount.handle.addReaction(emote).await()
                                        }
                                    } else {
                                        giveawayCount.handle.addReaction(reaction).await()
                                    }
                                } catch (e: Exception) {
                                    reaction = "\uD83C\uDF89"
                                }

                                giveawayCount.delete()

                                GiveawayManager.spawnGiveaway(
                                        loritta.getLocaleById(context.config.localeId),
                                        channel,
                                        reason,
                                        description,
                                        reaction,
                                        epoch,
                                        numberOfWinners,
                                        customGiveawayMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun addCancelOption(context: DiscordCommandContext, message: DiscordMessage) {
        message.handle.onReactionAddByAuthor(context) {
            if (it.reactionEmote.idLong == 412585701054611458L) {
                message.delete()
                context.reply(
                        LoriReply(
                                context.locale["commands.fun.giveaway.giveawaySetupCancelled"]
                        )
                )
            }
        }
    }
}