package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneOffset
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.utils.PendingImportantNotificationState
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.CorreiosPackageTracker
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UsersFollowingCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
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

                val now = Instant.now()

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
                                    val packageEventId = TrackedCorreiosPackagesEvents.insertAndGetId {
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
                                            val userNotificationId = UserNotifications.insertAndGetId {
                                                it[UserNotifications.timestamp] = now
                                                it[UserNotifications.user] = user.value
                                            }

                                            CorreiosPackageUpdateUserNotifications.insert {
                                                it[CorreiosPackageUpdateUserNotifications.timestampLog] = userNotificationId
                                                it[CorreiosPackageUpdateUserNotifications.trackingId] = correiosPackage.numero
                                                it[CorreiosPackageUpdateUserNotifications.packageEvent] = packageEventId
                                            }

                                            PendingImportantNotifications.insert {
                                                it[PendingImportantNotifications.userId] = user.value
                                                it[PendingImportantNotifications.state] = PendingImportantNotificationState.PENDING
                                                it[PendingImportantNotifications.notification] = userNotificationId
                                                it[PendingImportantNotifications.submittedAt] = Instant.now()
                                            }
                                        }
                                    }

                                    if (event.type == EventType.PackageDeliveredToRecipient) {
                                        // If it is delivered, update the status with "delivered"
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