package net.perfectdreams.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.convertToEpochMillis
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
                        var reaction = it.message.contentRaw

                        if (reaction.startsWith("<")) {
                            reaction = reaction.substring(2, reaction.length - 1)
                        }
                        giveawayReaction.delete()

                        val giveawayWhere = context.reply(
                                LoriReply(
                                        message = "Em qual canal deverá acontecer o giveaway?",
                                        prefix = "\uD83E\uDD14"
                                )
                        )

                        giveawayWhere.onResponseByAuthor(context) {
                            val where = it.message.contentRaw
                            val epoch = time.convertToEpochMillis()

                            context.sendMessage("$reason, $time, $reaction, $where")

                            try {
                                giveawayWhere.handle.addReaction(reaction).await()
                            } catch (e: Exception) {
                                reaction = "\uD83C\uDF89"
                            }

                            giveawayWhere.delete()

                            GiveawayManager.spawnGiveaway(
                                    it.textChannel!!, /* it.guild!!.getTextChannelsByName(where, true)[0] */
                                    reason,
                                    description,
                                    reaction,
                                    epoch
                            )

                            /* val channel = it.guild!!.getTextChannelsByName(where, true)[0]

                            val embed = createEmbed(reason, description, reaction, epoch)

                            val message = channel.sendMessage(embed).complete()
                            val messageId = message.id

                            message.addReaction(reaction).complete()

                            val giveaway = Giveaway(reason, description, 1, epoch, reaction, channel.id, messageId)

                            /* thread {
                                val config = loritta.getServerConfigForGuild(context.guild.id)

                                config.giveaways.add(giveaway)

                                loritta save config
                            } */ */
                        }
                    }
                }
            }
        }
    }
}