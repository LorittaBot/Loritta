package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager

class GiveawayCommand : LorittaCommand(arrayOf("giveaway"), CommandCategory.FUN) {
    override val discordPermissions = listOf(Permission.MANAGE_CHANNEL)

    @Subcommand
    suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
        val createGiveaway = context.reply(
                LoriReply(
                        message = "Qual será o nome do giveaway?",
                        prefix = "\uD83E\uDD14"
                )
        )

        createGiveaway.onResponseByAuthor(context) {
            val reason = it.message.contentRaw

            createGiveaway.delete()

            val giveawayDescription = context.reply(
                    LoriReply(
                            message = "Qual será a descrição do giveaway?",
                            prefix = "\uD83E\uDD14"
                    )
            )

            giveawayDescription.onResponseByAuthor(context) {
                val description = it.message.contentRaw
                giveawayDescription.delete()

                val giveawayTime = context.reply(
                        LoriReply(
                                message = "Por enquanto tempo irá durar o giveaway?",
                                prefix = "\uD83E\uDD14"
                        )
                )

                giveawayTime.onResponseByAuthor(context) {
                    val time = it.message.contentRaw
                    giveawayTime.delete()

                    val giveawayReaction = context.reply(
                            LoriReply(
                                    message = "Qual emoji deverá ser usado nas reações?",
                                    prefix = "\uD83E\uDD14"
                            )
                    )

                    giveawayReaction.onResponseByAuthor(context) {
                        var reaction = it.message.emotes.firstOrNull()?.id ?: it.message.contentRaw

                        giveawayReaction.delete()

                        val giveawayWhere = context.reply(
                                LoriReply(
                                        message = "Em qual canal deverá acontecer o giveaway?",
                                        prefix = "\uD83E\uDD14"
                                )
                        )

                        giveawayWhere.onResponseByAuthor(context) {
                            val channel = it.message.contentRaw

                            val giveawayCount = context.reply(
                                    LoriReply(
                                            message = "Quantas pessoas vão poder ganhar o giveaway?",
                                            prefix = "\uD83E\uDD14"
                                    )
                            )

                            giveawayWhere.delete()

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
                                        it.textChannel!!, /* it.guild!!.getTextChannelsByName(where, true)[0] */
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