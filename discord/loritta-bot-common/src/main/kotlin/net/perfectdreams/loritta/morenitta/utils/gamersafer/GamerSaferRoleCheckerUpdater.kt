package net.perfectdreams.loritta.morenitta.utils.gamersafer

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationRoles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferRequiresVerificationUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferSuccessfulVerifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferUserRoles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import org.jetbrains.exposed.sql.*
import java.time.Instant

class GamerSaferRoleCheckerUpdater(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        logger.info { "Verifying GamerSafer protected roles..." }
        val now = Instant.now()

        m.transaction {
            // Check all "Requires Verification" roles
            // TODO: Get ONLY guilds that are handled by this instance
            val gsVerificationRoles = GamerSaferUserRoles.selectAll()

            for (gsVerificationRole in gsVerificationRoles) {
                try {
                    val lastUserVerification = GamerSaferSuccessfulVerifications.select {
                        GamerSaferSuccessfulVerifications.user eq gsVerificationRole[GamerSaferUserRoles.user] and (GamerSaferSuccessfulVerifications.guild eq gsVerificationRole[GamerSaferUserRoles.guild])
                    }.orderBy(GamerSaferSuccessfulVerifications.verifiedAt, SortOrder.DESC)
                        .firstOrNull()

                    if (lastUserVerification == null || now >= lastUserVerification[GamerSaferSuccessfulVerifications.verifiedAt].plusMillis(
                            gsVerificationRole[GamerSaferUserRoles.checkPeriod]
                        )
                    ) {
                        val guild = m.lorittaShards.getGuildById(gsVerificationRole[GamerSaferUserRoles.guild])
                        val role = guild?.getRoleById(gsVerificationRole[GamerSaferUserRoles.role])
                        val member = guild?.retrieveMemberOrNullById(gsVerificationRole[GamerSaferUserRoles.user])

                        if (role != null && member != null) {
                            try {
                                if (member.roles.contains(role)) {
                                    guild.removeRoleFromMember(UserSnowflake.fromId(member.idLong), role).await()

                                    member.user.openPrivateChannel().await()
                                        .sendMessage("Você precisa verificar novamente a sua pessoa no servidor ${guild.name} para você recuperar o cargo ${role.name}!")
                                        .await()
                                }
                            } catch (e: Exception) {
                            } // Can't send DM to this user
                        }
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to run required verification check for role ${gsVerificationRole[GamerSaferUserRoles.role]} in guild ${gsVerificationRole[GamerSaferUserRoles.guild]}"}
                }
            }
        }
    }
}