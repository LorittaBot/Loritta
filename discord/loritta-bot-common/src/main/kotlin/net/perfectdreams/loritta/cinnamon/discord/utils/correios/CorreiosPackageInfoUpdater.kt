package net.perfectdreams.loritta.cinnamon.discord.utils.correios

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosFoundObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosUnknownObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.EventType
import net.perfectdreams.loritta.cinnamon.pudding.tables.PendingImportantNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UsersFollowingCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.common.utils.PendingImportantNotificationState
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.LocalDateTime

class CorreiosPackageInfoUpdater(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
        val KTX_DATETIME_CORREIOS_OFFSET = UtcOffset(-3)
        private val JAVA_TIME_CORREIOS_OFFSET = KTX_DATETIME_CORREIOS_OFFSET.toJavaZoneOffset()
    }

    override suspend fun run() {
        logger.info { "Updating packages information..." }

        try {
            m.pudding.transaction {
                val trackedPackages = TrackedCorreiosPackages.select { TrackedCorreiosPackages.delivered eq false and (TrackedCorreiosPackages.unknownPackage eq false ) }
                    .map { it[TrackedCorreiosPackages.trackingId] }

                if (trackedPackages.isEmpty()) {
                    logger.info { "No packages need to be tracked, skipping..." }
                    return@transaction
                }

                logger.info { "Querying information about packages $trackedPackages" }
                val objects = runBlocking {
                    m.correiosClient.getPackageInfo(
                        *trackedPackages.toTypedArray()
                    )
                }

                val now = Instant.now()

                objects.forEach { correiosPackage ->
                    when (correiosPackage) {
                        is CorreiosFoundObjeto -> {
                            // Check when last event was received
                            val lastEventReceivedAt = TrackedCorreiosPackagesEvents.select {
                                TrackedCorreiosPackagesEvents.trackingId eq correiosPackage.codObjeto
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
                                            it.dtHrCriado > lastEventReceivedAt.toKotlinInstant()
                                        }
                                    else
                                        it
                                }
                                .sortedBy { it.dtHrCriado } // the order doesn't really matter because we sort when querying the database, but at least it looks prettier when querying the database without a sort
                                .forEach { event ->
                                    val packageEventId = TrackedCorreiosPackagesEvents.insertAndGetId {
                                        it[TrackedCorreiosPackagesEvents.trackingId] = correiosPackage.codObjeto
                                        it[TrackedCorreiosPackagesEvents.triggeredAt] = event.dtHrCriado.toJavaInstant()
                                        it[TrackedCorreiosPackagesEvents.event] = Json.encodeToString(event)
                                    }

                                    val whoIsTrackingThisPackage = UsersFollowingCorreiosPackages.innerJoin(
                                        TrackedCorreiosPackages
                                    ).select {
                                        TrackedCorreiosPackages.trackingId eq correiosPackage.codObjeto
                                    }.map { it[UsersFollowingCorreiosPackages.user] }

                                    if (!hasNeverReceivedAnyEventsBefore) {
                                        for (user in whoIsTrackingThisPackage) {
                                            val userNotificationId = UserNotifications.insertAndGetId {
                                                it[UserNotifications.timestamp] = now
                                                it[UserNotifications.user] = user.value
                                            }

                                            CorreiosPackageUpdateUserNotifications.insert {
                                                it[CorreiosPackageUpdateUserNotifications.timestampLog] = userNotificationId
                                                it[CorreiosPackageUpdateUserNotifications.trackingId] = correiosPackage.codObjeto
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

                                    if (event.codigo == EventType.PackageDeliveredToRecipient) {
                                        // If it is delivered, update the status with "delivered"
                                        logger.info { "Package ${correiosPackage.codObjeto} has been delivered! Updating its status in our database..." }
                                        TrackedCorreiosPackages.update({ TrackedCorreiosPackages.trackingId eq correiosPackage.codObjeto }) {
                                            it[TrackedCorreiosPackages.delivered] = true
                                        }
                                    }
                                }
                        }
                        is CorreiosUnknownObjeto -> {
                            logger.info { "Package ${correiosPackage.codObjeto} is unknown! Updating its status in our database..." }
                            TrackedCorreiosPackages.update({ TrackedCorreiosPackages.trackingId eq correiosPackage.codObjeto }) {
                                it[TrackedCorreiosPackages.unknownPackage] = true
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while updating packages information!" }
        }
    }
}