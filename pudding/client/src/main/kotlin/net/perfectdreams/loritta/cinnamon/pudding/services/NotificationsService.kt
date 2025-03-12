package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.serializable.UserNotification
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class NotificationsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getUserTotalNotifications(
        userId: UserId
    ) = pudding.transaction {
        userNotificationsQuery(userId).count()
    }

    suspend fun getUserNotification(
        userId: UserId,
        notificationId: Long
    ): UserNotification? {
        return pudding.transaction {
            userNotificationQuery(userId, notificationId)
                .limit(1)
                .firstOrNull()
                ?.let {
                    UserNotification.fromRow(it)
                }
        }
    }

    suspend fun getUserNotifications(
        userId: UserId,
        limit: Int,
        offset: Long
    ): List<UserNotification> {
        return pudding.transaction {
            userNotificationsQuery(userId)
                .orderBy(UserNotifications.id, SortOrder.DESC)
                .offset(offset).limit(limit)
                .map {
                    UserNotification.fromRow(it)
                }
        }
    }

    // If we want to filter for specific transactions, check if the table ID is null!
    // Example: BrokerSonhosTransactionsLog.id isNotNull
    private fun userNotificationsQuery(
        userId: UserId
    ) = UserNotifications
        .leftJoin(DailyTaxWarnUserNotifications)
        .leftJoin(DailyTaxTaxedUserNotifications)
        .leftJoin(CorreiosPackageUpdateUserNotifications.leftJoin(TrackedCorreiosPackagesEvents))
        .selectAll()
        .where {
            (UserNotifications.user eq userId.value.toLong())
        }

    // If we want to filter for specific transactions, check if the table ID is null!
    // Example: BrokerSonhosTransactionsLog.id isNotNull
    private fun userNotificationQuery(
        userId: UserId,
        notificationId: Long
    ) = UserNotifications
        .leftJoin(DailyTaxWarnUserNotifications)
        .leftJoin(DailyTaxTaxedUserNotifications)
        .leftJoin(CorreiosPackageUpdateUserNotifications.leftJoin(TrackedCorreiosPackagesEvents))
        .selectAll().where {
            (UserNotifications.user eq userId.value.toLong()) and (UserNotifications.id eq notificationId)
        }
}