package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class GoBackToPackageListButtonClickExecutor(
    val loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(
        GoBackToPackageListButtonClickExecutor::class,
        ComponentExecutorIds.GO_BACK_TO_PACKAGE_LIST_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        context.deferUpdateMessage()
        context.decodeViaComponentDataUtilsAndRequireUserToMatch<BarebonesSingleUserComponentData>(data)

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