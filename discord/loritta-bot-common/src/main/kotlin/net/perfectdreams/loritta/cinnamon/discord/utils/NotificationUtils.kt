package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaZoneOffset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosEvento
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.eventTypeWithStatus
import net.perfectdreams.loritta.cinnamon.pudding.data.*
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData

object NotificationUtils {
    private val KTX_DATETIME_CORREIOS_OFFSET = UtcOffset(-3)
    private val JAVA_TIME_CORREIOS_OFFSET = KTX_DATETIME_CORREIOS_OFFSET.toJavaZoneOffset()

    fun buildUserNotificationMessage(
        i18nContext: I18nContext,
        notification: UserNotification,
        lorittaWebsiteUrl: String
    ): MessageBuilder.() -> Unit {
        when (notification) {
            is DailyTaxTaxedUserNotification -> {
                return UserUtils.buildDailyTaxMessage(
                    i18nContext,
                    lorittaWebsiteUrl,
                    notification.user,
                    notification
                )
            }

            is DailyTaxWarnUserNotification -> {
                return UserUtils.buildDailyTaxMessage(
                    i18nContext,
                    lorittaWebsiteUrl,
                    notification.user,
                    notification
                )
            }

            is CorreiosPackageUpdateUserNotification -> {
                val event = Json.decodeFromJsonElement<CorreiosEvento>(notification.event)

                return {
                    embed {
                        // Package ID here
                        title =
                            i18nContext.get(I18nKeysData.Commands.Command.Package.PackageUpdate(notification.trackingId))

                        val eventTypeWithStatus = event.eventTypeWithStatus

                        field(
                            "${CorreiosUtils.getEmoji(eventTypeWithStatus)} ${event.descricao}",
                            CorreiosUtils.formatEvent(event),
                            false
                        )

                        image = CorreiosUtils.getImage(eventTypeWithStatus)
                        color = LorittaColors.CorreiosYellow.toKordColor()
                        timestamp = event.criacao.toInstant(KTX_DATETIME_CORREIOS_OFFSET)
                    }
                }
            }

            is UnknownUserNotification -> TODO()
        }
    }
}