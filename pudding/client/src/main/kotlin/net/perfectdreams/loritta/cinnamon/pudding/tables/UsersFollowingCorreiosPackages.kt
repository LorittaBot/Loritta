package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object UsersFollowingCorreiosPackages : LongIdTable() {
    val user = reference("user", Profiles).index()
    val trackedPackage = reference("tracked_package", TrackedCorreiosPackages).index()
}