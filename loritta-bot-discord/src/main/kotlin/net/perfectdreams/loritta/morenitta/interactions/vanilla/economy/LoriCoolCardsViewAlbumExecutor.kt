package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant

class LoriCoolCardsViewAlbumExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor() {
    inner class Options : ApplicationCommandOptions() {
        val page = optionalLong("page", TodoFixThisData)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val pageLookup = args[options.page]?.toInt() ?: 1

        viewAlbum(
            context,
            pageLookup,
        ) {
            context.reply(false) {
                it.invoke(this)
            }
        }
    }

    suspend fun viewAlbum(
        context: UnleashedContext,
        pageLookup: Int,
        targetAlbumEdit: suspend (InlineMessage<*>.() -> (Unit)) -> (Unit)
    ) {
        val now = Instant.now()

        val result = loritta.transaction {
            val event = LoriCoolCardsEvents.select {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction ViewAlbumResult.EventUnavailable

            // First we need to check all cards that we have already sticked
            // We will inner join because we need that info when generating the album
            val alreadyStickedCards = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
            }.toList()

            // The stickers will be sticked when the user clicks to stick the sticker
            return@transaction ViewAlbumResult.Success(Json.decodeFromString(event[LoriCoolCardsEvents.template]), alreadyStickedCards)
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
                    content = "Páginas ${pageCombo.pageLeft} e ${pageCombo.pageRight}"
                    files += FileUpload.fromData(album, "album.png").setDescription("Página ${pageCombo.pageLeft} e ${pageCombo.pageRight} do Álbum de Figurinhas de ${context.user.name}")

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

                                viewAlbum(it, 1) {
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

                                viewAlbum(it, pageLookup - 2) {
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

                                viewAlbum(it, pageLookup + 2) {
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

                                viewAlbum(it, template.pages.last().pageRight) {
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
        class Success(val template: StickerAlbumTemplate, val alreadyStickedCards: List<ResultRow>) : ViewAlbumResult()
    }
}