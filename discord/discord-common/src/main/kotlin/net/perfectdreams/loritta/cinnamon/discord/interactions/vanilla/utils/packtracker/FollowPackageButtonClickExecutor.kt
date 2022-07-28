package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.PackagesTrackingService

class FollowPackageButtonClickExecutor(
    loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.FOLLOW_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<FollowPackageData>()

        // Check if the package is already delivered and, if it is, don't allow the user to track it!
        val correiosResponse = correios.getPackageInfo(decoded.trackingId)

        when (val firstObject = correiosResponse.objeto.first()) {
            is CorreiosFoundObjeto -> {
                // Some Correios' packages are super wonky and while they are marked as delivered, they have duplicate events "package delievered to recipient" events (See: AA123456785BR)
                if (firstObject.events.any { it.type == EventType.PackageDeliveredToRecipient })
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.PackageAlreadyDelivered),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
                    )

                try {
                    loritta.services.packagesTracking.trackCorreiosPackage(UserId(user.id.value), decoded.trackingId)

                    context.updateMessage {
                        actionRow {
                            interactiveButton(
                                ButtonStyle.Primary,
                                context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.UnfollowPackageUpdates),
                                UnfollowPackageButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    UnfollowPackageData(
                                        context.user.id,
                                        decoded.trackingId
                                    )
                                )
                            )
                        }
                    }

                    context.sendEphemeralReply(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.YouAreNowFollowingThePackage(decoded.trackingId)),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSunglasses
                    )
                } catch (e: PackagesTrackingService.UserIsAlreadyTrackingPackageException) {
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.YouAreAlreadyFollowingThePackage),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
                    )
                } catch (e: PackagesTrackingService.UserIsAlreadyTrackingTooManyPackagesException) {
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.YouAreAlreadyFollowingTooManyPackages),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
                    )
                }
            }

            is CorreiosUnknownObjeto -> context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.ObjectNotFoundCorreios),
                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob
            )
        }
    }
}