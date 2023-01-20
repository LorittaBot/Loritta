package net.perfectdreams.loritta.morenitta.utils.gamersafer

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationRoles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferSuccessfulVerifications
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class GamerSaferRoleCheckerUpdater(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        logger.info { "Verifying GamerSafer protected roles..." }
        m.transaction {
            // Check all "Requires Verification" roles
            // TODO: Get ONLY guilds that are handled by this instance
            val requiresVerificationRoles = GamerSaferRequiresVerificationRoles.selectAll()

            println("requires verification roles count: ${requiresVerificationRoles.count()}")

            for (rvr in requiresVerificationRoles) {
                try {
                    println("check guild")
                    val guild = m.lorittaShards.getGuildById(rvr[GamerSaferRequiresVerificationRoles.guild]) ?: continue

                    // Role doesn't exist!
                    println("check role")
                    val role = guild.getRoleById(rvr[GamerSaferRequiresVerificationRoles.role]) ?: continue

                    // TODO: Cache member with role in the database, because the member may not be cached!
                    println("members with roles...")
                    val membersWithRoles = guild.getMembersWithRoles(role)

                    for (memberWithRole in membersWithRoles) {
                        println("for each member with role ${memberWithRole}")
                        // Is the user already have a pending verification request?
                        if (
                            GamerSaferRequiresVerificationUsers.select {
                                GamerSaferRequiresVerificationUsers.user eq memberWithRole.user.idLong and (GamerSaferRequiresVerificationUsers.guild eq guild.idLong) and (GamerSaferRequiresVerificationUsers.role eq role.idLong)
                            }.count() >= 1L
                        ) {
                            // If yes, ignore!
                            continue
                        }

                        // Has the user verified recently?
                        if (
                            GamerSaferSuccessfulVerifications.select {
                                GamerSaferSuccessfulVerifications.user eq memberWithRole.user.idLong and (GamerSaferSuccessfulVerifications.guild eq guild.idLong) and (GamerSaferSuccessfulVerifications.role eq role.idLong) and (GamerSaferSuccessfulVerifications.verifiedAt greaterEq Instant.now().minusSeconds(60))
                            }.count() >= 1L
                        ) {
                            // If yes, ignore!
                            continue
                        }

                        GamerSaferRequiresVerificationUsers.insert {
                            it[GamerSaferRequiresVerificationUsers.guild] = guild.idLong
                            it[GamerSaferRequiresVerificationUsers.role] = role.idLong
                            it[GamerSaferRequiresVerificationUsers.user] = memberWithRole.user.idLong
                            it[GamerSaferRequiresVerificationUsers.triggeredAt] = Instant.now()
                        }

                        // TODO: Check last verification
                        // TODO: oof, blocking the transaction (this is actually not a huuge deal but... you know)
                        guild.removeRoleFromMember(UserSnowflake.fromId(memberWithRole.idLong), role).await()

                        try {
                            memberWithRole.user.openPrivateChannel().await()
                                .sendMessage("Você precisa verificar novamente a sua pessoa no servidor ${guild.name} para você recuperar o cargo ${role.name}!")
                                .await()
                        } catch (e: Exception) {} // Can't send DM to this user
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to run required verification check for role ${rvr[GamerSaferRequiresVerificationRoles.role]} in guild ${rvr[GamerSaferRequiresVerificationRoles.guild]}"}
                }
            }
        }
    }
}