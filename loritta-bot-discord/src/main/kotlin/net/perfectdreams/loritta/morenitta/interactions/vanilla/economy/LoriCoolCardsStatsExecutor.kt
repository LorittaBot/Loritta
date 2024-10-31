package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserBoughtBoosterPacks
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import org.jetbrains.exposed.sql.*
import java.time.Instant

class LoriCoolCardsStatsExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Stats
    }

    inner class Options : ApplicationCommandOptions() {
        val album = string("album", I18N_PREFIX.Options.Album.Text) {
            autocomplete { context ->
                val now = Instant.now()

                // Autocomplete all albums
                val activeAlbums = loritta.transaction {
                    LoriCoolCardsEvents.select(LoriCoolCardsEvents.id, LoriCoolCardsEvents.eventName)
                        .where { LoriCoolCardsEvents.startsAt lessEq now }
                        .orderBy(LoriCoolCardsEvents.endsAt, SortOrder.DESC)
                        .toList()
                }

                activeAlbums
                    .filter { it[LoriCoolCardsEvents.eventName].startsWith(context.event.focusedOption.value, true) }
                    .take(25)
                    .associate {
                        it[LoriCoolCardsEvents.eventName] to it[LoriCoolCardsEvents.id].value.toString()
                    }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)
        val albumId = args[options.album].toLong()

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        // Load the selected event
        val result = loritta.transaction {
            // First we will get the active cards event to get the album template
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.id eq albumId and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction EventStatsResult.AlbumDoesNotExist

            val userCountDistinctField = LoriCoolCardsUserOwnedCards.user.countDistinct()
            val totalUsersParticipating = LoriCoolCardsUserOwnedCards.slice(userCountDistinctField).select {
                LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]
            }.first()[userCountDistinctField]

            val stickersInCirculation = LoriCoolCardsUserOwnedCards.select {
                LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.sticked eq false)
            }.count()

            val stickedStickers = LoriCoolCardsUserOwnedCards.select {
                LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.sticked eq true)
            }.count()

            val albumsFinished = LoriCoolCardsFinishedAlbumUsers.select {
                LoriCoolCardsFinishedAlbumUsers.event eq event[LoriCoolCardsEvents.id]
            }.count()

            val totalBoosterPacksBought = LoriCoolCardsUserBoughtBoosterPacks.select {
                LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id]
            }.count()

            val userBoosterPacksBought = LoriCoolCardsUserBoughtBoosterPacks.select {
                LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserBoughtBoosterPacks.user eq context.user.idLong)
            }.count()

            EventStatsResult.Success(
                event[LoriCoolCardsEvents.eventName],
                event[LoriCoolCardsEvents.startsAt],
                event[LoriCoolCardsEvents.endsAt],
                totalUsersParticipating,
                stickersInCirculation,
                stickedStickers,
                albumsFinished,
                totalBoosterPacksBought,
                userBoosterPacksBought
            )
        }

        when (result) {
            EventStatsResult.AlbumDoesNotExist -> {
                context.reply(false) {
                    styled(
                        "O álbum de figurinhas que você selecionou não existe!"
                    )
                }
            }
            is EventStatsResult.Success -> {
                context.reply(false) {
                    embed {
                        title = "${Emotes.LoriCoolSticker} Álbum de Figurinhas - ${result.eventName}"
                        color = LorittaColors.LorittaAqua.rgb

                        field {
                            name = "Evento começou em"
                            value = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.startedAt)
                            inline = false
                        }

                        field {
                            name = "Evento acaba em"
                            value = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.endsAt)
                            inline = false
                        }

                        field {
                            name = "Usuários participando"
                            value = "${result.totalUsersParticipating} usuários"
                            inline = false
                        }

                        field {
                            name = "Figurinhas em circulação"
                            value = "${result.stickersInCirculation} figurinhas"
                            inline = false
                        }

                        field {
                            name = "Figurinhas coladas em Álbuns"
                            value = "${result.stickedStickers} figurinhas"
                            inline = false
                        }

                        field {
                            name = "Total de Figurinhas"
                            value = "${result.stickersInCirculation + result.stickedStickers} figurinhas"
                            inline = false
                        }

                        field {
                            name = "Álbuns finalizados"
                            value = "${result.albumsFinished} álbuns"
                            inline = false
                        }

                        field {
                            name = "Total de Pacotes de Figurinhas que todos compraram"
                            value = "${result.totalBoosterPacksBought} pacotes"
                            inline = false
                        }

                        field {
                            name = "Total de Pacotes de Figurinhas que você comprou"
                            value = "${result.userBoosterPacksBought} pacotes"
                            inline = false
                        }
                        /* description = buildString {
                            appendLine("## Sobre o Evento")
                            appendLine("**Evento começou em:** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.startedAt)}")
                            appendLine("**Evento acaba em:** ${DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.endsAt)}")
                            appendLine("**Usuários participando:** ${result.totalUsersParticipating} usuários")
                            appendLine("**Figurinhas em circulação:** ${result.stickersInCirculation} figurinhas")
                            appendLine("**Figurinhas coladas em Álbuns:** ${result.stickedStickers} figurinhas")
                            appendLine("**Total de Figurinhas:** ${result.stickersInCirculation + result.stickedStickers} figurinhas")
                            appendLine("**Álbuns finalizados:** ${result.albumsFinished} álbuns")

                            appendLine("## Sobre Você")
                            appendLine("**Pacotinhos de Figurinhas Comprados:** ${result.userBoosterPacksBought} pacotes")
                        } */

                        image = "https://stuff.loritta.website/loricoolcards/figurittas-da-loritta-logo.png" // TODO: logo
                    }
                }
            }
        }
    }

    sealed class EventStatsResult {
        data object AlbumDoesNotExist : EventStatsResult()
        class Success(
            val eventName: String,
            val startedAt: Instant,
            val endsAt: Instant,
            val totalUsersParticipating: Long,
            val stickersInCirculation: Long,
            val stickedStickers: Long,
            val albumsFinished: Long,
            val totalBoosterPacksBought: Long,
            val userBoosterPacksBought: Long
        ) : EventStatsResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        return mapOf()
    }
}