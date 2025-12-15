package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableRoleList
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

open class GenericRemoveRoleFromListGuildDashboardRoute(
    website: LorittaDashboardWebServer,
    originalGuildPath: String,
    val removeEndpoint: String,
) : RequiresGuildAuthDashboardLocalizedRoute(website, originalGuildPath) {
    @Serializable
    data class RemoveRoleRequest(
        val roleId: Long,
        val roles: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<RemoveRoleRequest>(call.receiveText())

        // We COULD validate that the role is not on the list, but it really doesn't matter, right?
        val newList = request.roles.toMutableSet()
        newList.remove(request.roleId)

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Cargo removido!"
                )
            )

            configurableRoleList(
                i18nContext,
                guild,
                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}$removeEndpoint",
                newList
            )
        }
    }
}