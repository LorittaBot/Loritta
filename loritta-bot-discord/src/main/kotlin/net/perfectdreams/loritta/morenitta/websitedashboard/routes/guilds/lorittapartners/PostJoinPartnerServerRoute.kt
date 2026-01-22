package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.lorittapartners

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaPartners
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.CreatePartnerInviteRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.CreatePartnerInviteResponse
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.partnerapplications.PartnerPermissionLevel
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.luna.toasts.EmbeddedToast
import org.jetbrains.exposed.sql.selectAll

class PostJoinPartnerServerRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/loritta-partners/join") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override fun getMethod() = HttpMethod.Post

    override suspend fun onAuthenticatedGuildRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: LorittaUserSession,
        userPremiumPlan: UserPremiumPlans,
        theme: ColorTheme,
        shimejiSettings: LorittaShimejiSettings,
        guild: Guild,
        guildPremiumPlan: ServerPremiumPlans,
        member: Member
    ) {
        // Verify the guild is an approved partner
        val isLorittaPartner = website.loritta.transaction {
            LorittaPartners.selectAll()
                .where { LorittaPartners.guildId eq guild.idLong }
                .count() != 0L
        }

        if (!isLorittaPartner) {
            call.respondHtmlFragment(status = HttpStatusCode.Forbidden) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "O seu servidor não é parceiro!"
                    )
                )
            }
            return
        }

        // Determine the generator's permission level
        val generatorPermissionLevel = when {
            member.isOwner -> PartnerPermissionLevel.OWNER
            member.hasPermission(Permission.ADMINISTRATOR) -> PartnerPermissionLevel.ADMINISTRATOR
            member.hasPermission(Permission.MANAGE_SERVER) -> PartnerPermissionLevel.MANAGER
            else -> error("User ${member.idLong} doesn't have any expected permission level! Bug?")
        }

        // Call RPC to create the invite
        try {
            val cluster = DiscordUtils.getLorittaClusterForGuildId(website.loritta, website.loritta.config.loritta.partnerApplications.partnerGuildId)
            val response = LorittaRPC.CreatePartnerInvite.execute(
                website.loritta,
                cluster,
                CreatePartnerInviteRequest(
                    userId = session.userId,
                    requestedForGuildId = guild.idLong,
                    partnerGuildId = website.loritta.config.loritta.partnerApplications.partnerGuildId,
                    partnerInviteChannelId = website.loritta.config.loritta.partnerApplications.inviteChannelId,
                    generatorPermissionLevel = generatorPermissionLevel
                )
            )

            when (response) {
                is CreatePartnerInviteResponse.Success -> {
                    call.response.header("Bliss-Redirect", "https://discord.gg/${response.inviteCode}")
                    call.respondText("", status = HttpStatusCode.NoContent)
                }
                CreatePartnerInviteResponse.GuildNotFound -> {
                    call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Algo deu errado ao tentar gerar o seu convite"
                            ) {
                                text("A Loritta não está no seu servidor")
                            }
                        )
                    }
                }
                CreatePartnerInviteResponse.ChannelNotFound -> {
                    call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Algo deu errado ao tentar gerar o seu convite"
                            ) {
                                text("O canal para criar convites no servidor do Loritta Partners não foi encontrado")
                            }
                        )
                    }
                }
                CreatePartnerInviteResponse.MissingPermissions -> {
                    call.respondHtmlFragment(status = HttpStatusCode.Forbidden) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Algo deu errado ao tentar gerar o seu convite"
                            ) {
                                text("A Loritta não tem permissão para criar convites no servidor do Loritta Partners")
                            }
                        )
                    }
                }
                CreatePartnerInviteResponse.InviteCreationFailed -> {
                    call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                "Algo deu errado ao tentar gerar o seu convite"
                            ) {
                                text("Algo deu errado ao tentar criar o convite. Tente novamente mais tarde!")
                            }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create partner invite for user ${session.userId} in guild ${guild.idLong}" }
            call.respondHtmlFragment(status = HttpStatusCode.InternalServerError) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Algo deu errado ao tentar gerar o seu convite"
                    ) {
                        text("Algo deu errado... Tente novamente mais tarde!")
                    }
                )
            }
        }
    }
}
