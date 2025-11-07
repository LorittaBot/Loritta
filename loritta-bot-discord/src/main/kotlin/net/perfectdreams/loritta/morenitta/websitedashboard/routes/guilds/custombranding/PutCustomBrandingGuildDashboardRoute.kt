package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.custombranding

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.requests.RestAction
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import kotlin.io.encoding.Base64

class PutCustomBrandingGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/custom-branding") {
    @Serializable
    data class InputFile(
        val name: String,
        val data: String,
    )

    @Serializable
    data class SaveCustomBrandingRequest(
        val displayName: String?,
        val bio: String?,
        val avatar: Set<InputFile>?,
        val banner: Set<InputFile>?,
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveCustomBrandingRequest>(call.receiveText())

        val manager = guild.selfMember.manager

        val avatar = if (request.avatar?.isNotEmpty() == true) Icon.from(Base64.decode(request.avatar.first().data)) else null
        val banner = if (request.banner?.isNotEmpty() == true) Icon.from(Base64.decode(request.banner.first().data)) else null
        val displayName = request.displayName
        val bio = request.bio

        // TODO: Check user plan permission for customize branding
//        if ((avatar != null || banner != null || bio != null || displayName != null) && userPremiumPlan.customBrandingEnabled) {
//            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
//                blissShowToast(
//                    createEmbeddedToast(
//                        EmbeddedToast.Type.WARN,
//                        "O servidor precisa ter premium para fazer isto!"
//                    )
//                )
//            }
//            return
//        }

        if (avatar != null) {
            manager.setAvatar(avatar).queue()
        }

        if (banner != null) {
            manager.setBanner(banner).queue()
        }

        if (bio != null) {
            manager.setBio(bio).queue()
        }

        if (displayName != null) {
            manager.setNickname(displayName).queue()
        }

        call.respondConfigSaved(i18nContext)
    }
}