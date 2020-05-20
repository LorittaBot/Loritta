package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h2
import kotlinx.html.js.onClickFunction
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.PaymentUtils
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window

class AvailableBundlesDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/bundles") {
    override val keepLoadingScreen: Boolean
        get() = true

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/economy/bundles/sonhos")
            }

            val list = kotlinx.serialization.json.JSON.nonstrict.parse(Bundle.serializer().list, result)

            fixDummyNavbarHeight(call)
            m.fixLeftSidebarScroll {
                switchContent(call)
            }

            val entriesDiv = document.select<HTMLDivElement>("#bundles-content")

            entriesDiv.append {
                div {
                    style = "display: flex; flex-wrap: wrap;"

                    for (entry in list) {
                        div {
                            style = "text-align: center; padding: 8px;"
                            h2 {
                                + "${entry.sonhos} Sonhos"
                            }

                            button(classes = "button-discord button-discord-success pure-button") {
                                + "Comprar! (R$ ${entry.price})"

                                onClickFunction = {
                                    val o = object {
                                        val id = entry.id.toString()
                                    }

                                    println(JSON.stringify(o))

                                    PaymentUtils.openPaymentSelectionModal(o, "${loriUrl}api/v1/economy/bundles/sonhos/${entry.id}")
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