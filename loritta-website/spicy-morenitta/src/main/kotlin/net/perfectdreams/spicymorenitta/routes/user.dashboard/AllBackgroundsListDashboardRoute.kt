package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.builtins.ListSerializer
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.awaitLoad
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Image

class AllBackgroundsListDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/all-backgrounds") {
    override val keepLoadingScreen: Boolean
        get() = true

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/backgrounds")
            }

            val list = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(ListSerializer(Background.serializer()), result)

            val profileWrapper = Image()
            debug("Awaiting load...")
            profileWrapper.awaitLoad("${window.location.origin}/api/v1/users/@me/profile")
            debug("Load complete!")

            val entriesDiv = document.select<HTMLDivElement>("#bundles-content")

            entriesDiv.append {
                div {
                    style = "justify-content: space-between; display: flex; flex-wrap: wrap;"
                }
                for (background in list) {
                    div {
                        h1 {
                            + (background.internalName)
                        }

                        div {
                            style = "perspective: 500px;"

                            canvas("canvas-background-preview") {
                                id = "canvas-preview-${background.internalName}"
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
                            + locale["backgrounds.${background.internalName}.title"]
                        }

                        div {
                            b {
                                + "Descrição: "
                            }
                            + locale["backgrounds.${background.internalName}.description"]
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

            for (background in list) {
                m.launch {
                    val canvasPreview = document.select<HTMLCanvasElement>("#canvas-preview-${background.internalName}")

                    val backgroundImg = Image()
                    backgroundImg.awaitLoad("${window.location.origin}/assets/img/profiles/backgrounds/${background.imageFile}")

                    val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)
                    canvasPreviewContext
                            .drawImage(
                                    backgroundImg,
                                    (background.crop?.offsetX ?: 0).toDouble(),
                                    (background.crop?.offsetY ?: 0).toDouble(),
                                    (background.crop?.width ?: backgroundImg.width).toDouble(),
                                    (background.crop?.height ?: backgroundImg.height).toDouble(),
                                    0.0,
                                    0.0,
                                    800.0,
                                    600.0
                            )

                    canvasPreviewContext.drawImage(profileWrapper, 0.0, 0.0)
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