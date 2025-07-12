package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
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
import net.perfectdreams.loritta.morenitta.utils.RankPaginationUtils
import net.perfectdreams.loritta.morenitta.utils.RankingGenerator
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import kotlin.math.ceil

class LoriCoolCardsRankExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Album
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
        val page = optionalLong("page", I18N_PREFIX.Options.Page.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val albumId = args[options.album].toLong()
        val page = (args[options.page]?.minus(1)?.coerceAtLeast(0)) ?: 0

        context.reply(false) {
            createRankMessage(context, albumId, page)()
        }
    }

    private suspend fun createRankMessage(
        context: UnleashedContext,
        eventId: Long,
        page: Long
    ): suspend InlineMessage<*>.() -> (Unit) = {
        val now = Instant.now()

        val result = loritta.transaction {
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.id eq eventId and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction ViewAlbumRankResult.AlbumDoesNotExist

            val totalCount = LoriCoolCardsFinishedAlbumUsers
                .select(LoriCoolCardsFinishedAlbumUsers.user, LoriCoolCardsFinishedAlbumUsers.finishedAt)
                .where {
                    LoriCoolCardsFinishedAlbumUsers.event eq event[LoriCoolCardsEvents.id]
                }
                .orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt to SortOrder.ASC)
                .count()

            val profilesInTheQuery = LoriCoolCardsFinishedAlbumUsers
                .select(LoriCoolCardsFinishedAlbumUsers.user, LoriCoolCardsFinishedAlbumUsers.finishedAt)
                .where {
                    LoriCoolCardsFinishedAlbumUsers.event eq event[LoriCoolCardsEvents.id]
                }
                .orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt to SortOrder.ASC)
                .limit(5)
                .offset(page * 5)
                .toList()

            return@transaction ViewAlbumRankResult.Success(
                event,
                totalCount,
                profilesInTheQuery
            )
        }

        when (result) {
            ViewAlbumRankResult.AlbumDoesNotExist -> {
                context.reply(false) {
                    styled(
                        "O álbum de figurinhas que você selecionou não existe!"
                    )
                }
            }
            is ViewAlbumRankResult.Success -> {
                // Calculates the max page
                val maxPage = ceil(result.totalCount / 5.0)

                RankPaginationUtils.createRankMessage(
                    loritta,
                    context,
                    page,
                    maxPage.toInt(),
                    RankingGenerator.generateRanking(
                        loritta,
                        page * 5,
                        result.eventData[LoriCoolCardsEvents.eventName],
                        null,
                        result.users.map {
                            RankingGenerator.UserRankInformation(
                                it[LoriCoolCardsFinishedAlbumUsers.user],
                                DateUtils.formatDateDiff(context.i18nContext, result.eventData[LoriCoolCardsEvents.startsAt], it[LoriCoolCardsFinishedAlbumUsers.finishedAt])
                            )
                        }
                    ) {
                        null
                    }
                ) {
                    createRankMessage(context, eventId, it)
                }.invoke(this)
            }
        }
    }

    sealed class ViewAlbumRankResult {
        data object AlbumDoesNotExist : ViewAlbumRankResult()
        class Success(
            val eventData: ResultRow,
            val totalCount: Long,
            val users: List<ResultRow>
        ) : ViewAlbumRankResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        if (args.isEmpty()) {
            context.explain()
            return null
        }

        val eventId = loritta.transaction {
            LoriCoolCardsEvents
                .selectAll()
                .orderBy(LoriCoolCardsEvents.startsAt to SortOrder.DESC)
                .firstOrNull()
                ?.get(LoriCoolCardsEvents.id) ?: -1
        }

        return mapOf(
            options.album to eventId.toString(),
            options.page to args[0].toLongOrNull()
        )
    }
}