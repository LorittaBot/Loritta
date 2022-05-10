package net.perfectdreams.loritta.cinnamon.platform.commands.utils.packtracker

import dev.kord.common.entity.ButtonStyle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.PackageCommand
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.components.selectMenu
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.CorreiosUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosEvento
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.eventTypeWithStatus
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class SelectPackageSelectMenuExecutor(
    val loritta: LorittaCinnamon
) : SelectMenuWithDataExecutor {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.SELECT_PACKAGE_SELECT_MENU_EXECUTOR)

    override suspend fun onSelect(
        user: User,
        context: ComponentContext,
        data: String,
        values: List<String>
    ) {
        context.deferUpdateMessage()

        val viewingTrackingId = values.first()

        val packageEvents = context.loritta.services.packagesTracking.getCorreiosPackageEvents(viewingTrackingId)
            .map { Json.decodeFromString<CorreiosEvento>(it) }

        val trackingIdsTrackedByUser = context.loritta.services.packagesTracking.getTrackedCorreiosPackagesByUser(UserId(context.user.id.value))

        val lastEvent = packageEvents.maxByOrNull { it.criacao }

        context.updateMessage {
            embed {
                title = "`${viewingTrackingId}`"
                color = LorittaColors.CorreiosYellow.toKordColor()

                if (lastEvent != null) {
                    val eventTypeWithStatus = lastEvent.eventTypeWithStatus

                    field(
                        "${CorreiosUtils.getEmoji(eventTypeWithStatus)} ${lastEvent.descricao}",
                        CorreiosUtils.formatEvent(lastEvent),
                        false
                    )

                    image = CorreiosUtils.getImage(eventTypeWithStatus)
                }
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    GoBackToPackageListButtonClickExecutor,
                    ComponentDataUtils.encode(
                        BarebonesSingleUserComponentData(context.user.id)
                    )
                ) {
                    this.loriEmoji = Emotes.ChevronLeft
                }

                interactiveButton(
                    ButtonStyle.Primary,
                    TrackPackageButtonClickExecutor,
                    ComponentDataUtils.encode(
                        TrackPackageData(context.user.id, viewingTrackingId)
                    )
                ) {
                    this.label = context.i18nContext.get(PackageCommand.I18N_PREFIX.List.TrackPackage)
                    this.loriEmoji = Emotes.LoriReading
                }
            }

            actionRow {
                selectMenu(SelectPackageSelectMenuExecutor) {
                    for (trackingId in trackingIdsTrackedByUser) {
                        option(trackingId, trackingId) {
                            loriEmoji = Emotes.Correios
                            default = trackingId == viewingTrackingId
                        }
                    }
                }
            }
        }
    }
}