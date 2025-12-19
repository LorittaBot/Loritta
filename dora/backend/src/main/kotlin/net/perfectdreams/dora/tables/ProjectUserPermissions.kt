package net.perfectdreams.dora.tables

import net.perfectdreams.dora.ProjectPermissionLevel
import org.jetbrains.exposed.dao.id.LongIdTable

object ProjectUserPermissions : LongIdTable() {
    val project = reference("project", Projects).index()
    val user = reference("user", Users).index()
    val permissionLevel = enumerationByName<ProjectPermissionLevel>("permission_level", 64)
}