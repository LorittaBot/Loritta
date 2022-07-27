package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.*
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class UnfollowPackageButtonClickExecutor(
    loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.UNFOLLOW_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<UnfollowPackageData>()

        loritta.services.packagesTracking.untrackCorreiosPackage(
            UserId(user.id.value),
            decoded.trackingId
        )

        context.updateMessage {
            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackageUpdates),
                    FollowPackageButtonClickExecutor,
                    ComponentDataUtils.encode(
                        FollowPackageData(
                            context.user.id,
                            decoded.trackingId
                        )
                    )
                )
            }
        }

        context.sendEphemeralReply(
            context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.UnfollowPackage.YouUnfollowedThePackage(decoded.trackingId)),
            Emotes.LoriSunglasses
        )
    }
}