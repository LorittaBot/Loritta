package net.perfectdreams.loritta.cinnamon.platform.utils.giveaway

import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.request.KtorRequestException
import dev.kord.rest.service.RestClient
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingGiveaway
import net.perfectdreams.sequins.text.StringUtils

object GiveawayManager {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway.Manager

    suspend fun finishGiveaway(
        giveaway: PuddingGiveaway,
        rest: RestClient,
        i18nContext: I18nContext,
        winnersAmount: Int?
    ) {
        giveaway.finishGiveaway()

        rollWinners(giveaway, rest, i18nContext, winnersAmount ?: giveaway.numberOfWinners)
    }

    private suspend fun rollWinners(
        giveaway: PuddingGiveaway,
        rest: RestClient,
        i18nContext: I18nContext,
        winnersAmount: Int? = giveaway.numberOfWinners
    ) {
        if (giveaway.users.isEmpty()) {
            sendMessageInGiveawayChannel(giveaway, rest) {
                "${Emotes.LoriSob} " + i18nContext.get(I18N_PREFIX.WithoutWinners) + " ${Emotes.LoriTemmie}"
            }

            return
        }

        val winners = mutableListOf<String>()

        if (giveaway.users.size <= winnersAmount!!) {
            if (giveaway.users.size == 1) {
                winners.add(giveaway.users.first())
            } else
                winners.addAll(giveaway.users)
        } else {
            for (i in 0 until winnersAmount) {
                winners.add(giveaway.users.random())
            }
        }

        if (winners.size == 1) {
            sendMessageInGiveawayChannel(giveaway, rest) {
                content = "${Emotes.Tada} **|** " + i18nContext.get(
                    I18N_PREFIX.CongratulationsToTheWinner(
                        "<@${winners.first()}>"
                    )
                )
            }
        } else {
            val chunkedResponse = StringUtils.chunkedLines(
                i18nContext.get(I18N_PREFIX.CongratulationsToTheWinners)
                        + winners.joinToString { "\n${Emotes.Star} **|** <@$it>" },
                1_000,
                forceSplit = true,
                forceSplitOnSpaces = true
            )

            chunkedResponse.forEach {
                sendMessageInGiveawayChannel(giveaway, rest) {
                    content = it
                }
            }
        }

        if (giveaway.awardSonhosPerUser != null || giveaway.awardRoleIds?.isNotEmpty() == true)
            givePrizes(giveaway, rest, i18nContext, winners)
    }

    private suspend fun givePrizes(
        giveaway: PuddingGiveaway,
        rest: RestClient,
        i18nContext: I18nContext,
        winners: MutableList<String>
    ) {
        val guildId = Snowflake(giveaway.guildId)

        if (giveaway.awardRoleIds?.isNotEmpty() == true) {
            winners.map {
                rest.guild.getGuildMember(
                    guildId,
                    Snowflake(it)
                )
            }.forEach { member ->
                val rolesToBeGiven = member.roles.filter {
                    !giveaway.awardRoleIds!!.contains(it.asString)
                }

                if (rolesToBeGiven.isNotEmpty()) rolesToBeGiven.forEach {
                    rest.guild.addRoleToGuildMember(
                        guildId,
                        // TODO: FIX THIS
                        member.user.value?.id!!,
                        it,
                        i18nContext.get(I18N_PREFIX.ReasonToAddRole)
                    )
                }
            }
        }

        // if (giveaway.awardSonhosPerUser != null) { }
    }

    private suspend fun sendMessageInGiveawayChannel(
        giveaway: PuddingGiveaway,
        rest: RestClient,
        builder: UserMessageCreateBuilder.() -> Unit
    ) {
        try {
            rest.channel.createMessage(
                Snowflake(giveaway.channelId)
            ) {
                apply(builder)
            }
        } catch (e: KtorRequestException) {
            if (!giveaway.finished)
                giveaway.finishGiveaway()
        }
    }
}