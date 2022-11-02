package net.perfectdreams.loritta.deviousfun.utils

import net.perfectdreams.loritta.deviouscache.data.toLightweightSnowflake
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.User

object PermissionInteractionUtils {
    suspend fun canInteract(guild: Guild, issuers: List<User>, targets: List<User>): Map<User, List<InteractionCheck>> {
        val interactionChecks = mutableMapOf<User, List<InteractionCheck>>()

        val ownerId = guild.ownerId

        for (target in targets) {
            val targetInteractionChecks = mutableListOf<InteractionCheck>()

            for (issuer in issuers) {
                if (issuer.id == target.id) {
                    // Haha, so funny...
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TRYING_TO_INTERACT_WITH_SELF
                        )
                    )
                    continue
                }

                if (target.id == ownerId) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TARGET_IS_OWNER
                        )
                    )
                    continue
                }

                // They are the owner, so they can do anything haha
                if (issuer.id == ownerId) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.SUCCESS
                        )
                    )
                    continue
                }

                // If the target is null, then it means that they aren't in the server and anything that we do against them should succeed
                val targetAsMember = guild.retrieveMemberOrNullById(target.idLong)
                if (targetAsMember == null) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.SUCCESS
                        )
                    )
                    continue
                }

                // If the issuer is null, then we have bigger problems lmao
                val issuerAsMember = guild.retrieveMemberById(issuer.idLong)

                // Using "member.roles" is expensive because we would query the role list (which is a Get Guild query) every single time
                // However we already have the guild object... there! So let's get the roles from there!
                val issuerRoles = guild.roles.filter { it.idSnowflake.toLightweightSnowflake() in issuerAsMember.roleIds }
                    .toList()
                    .sortedByDescending { it.positionRaw }
                val targetRoles = guild.roles.filter { it.idSnowflake.toLightweightSnowflake() in targetAsMember.roleIds }
                    .toList()
                    .sortedByDescending { it.positionRaw }

                val firstIssuerRole = issuerRoles.firstOrNull()
                val firstTargetRole = targetRoles.firstOrNull()

                val firstIssuerRoleRawPosition = firstIssuerRole?.positionRaw ?: Int.MIN_VALUE
                val firstTargetRoleRawPosition = firstTargetRole?.positionRaw ?: Int.MIN_VALUE

                // The issuer raw position must be higher than the target raw position
                if (firstTargetRoleRawPosition >= firstIssuerRoleRawPosition) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER
                        )
                    )
                    continue
                }

                // Okay, everything is correct, so we can interact with the user!
                targetInteractionChecks.add(
                    InteractionCheck(
                        issuer,
                        target,
                        InteractionCheckResult.SUCCESS
                    )
                )
            }

            interactionChecks[target] = targetInteractionChecks
        }

        return interactionChecks
    }

    data class InteractionCheck(
        val issuer: User,
        val target: User,
        val result: InteractionCheckResult
    )

    enum class InteractionCheckResult {
        SUCCESS,
        TARGET_IS_OWNER,
        TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER,
        TRYING_TO_INTERACT_WITH_SELF
    }
}