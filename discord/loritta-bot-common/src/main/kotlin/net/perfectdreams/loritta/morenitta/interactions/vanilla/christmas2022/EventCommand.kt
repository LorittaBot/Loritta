package net.perfectdreams.loritta.morenitta.interactions.vanilla.christmas2022

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.XpCommand
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.math.ceil

class EventCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    private val I18N_PREFIX = I18nKeysData.Commands.Command.Christmas2022event

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.FUN) {
        subcommand(I18N_PREFIX.Join.Label, I18N_PREFIX.Description) {
            executor = JoinEventExecutor()
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description) {
            executor = StatsEventExecutor()
        }

        subcommand(I18N_PREFIX.Rank.Label, I18N_PREFIX.Rank.Description) {
            executor = StatsRankExecutor()
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
                        "Pelo o que eu vi, os presentes ${LorittaChristmas2022Event.emoji.asMention} estão espalhados pelo chat, aparecendo como reações nas conversas.",
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
                        "Boa caça ao tesou... Quer dizer, caça aos presentes!",
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
                    CollectedChristmas2022Points.user eq context.user.idLong and (CollectedChristmas2022Points.valid eq true)
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
                                buildString {
                                    append("**[${reward.requiredPoints} presentes]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Badge para o seu ${loritta.commandMentions.profileView}")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaChristmas2022Event.EventReward.SonhosReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} presentes]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("${reward.sonhos} sonhos")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaChristmas2022Event.EventReward.PremiumKeyReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} presentes]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Todas as vantagens premiums (R\$ 99,99) por três meses")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }

                        is LorittaChristmas2022Event.EventReward.ProfileDesignReward -> {
                            styled(
                                buildString {
                                    append("**[${reward.requiredPoints} presentes]")
                                    if (reward.prestige) {
                                        append(" (Prestígio \uD83D\uDD25)")
                                    }
                                    append("** ")
                                    append("Design de Perfil ${context.locale["profileDesigns.${reward.profileName}.title"]}")
                                },
                                prefix = if (collectedPoints >= reward.requiredPoints) "✅" else "❌"
                            )
                        }
                    }
                }

                styled("*Você precisa de um servidor com membros participando do evento, para que você encontre mais presentes? Então entre na Comunidade da Loritta! https://discord.gg/lori *")
            }
        }
    }

    inner class StatsRankExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", XpCommand.XP_RANK_I18N_PREFIX.Options.Page.Text) /* {
                // range = RankingGenerator.VALID_RANKING_PAGES
            } */
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            val page = (args[options.page]?.minus(1)) ?: 0

            context.deferChannelMessage(false)

            context.reply(false) {
                createRankMessage(context, page)()
            }
        }

        private suspend fun createRankMessage(context: InteractionContext, page: Long): suspend InlineMessage<*>.() -> (Unit) = {
            styled(
                context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
                Emotes.LoriReading
            )

            val countColumn = CollectedChristmas2022Points.points.count()

            val (totalCount, profiles) = loritta.pudding.transaction {
                val totalCount = CollectedChristmas2022Points.slice(CollectedChristmas2022Points.user, countColumn)
                    .select { CollectedChristmas2022Points.valid eq true }
                    .groupBy(CollectedChristmas2022Points.user)
                    .count()

                val profilesInTheQuery =
                    CollectedChristmas2022Points.slice(CollectedChristmas2022Points.user, countColumn)
                        .select { CollectedChristmas2022Points.valid eq true }
                        .groupBy(CollectedChristmas2022Points.user)
                        .orderBy(countColumn to SortOrder.DESC)
                        .limit(5, page * 5)
                        .toList()

                Pair(totalCount, profilesInTheQuery)
            }

            // Calculates the max page
            val maxPage = ceil(totalCount / 5.0)
            val maxPageZeroIndexed = maxPage - 1

            files += FileUpload.fromData(
                RankingGenerator.generateRanking(
                    loritta,
                    page * 5,
                    "Evento de Natal",
                    null,
                    profiles.map {
                        val presentesCount = it[countColumn]

                        RankingGenerator.UserRankInformationX(
                            it[CollectedChristmas2022Points.user].value,
                            "$presentesCount presentes"
                        )
                    }
                ) {
                    null
                }.toByteArray(ImageFormatType.PNG).inputStream(),
                "rank.png"
            )

            actionRow(
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    builder = {
                        loriEmoji = Emotes.ChevronLeft
                        disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                    }
                ) {
                    it.deferEdit()
                        .editOriginal(
                            InlineMessage(MessageEditBuilder())
                                .apply {
                                    createRankMessage(
                                        context,
                                        page - 1
                                    )()
                                }.build()
                        )
                        .await()
                },
                loritta.interactivityManager.buttonForUser(
                    context.user,
                    ButtonStyle.PRIMARY,
                    builder = {
                        loriEmoji = Emotes.ChevronRight
                        disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                    }
                ) {
                    it.deferEdit()
                        .editOriginal(
                            InlineMessage(MessageEditBuilder())
                                .apply {
                                    createRankMessage(
                                        context,
                                        page + 1
                                    )()
                                }.build()
                        )
                        .await()
                },
            )
        }
    }
}