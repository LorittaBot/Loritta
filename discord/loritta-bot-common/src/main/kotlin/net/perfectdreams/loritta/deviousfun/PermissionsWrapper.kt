package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions

@JvmInline
value class PermissionsWrapper(val permissions: Permissions) {
    /**
     * Checks if the [userId] has permission to talk with these [permissions]
     */
    suspend fun canTalk() = hasPermission(Permission.SendMessages)

    fun hasPermission(vararg permissionsToBeChecked: Permission, adminPermissionsBypassesCheck: Boolean = true): Boolean {
        if (adminPermissionsBypassesCheck && Permission.Administrator in permissions)
            return true

        return permissionsToBeChecked.all { it in this.permissions }
    }
}