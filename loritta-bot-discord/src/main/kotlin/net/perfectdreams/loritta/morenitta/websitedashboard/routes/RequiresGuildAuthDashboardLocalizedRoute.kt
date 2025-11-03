package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.header
import io.ktor.server.response.respondText
import io.ktor.server.util.getOrFail
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.common.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

abstract class RequiresGuildAuthDashboardLocalizedRoute(website: LorittaDashboardWebServer, originalGuildPath: String) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds/{guildId}$originalGuildPath") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val guildId = call.parameters.getOrFail("guildId").toLong()

        val guild = website.loritta.lorittaShards.getGuildById(guildId)

        if (guild == null) {
            if (call.request.header("Bliss-Request") == "true") {
                call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                    blissShowModal(
                        createEmbeddedModal(
                            "Adicionar a Loritta",
                            true,
                            {
                                text("Adicione a Loritta no servidor!")
                            },
                            listOf(
                                {
                                    defaultModalCloseButton(i18nContext)
                                },
                                {
                                    discordButtonLink(
                                        ButtonStyle.PRIMARY,
                                        href = net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL(
                                            website.loritta,
                                            guildId,
                                            null
                                        ).toString()
                                    ) {
                                        text("Adicionar")
                                    }
                                }
                            )
                        )
                    )
                }
            } else {
                call.respondText("Unknown Guild!")
            }
            return
        }

        val member = try {
            guild.retrieveMember(UserSnowflake.fromId(session.userId)).await()
        } catch (e: ErrorResponseException) {
            onUnauthenticatedGuildRequest(call, i18nContext, session, theme)
            return
        }

        val guildPremiumPlan = website.loritta.transaction {
            val guildPremiumKeys = website.loritta.transaction {
                DonationKeys.selectAll()
                    .where {
                        DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                    }
                    .toList()
            }

            val plan = ServerPremiumPlans.getPlanFromValue(guildPremiumKeys.sumOf { it[DonationKeys.value] })

            plan
        }

        if (member.isOwner || member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_SERVER)) {
            onAuthenticatedGuildRequest(call, i18nContext, session, userPremiumPlan, theme, shimejiSettings, guild, guildPremiumPlan)
        } else {
            onUnauthenticatedGuildRequest(call, i18nContext, session, theme)
        }
    }

    abstract suspend fun onAuthenticatedGuildRequest(
        call: ApplicationCall,
        i18nContext: I18nContext,
        session: UserSession,
        userPremiumPlan: UserPremiumPlans,
        theme: ColorTheme,
        shimejiSettings: LorittaShimejiSettings,
        guild: Guild,
        guildPremiumPlan: ServerPremiumPlans
    )

    suspend fun onUnauthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        if (call.request.header("Bliss-Request") == "true") {
            call.respondHtmlFragment(status = HttpStatusCode.NotFound) {
                blissShowModal(
                    createEmbeddedModal(
                        "Sem Permissão",
                        true,
                        {
                            text("Você não tem permissão para acessar as configurações deste servidor!")
                        },
                        listOf {
                            defaultModalCloseButton(i18nContext)
                        }
                    )
                )
            }
        } else {
            call.respondText("Requires Guild Login!")
        }
    }
}