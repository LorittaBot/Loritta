package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxbox

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxBoxConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

class PutTaxBoxGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/tax-box") {
    @Serializable
    data class SaveTaxBoxRequest(
        val enabled: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val request = Json.decodeFromString<SaveTaxBoxRequest>(call.receiveText())
        if (request.enabled && !guildPremiumPlan.taxBox) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "O servidor precisa ter premium para fazer isto!"
                    )
                )
            }
            return
        }

        website.loritta.transaction {
            TaxBoxConfigs.upsert(TaxBoxConfigs.id) {
                it[TaxBoxConfigs.id] = guild.idLong
                it[TaxBoxConfigs.enabled] = request.enabled
                it[TaxBoxConfigs.updatedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            }

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.CHANGED_TAX_BOX
            )
        }

        call.respondConfigSaved(i18nContext)
    }
}
