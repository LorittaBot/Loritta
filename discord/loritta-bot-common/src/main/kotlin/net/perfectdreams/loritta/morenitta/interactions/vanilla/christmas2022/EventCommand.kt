package net.perfectdreams.loritta.morenitta.interactions.vanilla.christmas2022

import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.Timestamp
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

class EventCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Christmas2022event

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        subcommand(I18N_PREFIX.Join.Label, I18N_PREFIX.Description) {
            executor = JoinEventExecutor()
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description) {
            executor = StatsEventExecutor()
        }
    }

    inner class JoinEventExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            if (!LorittaChristmas2022Event.isEventActive()) {
                context.reply(true) {
                    styled(
                        "Infelizmente o evento já acabou...",
                        Emotes.LoriSob
                    )
                }
                return
            }

            val alreadyJoined = loritta.newSuspendedTransaction {
                val alreadyJoined = Christmas2022Players.select {
                    Christmas2022Players.id eq context.user.idLong
                }.count() != 0L

                if (!alreadyJoined) {
                    Christmas2022Players.insert {
                        it[Christmas2022Players.id] = context.user.idLong
                        it[Christmas2022Players.joinedAt] = Instant.now()
                    }
                }

                alreadyJoined
            }

            if (alreadyJoined) {
                context.reply(true) {
                    styled(
                        "Você já está participando do evento!",
                        Emotes.LoriShrug
                    )
                }
                return
            } else {
                context.reply(true) {
                    styled(
                        "Você está participando do evento! Obrigada a ajudar a Loritta a encontrar os presentes de Natal para ela!",
                        Emotes.LoriHi
                    )

                    styled(
                        "Pelo o que eu vi, os presentes ${LorittaChristmas2022Event.emoji.asMention} estão espalhandos pelo chat, aparecendo como reações nas conversas.",
                        Emotes.LoriHm
                    )

                    styled(
                        "Ao encontrar um presente ${LorittaChristmas2022Event.emoji.asMention}, reaja nele para coletá-lo. Mas seja rápido, pois os presentes expiram! Por que eles expiram? Porque a Loritta falou \"nossa, como eu amo Pudim!!!\" e por isso o Presente de Natal dela são pudins.",
                        Emotes.LoriWow
                    )

                    styled(
                        "Como a Loritta é estrelinha, os presentes só aparecem em servidores que possuem mais de mil membros!",
                        Emotes.LoriHmpf
                    )

                    styled(
                        "Você pode ver todas as recompensas do evento e quantos presentes você já coletou usando ${loritta.commandMentions.eventStats}!",
                        Emotes.LoriRich
                    )

                    styled(
                        "Boa caça ao tesouro!",
                        Emotes.LoriHeart
                    )
                }
                return
            }
        }
    }

    inner class StatsEventExecutor : LorittaSlashCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)

            val (joined, collectedPoints) = loritta.newSuspendedTransaction {
                val joined = Christmas2022Players.select {
                    Christmas2022Players.id eq context.user.idLong
                }.count() != 0L

                val count = CollectedChristmas2022Points.select {
                    CollectedChristmas2022Points.user eq context.user.idLong
                }.count()

                Pair(joined, count)
            }

            if (!joined) {
                context.reply(true) {
                    styled("Você precisa entrar no evento antes de poder ver as suas estatísticas!")
                }
                return
            }

            context.reply(true) {
                styled("Você pegou **$collectedPoints presentes ${LorittaChristmas2022Event.emoji.asMention}**!")

                styled("O evento irá acabar às ${TimeFormat.DATE_TIME_LONG.format(LorittaChristmas2022Event.endOfEvent)}!")
                styled("Lembre-se que presentes apenas aparecem em servidores que possuem mais de mil membros!")

                for (reward in LorittaChristmas2022Event.eventRewards.sortedBy { it.requiredPoints }) {
                    when (reward) {
                        is LorittaChristmas2022Event.EventReward.BadgeReward -> {
                            styled(
                                "**[${reward.requiredPoints} presentes]** Badge para o seu ${loritta.commandMentions.profileView}",
                                prefix = if (collectedPoints >= reward.requiredPoints) Emotes.LoriYay else Emotes.LoriHm
                            )
                        }

                        is LorittaChristmas2022Event.EventReward.SonhosReward -> {
                            styled(
                                "**[${reward.requiredPoints} presentes]** ${reward.sonhos} sonhos",
                                prefix = if (collectedPoints >= reward.requiredPoints) Emotes.LoriYay else Emotes.LoriHm
                            )
                        }

                        is LorittaChristmas2022Event.EventReward.PremiumKeyReward -> {
                            styled(
                                "**[${reward.requiredPoints} presentes]** Vantagens premium por um mês",
                                prefix = if (collectedPoints >= reward.requiredPoints) Emotes.LoriYay else Emotes.LoriHm
                            )
                        }
                    }
                }
            }
        }
    }
}