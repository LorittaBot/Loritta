package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class GoBackToPackageListButtonClickExecutor(
    loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.GO_BACK_TO_PACKAGE_LIST_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()
        context.decodeDataFromComponentAndRequireUserToMatch<BarebonesSingleUserComponentData>()

        val packageIds = context.loritta.services.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))

        if (packageIds.isEmpty())
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.List.YouAreNotFollowingAnyPackage),
                Emotes.LoriSob
            )

        val message = PackageListExecutor.createMessage(context.i18nContext, packageIds)

        context.updateMessage {
            message()
        }
    }
}