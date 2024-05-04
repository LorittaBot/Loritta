package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.MarkdownUtil
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rank
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant

class LoriCoolCardsViewAlbumExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor() {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Album
    }

    inner class Options : ApplicationCommandOptions() {
        val user = optionalUser("user", I18N_PREFIX.Options.User.Text)
        val page = optionalLong("page", I18N_PREFIX.Options.Page.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val pageLookup = args[options.page]?.toInt() ?: 1
        val user = args[options.user]?.user ?: context.user

        viewAlbum(
            context,
            pageLookup,
            user
        ) {
            context.reply(false) {
                it.invoke(this)
            }
        }
    }

    suspend fun viewAlbum(
        context: UnleashedContext,
        pageLookup: Int,
        userToBeViewed: User,
        targetAlbumEdit: suspend (InlineMessage<*>.() -> (Unit)) -> (Unit)
    ) {
        val now = Instant.now()

        val result = loritta.transaction {
            val event = LoriCoolCardsEvents.select {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction ViewAlbumResult.EventUnavailable

            // First we need to check all cards that we have already sticked
            // We will inner join because we need that info when generating the album
            val totalStickers = LoriCoolCardsEventCards.select {
                (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
            }.count()

            // First we need to check all cards that we have already sticked
            // We will inner join because we need that info when generating the album
            val alreadyStickedCards = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.user eq userToBeViewed.idLong)
            }.toList()

            // Have we finished the album? If yes, in what position are we in?
            val rankOverField = rank().over().orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt, SortOrder.ASC)
            val albumRank = LoriCoolCardsFinishedAlbumUsers.select(
                LoriCoolCardsFinishedAlbumUsers.user,
                LoriCoolCardsFinishedAlbumUsers.finishedAt,
                rankOverField
            ).where {
                // We cannot filter by user here, if we do an "eq userToBeViewed.idLong" here, the rank position will always be 1 (or null, if the user hasn't completed the album)
                // So we filter it after the fact
                LoriCoolCardsFinishedAlbumUsers.event eq event[LoriCoolCardsEvents.id]
            }.firstOrNull { it[LoriCoolCardsFinishedAlbumUsers.user] == userToBeViewed.idLong }

            // The stickers will be sticked when the user clicks to stick the sticker
            return@transaction ViewAlbumResult.Success(
                Json.decodeFromString(event[LoriCoolCardsEvents.template]),
                alreadyStickedCards,
                totalStickers,
                albumRank?.let {
                    ViewAlbumResult.Success.FinishedAlbumResult(
                        it[rankOverField],
                        it[LoriCoolCardsFinishedAlbumUsers.finishedAt]
                    )
                }

            )
        }

        when (result) {
            ViewAlbumResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is ViewAlbumResult.Success -> {
                val template = result.template
                val pageCombo = template.getAlbumComboPageByPage(pageLookup)
                if (pageCombo == null) {
                    context.reply(false) {
                        styled(
                            "Página desconhecida!"
                        )
                    }
                    return
                }

                val leftButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronLeft
                )

                val rightButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronRight
                )

                val jumpToFirstButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronSuperLeft
                )

                val jumpToLastButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronSuperRight
                )

                // Get the page that we want to render
                val album = loritta.loriCoolCardsManager.generateAlbumPreview(template, result.alreadyStickedCards, pageCombo)
                targetAlbumEdit.invoke {
                    styled(
                        MarkdownUtil.bold(
                            context.i18nContext.get(
                                if (context.user == userToBeViewed)
                                    I18N_PREFIX.YourAlbum
                                else
                                    I18N_PREFIX.UserAlbum(userToBeViewed.asMention)
                            )
                        ),
                        Emotes.LoriLurk
                    )

                    styled(
                        context.i18nContext.get(I18N_PREFIX.AlbumPages(pageCombo.pageLeft, pageCombo.pageRight)),
                        Emotes.LoriCoolSticker
                    )

                    styled(
                        context.i18nContext.get(I18N_PREFIX.StickedStickers(result.alreadyStickedCards.size, result.totalStickers)),
                        Emotes.LoriHanglooseRight
                    )

                    if (result.finishedStats != null) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.FinishedAlbumStats(user = userToBeViewed.asMention, finishedPosition = result.finishedStats.finishedRank, date = DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(result.finishedStats.finishedAt))),
                            Emotes.Sparkles
                        )
                    }

                    files += FileUpload.fromData(album, "album.png")
                        .setDescription("Página ${pageCombo.pageLeft} e ${pageCombo.pageRight} do Álbum de Figurinhas de ${context.user.name}")

                    actionRow(
                        if (pageCombo.pageLeft != 1) {
                            loritta.interactivityManager.buttonForUser(
                                context.user.idLong,
                                jumpToFirstButton
                            ) {
                                it.invalidateComponentCallback()

                                val editJob = it.event.editMessage(
                                    MessageEdit {
                                        actionRow(
                                            jumpToFirstButton.asDisabled()
                                                .withEmoji(LoadingEmojis.random().toJDA())
                                                .asDisabled(),
                                            leftButton.asDisabled(),
                                            rightButton.asDisabled(),
                                            jumpToLastButton.asDisabled(),
                                        )
                                    }
                                ).submit()

                                val hook = it.event.hook

                                viewAlbum(it, 1, userToBeViewed) {
                                    editJob.await()

                                    hook.editOriginal(
                                        MessageEdit {
                                            it.invoke(this)
                                        }
                                    ).await()
                                }
                            }
                        } else jumpToFirstButton.asDisabled(),
                        if (template.getAlbumComboPageByPage(pageLookup - 2) != null) {
                            loritta.interactivityManager.buttonForUser(
                                context.user.idLong,
                                leftButton
                            ) {
                                it.invalidateComponentCallback()

                                val editJob = it.event.editMessage(
                                    MessageEdit {
                                        actionRow(
                                            jumpToFirstButton.asDisabled(),
                                            leftButton
                                                .withEmoji(LoadingEmojis.random().toJDA())
                                                .asDisabled(),
                                            rightButton.asDisabled(),
                                            jumpToLastButton.asDisabled(),
                                        )
                                    }
                                ).submit()

                                val hook = it.event.hook

                                viewAlbum(it, pageLookup - 2, userToBeViewed) {
                                    editJob.await()

                                    hook.editOriginal(
                                        MessageEdit {
                                            it.invoke(this)
                                        }
                                    ).await()
                                }
                            }
                        } else leftButton.asDisabled(),
                        if (template.getAlbumComboPageByPage(pageLookup + 2) != null) {
                            loritta.interactivityManager.buttonForUser(
                                context.user.idLong,
                                rightButton
                            ) {
                                it.invalidateComponentCallback()

                                val editJob = it.event.editMessage(
                                    MessageEdit {
                                        actionRow(
                                            jumpToFirstButton.asDisabled(),
                                            leftButton.asDisabled(),
                                            rightButton
                                                .withEmoji(LoadingEmojis.random().toJDA())
                                                .asDisabled(),
                                            jumpToLastButton.asDisabled()
                                        )
                                    }
                                ).submit()

                                val hook = it.event.hook

                                viewAlbum(it, pageLookup + 2, userToBeViewed) {
                                    editJob.await()

                                    hook.editOriginal(
                                        MessageEdit {
                                            it.invoke(this)
                                        }
                                    ).await()
                                }
                            }
                        } else rightButton.asDisabled(),
                        if (pageCombo.pageRight != template.pages.last().pageRight) {
                            loritta.interactivityManager.buttonForUser(
                                context.user.idLong,
                                jumpToLastButton
                            ) {
                                it.invalidateComponentCallback()

                                val editJob = it.event.editMessage(
                                    MessageEdit {
                                        actionRow(
                                            jumpToFirstButton.asDisabled(),
                                            leftButton.asDisabled(),
                                            rightButton.asDisabled(),
                                            jumpToLastButton.asDisabled()
                                        )
                                    }
                                ).submit()

                                val hook = it.event.hook

                                viewAlbum(it, template.pages.last().pageRight, userToBeViewed) {
                                    editJob.await()

                                    hook.editOriginal(
                                        MessageEdit {
                                            it.invoke(this)
                                        }
                                    ).await()
                                }
                            }
                        } else jumpToLastButton.asDisabled(),
                    )
                }
            }
        }
    }

    sealed class ViewAlbumResult {
        data object EventUnavailable : ViewAlbumResult()
        class Success(
            val template: StickerAlbumTemplate,
            val alreadyStickedCards: List<ResultRow>,
            val totalStickers: Long,
            val finishedStats: FinishedAlbumResult?
        ) : ViewAlbumResult() {
            data class FinishedAlbumResult(
                val finishedRank: Long,
                val finishedAt: Instant
            )
        }
    }
}