package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.PatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.ReceivedPatchNotesNotifications
import net.perfectdreams.loritta.serializable.PatchNotesNotification
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class PatchNotesNotificationsService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets unread patch notes notifications and marks them as read
     *
     * Only patch notes that are between [PatchNotesNotifications.broadcastAfter] and [PatchNotesNotifications.expiresAt] are considered active and will be retrieved.
     *
     * @param user        the user ID
     * @param currentTime the current time
     * @return a list of all unread patch notes notifications
     */
    suspend fun getUnreadPatchNotesNotificationsAndMarkAsRead(
        user: UserId,
        currentTime: Instant
    ): List<PatchNotesNotification> {
        return pudding.transaction {
            val jInstant = currentTime.toJavaInstant()

            val receivedPatchNotesResultRows = PatchNotesNotifications.selectAll().where {
                PatchNotesNotifications.broadcastAfter lessEq jInstant and (PatchNotesNotifications.expiresAt greater jInstant) and (PatchNotesNotifications.id notInSubQuery ReceivedPatchNotesNotifications.select(ReceivedPatchNotesNotifications.patchNotesNotification).where { ReceivedPatchNotesNotifications.user eq user.value.toLong() })
            }.orderBy(PatchNotesNotifications.broadcastAfter, SortOrder.DESC)
                .toList() // We call to list here because every time you call "receivedPatchNotes" it would execute a new query

            var profile: PuddingUserProfile

            receivedPatchNotesResultRows.forEach { row ->
                // Only create the user profile if there are pending received patch notes
                profile = pudding.users.getOrCreateUserProfile(user)

                ReceivedPatchNotesNotifications.insert {
                    it[ReceivedPatchNotesNotifications.patchNotesNotification] = row[PatchNotesNotifications.id]
                    it[ReceivedPatchNotesNotifications.user] = profile.id.value.toLong()
                    it[ReceivedPatchNotesNotifications.receivedAt] = java.time.Instant.now()
                }
            }

            return@transaction receivedPatchNotesResultRows.map { PatchNotesNotification.fromRow(it) }
        }
    }
}