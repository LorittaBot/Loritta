package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.append
import net.perfectdreams.loritta.serializable.BackgroundListResponse
import net.perfectdreams.loritta.serializable.DefaultBackgroundVariation
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.LockerUtils
import net.perfectdreams.spicymorenitta.utils.awaitLoad
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Image

class AllBackgroundsListDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/dashboard/all-backgrounds") {
    override val keepLoadingScreen: Boolean
        get() = true

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get {
                url("${window.location.origin}/api/v1/loritta/backgrounds")
            }.bodyAsText()

            val backgroundListRequest = kotlinx.serialization.json.JSON.nonstrict.decodeFromString<BackgroundListResponse>(result)

            val profileWrapper = Image()
            debug("Awaiting load...")
            profileWrapper.awaitLoad("${window.location.origin}/api/v1/users/@me/profile")
            debug("Load complete!")

            val entriesDiv = document.select<HTMLDivElement>("#bundles-content")

            entriesDiv.append {
                div {
                    style = "justify-content: space-between; display: flex; flex-wrap: wrap;"
                }
                for ((background, variations) in backgroundListRequest.backgroundsWithVariations) {
                    div {
                        h1 {
                            + (background.id)
                        }

                        div {
                            style = "perspective: 500px;"

                            canvas("canvas-background-preview") {
                                id = "canvas-preview-${background.id}"
                                width = "800"
                                height = "600"
                                style = "width: 400px; height: 300px; transform: rotateY(-10deg);\n" +
                                        "\n" +
                                        "box-shadow: 0px 0px 10px black;\n" +
                                        "\n" +
                                        "margin: 20px;"
                            }
                        }

                        div {
                            b {
                                + "Nome: "
                            }
                            + locale["backgrounds.${background.id}.title"]
                        }

                        div {
                            b {
                                + "Descrição: "
                            }
                            + locale["backgrounds.${background.id}.description"]
                        }

                        div {
                            b {
                                + "Raridade: "
                            }
                            + background.rarity.name
                        }

                        div {
                            b {
                                + "Ativado? "
                            }
                            + background.enabled.toString()
                        }
                    }
                }
            }

            for ((background, variations) in backgroundListRequest.backgroundsWithVariations) {
                for (variation in variations) {

                }
                m.launch {
                    val validVariation = variations.firstOrNull { it is DefaultBackgroundVariation }
                    if (validVariation != null) {
                        val canvasPreview = document.select<HTMLCanvasElement>("#canvas-preview-${background.id}")

                        LockerUtils.prepareBackgroundCanvasPreview(
                            m,
                            backgroundListRequest.dreamStorageServiceUrl,
                            backgroundListRequest.namespace,
                            backgroundListRequest.etherealGambiUrl,
                            validVariation,
                            canvasPreview
                        )
                    }
                }
            }

            fixDummyNavbarHeight(call)
            m.fixLeftSidebarScroll {
                switchContent(call)
            }

            m.hideLoadingScreen()
        }
    }
}