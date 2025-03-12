package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UsersFollowingCorreiosPackages
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class PackagesTrackingService(private val pudding: Pudding) : Service(pudding) {
    suspend fun trackCorreiosPackage(user: UserId, trackingId: String) {
        pudding.transaction {
            var trackingPackageEntryId = TrackedCorreiosPackages.selectFirstOrNull { TrackedCorreiosPackages.trackingId eq trackingId }
                ?.getOrNull(TrackedCorreiosPackages.id)

            if (trackingPackageEntryId == null) {
                trackingPackageEntryId = TrackedCorreiosPackages.insertAndGetId {
                    it[TrackedCorreiosPackages.trackingId] = trackingId
                    it[TrackedCorreiosPackages.addedAt] = Instant.now()
                    it[TrackedCorreiosPackages.delivered] = false
                    it[TrackedCorreiosPackages.unknownPackage] = false
                }
            }

            val profile = pudding.users.getOrCreateUserProfile(user)

            if (UsersFollowingCorreiosPackages.selectAll().where { UsersFollowingCorreiosPackages.user eq profile.id.value.toLong() and (UsersFollowingCorreiosPackages.trackedPackage eq trackingPackageEntryId) }.count() > 0)
                throw UserIsAlreadyTrackingPackageException()

            if (UsersFollowingCorreiosPackages.innerJoin(TrackedCorreiosPackages).selectAll().where { UsersFollowingCorreiosPackages.user eq profile.id.value.toLong() and (TrackedCorreiosPackages.delivered eq false) and (TrackedCorreiosPackages.unknownPackage eq false) }.count() == 25L)
                throw UserIsAlreadyTrackingTooManyPackagesException()

            UsersFollowingCorreiosPackages.insert {
                it[UsersFollowingCorreiosPackages.user] = profile.id.value.toLong()
                it[UsersFollowingCorreiosPackages.trackedPackage] = trackingPackageEntryId
            }
        }
    }

    suspend fun untrackCorreiosPackage(user: UserId, trackingId: String) {
        pudding.transaction {
            val trackingPackageEntryId: EntityID<Long> = TrackedCorreiosPackages.selectFirstOrNull { TrackedCorreiosPackages.trackingId eq trackingId }
                ?.getOrNull(TrackedCorreiosPackages.id) ?: return@transaction

            UsersFollowingCorreiosPackages.deleteWhere {
                UsersFollowingCorreiosPackages.user eq user.value.toLong() and (UsersFollowingCorreiosPackages.trackedPackage eq trackingPackageEntryId)
            }

            // If no one is tracking the package
            if (
                UsersFollowingCorreiosPackages.selectAll().where {
                    UsersFollowingCorreiosPackages.trackedPackage eq trackingPackageEntryId
                }.count() == 0L
            ) {
                TrackedCorreiosPackages.deleteWhere { TrackedCorreiosPackages.id eq trackingPackageEntryId }
            }
        }
    }

    suspend fun getTrackedCorreiosPackagesByUser(user: UserId) = pudding.transaction {
        UsersFollowingCorreiosPackages.innerJoin(TrackedCorreiosPackages).selectAll().where {
            UsersFollowingCorreiosPackages.user eq user.value.toLong() and (TrackedCorreiosPackages.delivered eq false and (TrackedCorreiosPackages.unknownPackage eq false))
        }.map { it[TrackedCorreiosPackages.trackingId] }
    }

    suspend fun getCorreiosPackageEvents(trackingId: String) = pudding.transaction {
        TrackedCorreiosPackagesEvents.selectAll().where { TrackedCorreiosPackagesEvents.trackingId eq trackingId }
            .map { it[TrackedCorreiosPackagesEvents.event] }
    }

    class UserIsAlreadyTrackingPackageException : RuntimeException()
    class UserIsAlreadyTrackingTooManyPackagesException : RuntimeException()
}