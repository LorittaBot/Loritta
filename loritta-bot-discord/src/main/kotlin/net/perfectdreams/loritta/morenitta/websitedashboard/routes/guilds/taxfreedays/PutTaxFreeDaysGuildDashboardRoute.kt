package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.taxfreedays

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DropsConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxFreeDaysConfigs
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
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

class PutTaxFreeDaysGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/tax-free-days") {
    @Serializable
    data class SaveTaxFreeDaysRequest(
        val enabledDuringFriday: Boolean,
        val enabledDuringSaturday: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveTaxFreeDaysRequest>(call.receiveText())
        if (request.enabledDuringFriday && !guildPremiumPlan.taxFreeFridays) {
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

        if (request.enabledDuringSaturday && !guildPremiumPlan.taxFreeSaturdays) {
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
            TaxFreeDaysConfigs.upsert(DropsConfigs.id) {
                it[TaxFreeDaysConfigs.id] = guild.idLong
                it[TaxFreeDaysConfigs.enabledDuringFriday] = request.enabledDuringFriday
                it[TaxFreeDaysConfigs.enabledDuringSaturday] = request.enabledDuringSaturday
                it[TaxFreeDaysConfigs.updatedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            }

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.CHANGED_TAX_FREE_DAYS
            )
        }

        call.respondConfigSaved(i18nContext)
    }
}