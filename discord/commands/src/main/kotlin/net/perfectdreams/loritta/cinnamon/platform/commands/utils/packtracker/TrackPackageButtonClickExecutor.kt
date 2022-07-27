package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.exceptions.InvalidTrackingIdException
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class TrackPackageButtonClickExecutor(
    loritta: LorittaCinnamon,
    val client: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.TRACK_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()
        val (userId, trackingId) = context.decodeDataFromComponentAndRequireUserToMatch<TrackPackageData>()

        val correiosResponse = try {
            client.getPackageInfo(trackingId)
        } catch (e: InvalidTrackingIdException) {
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.InvalidCorreiosTrackingId("AA123456785BR")),
                Emotes.LoriSob
            )
        }

        val pack = correiosResponse.objeto.firstOrNull()

        if (pack is CorreiosUnknownObjeto)
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.ObjectNotFoundCorreios),
                Emotes.LoriSob
            )

        if (pack is CorreiosFoundObjeto) {
            val message = TrackPackageExecutor.createMessage(
                context.i18nContext,
                context.user.id,
                trackingId,
                pack,
                trackingId in context.loritta.services.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))
            )

            context.updateMessage {
                message()
            }
        }
    }
}