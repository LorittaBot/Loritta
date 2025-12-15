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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configurableChannelList
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

open class GenericRemoveChannelFromListGuildDashboardRoute(
    website: LorittaDashboardWebServer,
    originalGuildPath: String,
    val removeEndpoint: String,
) : RequiresGuildAuthDashboardLocalizedRoute(website, originalGuildPath) {
    @Serializable
    data class RemoveChannelRequest(
        val channelsName: String,
        val swapToElementId: String,
        val channelId: Long,
        val channels: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<RemoveChannelRequest>(call.receiveText())

        // We COULD validate that the channel is not on the list, but it really doesn't matter, right?
        val newList = request.channels.toMutableSet()
        newList.remove(request.channelId)

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    "Canal removido!"
                )
            )

            configurableChannelList(
                i18nContext,
                guild,
                request.swapToElementId,
                request.channelsName,
                "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}$removeEndpoint",
                newList
            )
        }
    }
}