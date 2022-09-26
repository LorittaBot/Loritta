package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class UnfollowPackageButtonClickExecutor(
    loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.UNFOLLOW_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<UnfollowPackageData>()

        loritta.pudding.packagesTracking.untrackCorreiosPackage(
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