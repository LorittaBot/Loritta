package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h2
import kotlinx.html.js.onClickFunction
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.PaymentUtils
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement

class AvailableBundlesDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/bundles") {
    override val keepLoadingScreen: Boolean
        get() = true

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get {
                url("${window.location.origin}/api/v1/economy/bundles/sonhos")
            }.bodyAsText()

            val list = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(ListSerializer(Bundle.serializer()), result)

            fixDummyNavbarHeight(call)
            m.fixLeftSidebarScroll {
                switchContent(call)
            }

            val entriesDiv = document.select<HTMLDivElement>("#bundles-content")

            entriesDiv.append {
                div {
                    style = "display: flex; flex-wrap: wrap;"

                    // Only show active bundles
                    for (entry in list.filter { it.active }) {
                        div {
                            style = "text-align: center; padding: 8px;"
                            h2 {
                                + "${entry.sonhos} Sonhos"
                            }

                            button(classes = "button-discord button-discord-success pure-button") {
                                + "Comprar! (R$ ${entry.price})"

                                onClickFunction = {
                                    PaymentUtils.requestAndRedirectToPaymentUrl(
                                        buildJsonObject {
                                            put("id", entry.id.toString())
                                        },
                                        "${loriUrl}api/v1/economy/bundles/sonhos/${entry.id}"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            m.hideLoadingScreen()
        }
    }

    @Serializable
    class Bundle(
            val id: Long,
            val active: Boolean,
            val sonhos: Long,
            val price: Double
    )
}