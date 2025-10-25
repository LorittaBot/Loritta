package net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop

import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import kotlinx.coroutines.delay
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import net.perfectdreams.bliss.SSEBliss
import net.perfectdreams.bliss.SSEBlissSwap
import net.perfectdreams.bliss.SSECustomEvent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.SseEvent
import net.perfectdreams.loritta.morenitta.utils.extensions.writeSseEvent
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dailyShopItems
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class SSEDailyShopTimerUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/daily-shop/timer") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        // Makes SSE work behind nginx
        // https://stackoverflow.com/a/33414096/7271796
        call.response.header("X-Accel-Buffering", "no")
        call.response.cacheControl(CacheControl.NoCache(null))

        // Hacky!
        val locale = website.loritta.localeManager.getLocaleById("default")

        val currentDailyShop = website.loritta.transaction {
            DashboardDailyShopUtils.queryDailyShopResult(
                website.loritta,
                session.userId,
                dreamStorageServiceNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
            )
        }

        var lastDailyShopGeneratedAt = currentDailyShop.generatedAt
        var lastGeneratedText: String? = null

        call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
            while (true) {
                val currentDailyShop = website.loritta.transaction {
                    DashboardDailyShopUtils.queryDailyShopResult(
                        website.loritta,
                        session.userId,
                        dreamStorageServiceNamespace = website.loritta.dreamStorageService.getCachedNamespaceOrRetrieve()
                    )
                }

                writeSseEvent(
                    SseEvent(
                        data = Json.encodeToString<SSEBliss>(
                            SSECustomEvent(
                                "loritta:showToast",
                                "document",
                                Json.encodeToString<EmbeddedToast>(createEmbeddedToast(EmbeddedToast.Type.INFO, "tick", null))
                            )
                        )
                    )
                )

                if (lastDailyShopGeneratedAt != currentDailyShop.generatedAt) {
                    lastDailyShopGeneratedAt = currentDailyShop.generatedAt

                    // A bit hacky but hey, there's nothing a lot we can do rn
                    val galleryOfDreamsResponse = website.loritta.cachedGalleryOfDreamsDataResponse!!

                    writeSseEvent(
                        SseEvent(
                            data = Json.encodeToString<SSEBliss>(
                                SSEBlissSwap(
                                    createHTML(false)
                                        .body {
                                            dailyShopItems(
                                                i18nContext,
                                                locale,
                                                currentDailyShop,
                                                galleryOfDreamsResponse
                                            )
                                        },
                                    "body (innerHTML) -> #loritta-items-wrapper (innerHTML)"
                                )
                            )
                        )
                    )

                    writeSseEvent(
                        SseEvent(
                            data = Json.encodeToString<SSEBliss>(
                                SSECustomEvent(
                                    "loritta:showToast",
                                    "document",
                                    Json.encodeToString<EmbeddedToast>(createEmbeddedToast(EmbeddedToast.Type.INFO, "Loja atualizada!", null))
                                )
                            )
                        )
                    )
                }

                val newText = DateUtils.formatDateDiff(i18nContext, System.currentTimeMillis(), DashboardDailyShopUtils.getShopResetsEpochMilli(), maxParts = 1)
                if (lastGeneratedText == null || lastGeneratedText != newText) {
                    writeSseEvent(
                        SseEvent(
                            data = Json.encodeToString<SSEBliss>(
                                SSEBlissSwap(
                                    createHTML(false)
                                        .body {
                                            text(newText)
                                        },
                                    "body (innerHTML) -> .shop-timer-date (innerHTML)"
                                )
                            )
                        )
                    )
                    lastGeneratedText = newText
                }

                flush()
                delay(1_000)
            }
        }
    }
}