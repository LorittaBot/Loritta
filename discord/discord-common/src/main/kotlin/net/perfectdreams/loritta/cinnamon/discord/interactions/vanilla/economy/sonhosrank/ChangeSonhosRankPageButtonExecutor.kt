package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.discord.utils.RankingGenerator
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class ChangeSonhosRankPageButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_SONHOS_RANK_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ChangeSonhosRankPageData>()

        // Loading Section
        val loadingEmoji = LoadingEmojis.random()
        context.updateMessage {
            styled(
                context.i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                loadingEmoji
            )

            actionRow {
                disabledButton(ButtonStyle.Primary) {
                    loriEmoji = if (data.button == ChangeSonhosRankPageData.Button.LEFT_ARROW) loadingEmoji else Emotes.ChevronLeft
                }

                disabledButton(ButtonStyle.Primary) {
                    loriEmoji = if (data.button == ChangeSonhosRankPageData.Button.RIGHT_ARROW) loadingEmoji else Emotes.ChevronRight
                }
            }
        }

        val message = if (data.rankType == SonhosRankType.LOCAL) {
            if (context !is GuildComponentContext)
                error("Trying to look up a local sonhos rank outside of a guild!")

            SonhosRankExecutor.createMessageLocal(loritta, context, loritta.kord.getGuild(context.guildId)!!, data.page)
        } else {
            SonhosRankExecutor.createMessageGlobal(loritta, context, data.page)
        }

        context.updateMessage {
            message()
        }
    }
}