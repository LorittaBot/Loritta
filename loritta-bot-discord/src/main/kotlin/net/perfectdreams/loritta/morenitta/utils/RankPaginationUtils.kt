package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.awt.image.BufferedImage

/**
 * Utilities related to rank pagination
 */
object RankPaginationUtils {
    /**
     * Creates a [InlineMessage] with the rank image and the buttons to navigate between pages
     */
    fun createRankMessage(
        loritta: LorittaBot,
        context: UnleashedContext,
        page: Long,
        maxPage: Int,
        rankingImage: BufferedImage,
        onNewPage: suspend (Long) -> (suspend InlineMessage<*>.() -> (Unit))
    ): suspend InlineMessage<*>.() -> (Unit) = {
        styled(
            context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.Page(page + 1)),
            Emotes.LoriReading
        )

        val maxPageZeroIndexed = maxPage - 1

        this.files += FileUpload.fromData(
            rankingImage.toByteArray(ImageFormatType.PNG).inputStream(),
            "rank.png"
        )

        actionRow(
            loritta.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                builder = {
                    loriEmoji = Emotes.ChevronLeft
                    disabled = page !in RankingGenerator.VALID_RANKING_PAGES
                }
            ) {
                val hook = it.updateMessageSetLoadingState()

                val builtMessage = onNewPage.invoke(page - 1)

                val asMessageEditData = MessageEdit {
                    builtMessage()
                }

                hook.editOriginal(asMessageEditData).await()
            },
            loritta.interactivityManager.buttonForUser(
                context.user,
                context.alwaysEphemeral,
                ButtonStyle.PRIMARY,
                builder = {
                    loriEmoji = Emotes.ChevronRight
                    disabled = page + 2 !in RankingGenerator.VALID_RANKING_PAGES || page >= maxPageZeroIndexed
                }
            ) {
                val hook = it.updateMessageSetLoadingState()

                val builtMessage = onNewPage.invoke(page + 1)

                val asMessageEditData = MessageEdit {
                    builtMessage()
                }

                hook.editOriginal(asMessageEditData).await()
            }
        )
    }
}