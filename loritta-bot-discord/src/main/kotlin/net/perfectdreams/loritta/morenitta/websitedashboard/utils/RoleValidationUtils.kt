package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import io.ktor.http.*
import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.luna.toasts.EmbeddedToast

/**
 * Validates that the given member can safely configure a collection of role IDs.
 *
 * This prevents privilege escalation by ensuring:
 * - All roles exist in the guild
 * - No role is @everyone
 * - No role is managed by an integration
 * - The user's highest role is above all target roles
 * - Loritta's highest role is above all target roles (so it can actually assign them)
 *
 * @param guild The Discord guild
 * @param member The member attempting to configure the roles (from dashboard session)
 * @param roleIds Collection of role IDs to validate
 * @return [RoleValidationResult.Success] if all roles are valid, or a specific error result
 */
fun validateRolesForConfiguration(
    guild: Guild,
    member: Member,
    roleIds: Collection<Long>
): RoleValidationResult {
    for (roleId in roleIds) {
        // Check if role exists
        val role = guild.getRoleById(roleId) ?: return RoleValidationResult.RoleNotFound(roleId)

        // Check if it's @everyone
        if (role.isPublicRole)
            return RoleValidationResult.RoleIsEveryone(role)

        // Check if it's a managed role (bot roles, integration roles, etc.)
        if (role.isManaged)
            return RoleValidationResult.RoleIsManaged(role)

        // Check if the user can interact with this role (prevents privilege escalation)
        if (!member.canInteract(role))
            return RoleValidationResult.UserCannotInteract(role)

        // Check if Loritta can interact with this role (so she can actually assign it)
        if (!guild.selfMember.canInteract(role))
            return RoleValidationResult.LorittaCannotInteract(role)
    }

    return RoleValidationResult.Success
}

/**
 * Responds with an appropriate error message for a role validation failure.
 *
 * @param result The validation result (should not be Success)
 * @param i18nContext The i18n context for localized messages
 */
suspend fun ApplicationCall.respondRoleValidationError(
    result: RoleValidationResult,
    i18nContext: I18nContext
) {
    val errorMessage = when (result) {
        is RoleValidationResult.RoleNotFound -> {
            i18nContext.get(I18nKeysData.Website.Dashboard.RoleValidation.RoleNotFound(roleId = result.roleId.toString()))
        }
        is RoleValidationResult.RoleIsEveryone -> {
            i18nContext.get(I18nKeysData.Website.Dashboard.RoleValidation.RoleIsEveryone)
        }
        is RoleValidationResult.RoleIsManaged -> {
            i18nContext.get(I18nKeysData.Website.Dashboard.RoleValidation.RoleIsManaged(roleName = result.role.name))
        }
        is RoleValidationResult.UserCannotInteract -> {
            i18nContext.get(I18nKeysData.Website.Dashboard.RoleValidation.UserCannotInteract(roleName = result.role.name))
        }
        is RoleValidationResult.LorittaCannotInteract -> {
            i18nContext.get(I18nKeysData.Website.Dashboard.RoleValidation.LorittaCannotInteract(roleName = result.role.name))
        }
        is RoleValidationResult.Success -> error("This should never happen!")
    }

    respondHtmlFragment(status = HttpStatusCode.Forbidden) {
        blissShowToast(
            createEmbeddedToast(
                EmbeddedToast.Type.WARN,
                errorMessage
            )
        )
    }
}

sealed class RoleValidationResult {
    object Success : RoleValidationResult()
    data class RoleNotFound(val roleId: Long) : RoleValidationResult()
    data class RoleIsEveryone(val role: Role) : RoleValidationResult()
    data class RoleIsManaged(val role: Role) : RoleValidationResult()
    data class UserCannotInteract(val role: Role) : RoleValidationResult()
    data class LorittaCannotInteract(val role: Role) : RoleValidationResult()
}
