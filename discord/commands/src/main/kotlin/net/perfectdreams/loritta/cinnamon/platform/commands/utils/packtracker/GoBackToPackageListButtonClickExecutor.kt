package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
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