package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank.ChangeSonhosRankPageData
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class ChangeXpRankPageButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_XP_RANK_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext)
            return

        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ChangeXpRankPageData>()

        // Loading Section
        val loadingEmoji = LoadingEmojis.random()
        context.updateMessage {
            styled(
                context.i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                loadingEmoji
            )

            actionRow {
                disabledButton(ButtonStyle.Primary) {
                    loriEmoji = if (data.button == ChangeXpRankPageData.Button.LEFT_ARROW) loadingEmoji else Emotes.ChevronLeft
                }

                disabledButton(ButtonStyle.Primary) {
                    loriEmoji = if (data.button == ChangeXpRankPageData.Button.RIGHT_ARROW) loadingEmoji else Emotes.ChevronRight
                }
            }
        }

        // TODO: Cache this somewhere
        val guild = loritta.kord.getGuild(context.guildId)!!

        val message = XpRankExecutor.createMessage(loritta, context, guild, data.page)

        context.updateMessage {
            message()
        }
    }
}