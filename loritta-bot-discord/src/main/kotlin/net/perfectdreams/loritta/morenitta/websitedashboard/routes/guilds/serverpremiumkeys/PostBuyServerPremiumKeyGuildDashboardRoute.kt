package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.serverpremiumkeys

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.UserPremiumPlan
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

class PostBuyServerPremiumKeyGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/premium/buy") {
    companion object {
        private val ALLOWED_PLAN_VALUES = mapOf(
            34.99 to 3499L,
            59.99 to 5999L
        )
    }

    @Serializable
    data class BuyServerPremiumKeyRequest(
        val planValue: Double,
        val durationDays: Int? = null,
        val durationYears: Int? = null
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlan, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlan, member: Member) {
        val request = Json.decodeFromString<BuyServerPremiumKeyRequest>(call.receiveText())

        val monthlyAmountInCents = ALLOWED_PLAN_VALUES[request.planValue]
        if (monthlyAmountInCents == null) {
            call.respondHtmlFragment {
                blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.InvalidPlan)))
            }
            return
        }

        val totalAmountInCents: Long
        val durationDescription: String

        when {
            request.durationDays == 30 && request.durationYears == null -> {
                totalAmountInCents = monthlyAmountInCents
                durationDescription = i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.Duration.XDays(30))
            }
            request.durationYears == 1 && request.durationDays == null -> {
                totalAmountInCents = (monthlyAmountInCents * 12 * 0.8).toLong()
                durationDescription = i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.Duration.Annual)
            }
            else -> {
                call.respondHtmlFragment {
                    blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.InvalidPlan)))
                }
                return
            }
        }

        val planName = when (request.planValue) {
            34.99 -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.PlanNameForPayment.Recommended)
            59.99 -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.PlanNameForPayment.Complete)
            else -> i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.PlanNameForPayment.Premium)
        }

        val whoDonated = session.cachedUserIdentification.username

        val paymentUrl = website.loritta.perfectPaymentsClient.createPayment(
            website.loritta,
            session.userId,
            i18nContext.get(DashboardI18nKeysData.PremiumKeys.Buy.PaymentDescription.ServerPremium(planName, durationDescription, whoDonated, session.userId.toString())),
            totalAmountInCents,
            totalAmountInCents,
            PaymentReason.SERVER_PREMIUM_KEY,
            "LORITTA-SERVER-PREMIUM-%d",
            null,
            null,
            buildJsonObject {
                put("bundleType", "serverPremiumKey")
                put("planValue", request.planValue)
                if (request.durationDays != null) put("durationDays", request.durationDays)
                if (request.durationYears != null) put("durationYears", request.durationYears)
                put("guildId", guild.idLong)
            }
        )

        call.response.header("Bliss-Redirect", paymentUrl)
        call.respondText("", status = HttpStatusCode.NoContent)
    }
}
