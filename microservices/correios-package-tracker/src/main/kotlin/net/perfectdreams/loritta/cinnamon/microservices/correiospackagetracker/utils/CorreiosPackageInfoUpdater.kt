package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneOffset
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.common.utils.PendingImportantNotificationState
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.CorreiosPackageTracker
import net.perfectdreams.loritta.cinnamon.platform.utils.CorreiosUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.ImportantNotificationDatabaseMessageBuilder
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.eventTypeWithStatus
import net.perfectdreams.loritta.cinnamon.platform.utils.embed
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UsersFollowingCorreiosPackages
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.LocalDateTime

class CorreiosPackageInfoUpdater(val m: CorreiosPackageTracker) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
        val KTX_DATETIME_CORREIOS_OFFSET = UtcOffset(-3)
        private val JAVA_TIME_CORREIOS_OFFSET = KTX_DATETIME_CORREIOS_OFFSET.toJavaZoneOffset()
    }

    override suspend fun runCoroutine() {
        // TODO: proper i18n
        val i18nContext = m.languageManager.getI18nContextById("pt")

        logger.info { "Updating packages information..." }

        try {
            m.services.transaction {
                val trackedPackages = TrackedCorreiosPackages.select { TrackedCorreiosPackages.delivered eq false and (TrackedCorreiosPackages.unknownPackage eq false ) }
                    .map { it[TrackedCorreiosPackages.trackingId] }

                if (trackedPackages.isEmpty()) {
                    logger.info { "No packages need to be tracked, skipping..." }
                    return@transaction
                }

                logger.info { "Querying information about packages $trackedPackages" }
                val packageInformations = m.correiosClient.getPackageInfo(
                    *trackedPackages.toTypedArray()
                )

                packageInformations.objeto.forEach { correiosPackage ->
                    when (correiosPackage) {
                        is CorreiosFoundObjeto -> {
                            // Check when last event was received
                            val lastEventReceivedAt = TrackedCorreiosPackagesEvents.select {
                                TrackedCorreiosPackagesEvents.trackingId eq correiosPackage.numero
                            }.orderBy(TrackedCorreiosPackagesEvents.triggeredAt, SortOrder.DESC)
                                .limit(1)
                                .firstOrNull()
                                ?.getOrNull(TrackedCorreiosPackagesEvents.triggeredAt)

                            // If this is false, then we won't need to notify the user about package updates, because we are filling our db with all the current events
                            val hasNeverReceivedAnyEventsBefore = lastEventReceivedAt == null

                            correiosPackage.events
                                .let {
                                    if (lastEventReceivedAt != null) // I wanted to use hasNeverReceivedAnyEventsBefore here but Kotlin isn't smart enough to know that it is non null
                                        it.filter {
                                            it.criacao > LocalDateTime.ofEpochSecond(
                                                lastEventReceivedAt.epochSecond,
                                                0,
                                                JAVA_TIME_CORREIOS_OFFSET
                                            )
                                                .toKotlinLocalDateTime()
                                        }
                                    else
                                        it
                                }
                                .sortedBy { it.criacao } // the order doesn't really matter because we sort when querying the database, but at least it looks prettier when querying the database without a sort
                                .forEach { event ->
                                    TrackedCorreiosPackagesEvents.insertAndGetId {
                                        it[TrackedCorreiosPackagesEvents.trackingId] = correiosPackage.numero
                                        it[TrackedCorreiosPackagesEvents.triggeredAt] = event.criacao.toInstant(KTX_DATETIME_CORREIOS_OFFSET)
                                            .toJavaInstant()
                                        it[TrackedCorreiosPackagesEvents.event] = Json.encodeToString(event)
                                    }

                                    val whoIsTrackingThisPackage = UsersFollowingCorreiosPackages.innerJoin(TrackedCorreiosPackages).select {
                                        TrackedCorreiosPackages.trackingId eq correiosPackage.numero
                                    }.map { it[UsersFollowingCorreiosPackages.user] }

                                    if (!hasNeverReceivedAnyEventsBefore) {
                                        for (user in whoIsTrackingThisPackage) {
                                            val message = ImportantNotificationDatabaseMessageBuilder().apply {
                                                embed {
                                                    // Package ID here
                                                    title = i18nContext.get(I18nKeysData.Commands.Command.Package.PackageUpdate(correiosPackage.numero))

                                                    val eventTypeWithStatus = event.eventTypeWithStatus

                                                    field(
                                                        "${CorreiosUtils.getEmoji(eventTypeWithStatus)} ${event.descricao}",
                                                        CorreiosUtils.formatEvent(event),
                                                        false
                                                    )

                                                    image = CorreiosUtils.getImage(eventTypeWithStatus)
                                                    color = LorittaColors.CorreiosYellow.toKordColor()
                                                    timestamp = event.criacao.toInstant(CorreiosPackageInfoUpdater.KTX_DATETIME_CORREIOS_OFFSET)
                                                }
                                            }.toMessage()

                                            PendingImportantNotifications.insert {
                                                it[PendingImportantNotifications.userId] = user.value
                                                it[PendingImportantNotifications.submittedAt] = Instant.now()
                                                it[PendingImportantNotifications.message] = Json.encodeToString(message)
                                                it[PendingImportantNotifications.state] = PendingImportantNotificationState.PENDING
                                            }
                                        }
                                    }

                                    if (event.type == EventType.PackageDeliveredToRecipient) {
                                        // If it is delieverd, update the status with "delivered"
                                        logger.info { "Package ${correiosPackage.numero} has been delivered! Updating its status in our database..." }
                                        TrackedCorreiosPackages.update({ TrackedCorreiosPackages.trackingId eq correiosPackage.numero }) {
                                            it[TrackedCorreiosPackages.delivered] = true
                                        }
                                    }
                                }
                        }
                        is CorreiosUnknownObjeto -> {
                            logger.info { "Package ${correiosPackage.numero} is unknown! Updating its status in our database..." }
                            TrackedCorreiosPackages.update({ TrackedCorreiosPackages.trackingId eq correiosPackage.numero }) {
                                it[TrackedCorreiosPackages.unknownPackage] = true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while updating packages information!" }
        }
    }
}