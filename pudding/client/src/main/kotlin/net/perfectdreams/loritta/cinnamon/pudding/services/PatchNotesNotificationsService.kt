package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.PatchNotesNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.PatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.ReceivedPatchNotesNotifications
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class PatchNotesNotificationsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getUnreadPatchNotesNotificationsAndMarkAsRead(
        user: UserId,
        currentTime: Instant
    ): List<PatchNotesNotification> {
        return pudding.transaction {
            val jInstant = currentTime.toJavaInstant()

            val receivedPatchNotesResultRows = PatchNotesNotifications.select {
                PatchNotesNotifications.submittedAt lessEq jInstant and (PatchNotesNotifications.expiresAt greater jInstant) and (PatchNotesNotifications.id notInSubQuery ReceivedPatchNotesNotifications.slice(ReceivedPatchNotesNotifications.patchNotesNotification).select { ReceivedPatchNotesNotifications.user eq user.value.toLong() })
            }.orderBy(PatchNotesNotifications.submittedAt, SortOrder.DESC)
                .toList() // We call to list here because every time you call "receivedPatchNotes" it would execute a new query

            var profile: PuddingUserProfile

            receivedPatchNotesResultRows.forEach { row ->
                // Only create the user profile if there are pending received patch notes
                profile = pudding.users._getOrCreateUserProfile(user)

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