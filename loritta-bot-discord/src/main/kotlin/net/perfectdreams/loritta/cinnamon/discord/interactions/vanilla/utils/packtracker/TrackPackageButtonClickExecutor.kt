package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.exceptions.InvalidTrackingIdException
import net.perfectdreams.loritta.serializable.UserId

class TrackPackageButtonClickExecutor(
    loritta: LorittaBot,
    val client: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.TRACK_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()
        val (userId, trackingId) = context.decodeDataFromComponentAndRequireUserToMatch<TrackPackageData>()

        val objects = try {
            client.getPackageInfo(trackingId)
        } catch (e: InvalidTrackingIdException) {
            context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.InvalidCorreiosTrackingId("AA123456785BR")),
                Emotes.LoriSob
            )
        }

        val pack = objects.firstOrNull()

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
                trackingId in context.loritta.pudding.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))
            )

            context.updateMessage {
                message()
            }
        }
    }
}