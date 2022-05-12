package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.PackagesTrackingService

class FollowPackageButtonClickExecutor(
    val loritta: LorittaCinnamon,
    val correios: CorreiosClient
) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ComponentExecutorIds.FOLLOW_PACKAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<FollowPackageData>(data)

        // Check if the package is already delivered and, if it is, don't allow the user to track it!
        val correiosResponse = correios.getPackageInfo(decoded.trackingId)

        when (val firstObject = correiosResponse.objeto.first()) {
            is CorreiosFoundObjeto -> {
                // Some Correios' packages are super wonky and while they are marked as delivered, they have duplicate events "package delievered to recipient" events (See: AA123456785BR)
                if (firstObject.events.any { it.type == EventType.PackageDeliveredToRecipient })
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.PackageAlreadyDelivered),
                        Emotes.LoriSob
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
                        Emotes.LoriSunglasses
                    )
                } catch (e: PackagesTrackingService.UserIsAlreadyTrackingPackageException) {
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.YouAreAlreadyFollowingThePackage),
                        Emotes.LoriSob
                    )
                } catch (e: PackagesTrackingService.UserIsAlreadyTrackingTooManyPackagesException) {
                    context.failEphemerally(
                        context.i18nContext.get(PackageCommand.I18N_PREFIX.Track.FollowPackage.YouAreAlreadyFollowingTooManyPackages),
                        Emotes.LoriSob
                    )
                }
            }

            is CorreiosUnknownObjeto -> context.failEphemerally(
                context.i18nContext.get(PackageCommand.I18N_PREFIX.ObjectNotFoundCorreios),
                Emotes.LoriSob
            )
        }
    }
}