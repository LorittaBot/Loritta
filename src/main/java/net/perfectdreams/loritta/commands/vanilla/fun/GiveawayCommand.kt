package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
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
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        val createGiveaway = context.reply(
                LoriReply(
                        message = locale["commands.fun.giveaway.giveawayName"],
                        prefix = "\uD83E\uDD14"
                )
        )

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

                                val epoch = time.convertToEpochMillis()

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
                                        channel,
                                        reason,
                                        description,
                                        reaction,
                                        epoch,
                                        numberOfWinners
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}