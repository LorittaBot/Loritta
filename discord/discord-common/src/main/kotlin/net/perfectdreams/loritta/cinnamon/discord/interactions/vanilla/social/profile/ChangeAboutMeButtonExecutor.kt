package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.modals.components.textInput
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class ChangeAboutMeButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_ABOUT_ME_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ChangeAboutMeButtonData>()

        // Because we are storing the interaction token, we will store it on the database
        val encodedData = loritta.encodeDataForComponentOnDatabase(
            ChangeAboutMeModalData(context.interaKTionsContext.discordInteraction.token)
        ).data

        context.sendModal(
            ChangeAboutMeModalExecutor,
            encodedData,
            context.i18nContext.get(I18nKeysData.Commands.Command.Profileview.ChangeAboutMe)
        ) {
            actionRow {
                textInput(ChangeAboutMeModalExecutor.options.aboutMe, context.i18nContext.get(I18nKeysData.Profiles.AboutMe)) {
                    this.value = data.currentAboutMe
                }
            }
        }
    }
}