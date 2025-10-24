package net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.datetime.Instant
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.shipeffects.PostBuyShipEffectsUserDashboardRoute.BuyShipEffectsRequest
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmPurchaseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PostPreBuyShipEffectsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/ship-effects/pre-buy") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val body = call.receiveText()
        val request = Json.decodeFromString<BuyShipEffectsRequest>(body)

        val profile = website.loritta.getLorittaProfile(session.userId)

        val activeShipEffects = website.loritta.transaction {
            ShipEffects.selectAll()
                .where {
                    ShipEffects.buyerId eq session.userId and (ShipEffects.expiresAt greater System.currentTimeMillis())
                }.map { row ->
                    ShipEffect(
                        row[ShipEffects.id].value,
                        UserId(row[ShipEffects.buyerId].toULong()),
                        UserId(row[ShipEffects.user1Id].toULong()),
                        UserId(row[ShipEffects.user2Id].toULong()),
                        row[ShipEffects.editedShipValue],
                        Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
                    )
                }
        }

        // Does the user already have an active ship effect for the same user + percentage?
        val showWarningModal = activeShipEffects.any { it.user2.value.toLong() == request.receivingEffectUserId && request.shipPercentage == it.editedShipValue }

        val confirmPurchaseModal = createEmbeddedConfirmPurchaseModal(
            i18nContext,
            3_000,
            profile?.money ?: 0
        ) {
            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/ship-effects/buy"
            attributes["bliss-swap:200"] = "body (innerHTML) -> #active-bribes (innerHTML)"
            attributes["bliss-vals-json"] = body
        }

        if (showWarningModal) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowModal(
                            createEmbeddedModal(
                                i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Title),
                                true,
                                {
                                    p {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Description))
                                    }
                                },
                                listOf(
                                    {
                                        defaultModalCloseButton(i18nContext)
                                    },
                                    {
                                        discordButton(ButtonStyle.PRIMARY) {
                                            openModalOnClick(confirmPurchaseModal)
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Continue))
                                        }
                                    }
                                )
                            )
                        )
                    }
            )
        } else {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowModal(confirmPurchaseModal)
                    }
            )
        }
    }
}