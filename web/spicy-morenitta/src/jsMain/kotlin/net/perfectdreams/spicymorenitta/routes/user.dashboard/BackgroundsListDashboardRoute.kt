package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.hasClass
import kotlinx.dom.removeClass
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.button
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h2
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import org.w3c.dom.*
import org.w3c.files.FileReader
import kotlin.js.Date

class BackgroundsListDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/dashboard/backgrounds") {
    override val keepLoadingScreen: Boolean
        get() = true
    private val activeBackgroundTitleElement: Element
        get() = document.select("#active-background-title")
    private val activeBackgroundDescriptionElement: Element
        get() = document.select("#active-background-description")
    private val activeBackgroundSetElement: Element
        get() = document.select("#active-background-set")
    private val activateBackgroundButtonElement: HTMLButtonElement
        get() = document.select(".activate-button")
    private var activeBackground: Background? = null
    private var enabledBackground: Background? = null

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        m.showLoadingScreen()

        SpicyMorenitta.INSTANCE.launch {
            fixDummyNavbarHeight(call)
            /* m.fixLeftSidebarScroll {
                switchContent(call)
            } */

            val userBackgroundsJob = m.async {
                debug("Retrieving profiles & background info...")
                val payload = http.get {
                    url("${window.location.origin}/api/v1/users/@me/backgrounds,settings,donations")
                }.bodyAsText()

                debug("Retrieved profiles & background info!")
                val result = JSON.nonstrict.decodeFromString(ProfileSectionsResponse.serializer(), payload)
                return@async result
            }

            // ===[ USER PROFILE IMAGE ]===
            val profileWrapperJob = m.async {
                val profileWrapper = Image()
                debug("Awaiting load...")
                profileWrapper.awaitLoad("${window.location.origin}/api/v1/users/@me/profile?t=${Date().getTime()}")
                debug("Load complete!")
                profileWrapper
            }

            val result = userBackgroundsJob.await()
            // Those should always be present due to our URL query, but who knows, right?
            // I tried using the "error" method to throw an IllegalArgumentException in a nice way... but the "Logger" class also has a "error" method, smh
            val backgroundsWrapper = result.backgrounds ?: throw IllegalArgumentException("Background Wrapper is not present! Bug?")
            val settingsWrapper = result.settings ?: throw IllegalArgumentException("Settings Wrapper is not present! Bug?")
            val donationsWrapper = result.donations ?: throw IllegalArgumentException("Donations Wrapper is not present! Bug?")
            val profileWrapper = profileWrapperJob.await()

            val entriesDiv = document.select<HTMLDivElement>("#bundles-content")

            val backgrounds = backgroundsWrapper.backgrounds.sortedByDescending { it.background.rarity.getBackgroundPrice() }
                .toMutableList()
                .apply {
                    this.add(
                        BackgroundWithVariations(
                            Background(
                                "random",
                                true,
                                Rarity.COMMON,
                                listOf(),
                                null
                            ),
                            listOf(
                                DefaultBackgroundVariation(
                                    "random",
                                    "image/png",
                                    null,
                                    BackgroundStorageType.DREAM_STORAGE_SERVICE
                                )
                            )
                        )
                    )
                    this.add(
                        BackgroundWithVariations(
                            Background(
                                "custom",
                                true,
                                Rarity.COMMON,
                                listOf(),
                                null
                            ),
                            listOf(
                                DefaultBackgroundVariation(
                                    "custom",
                                    "image/png",
                                    null,
                                    BackgroundStorageType.DREAM_STORAGE_SERVICE
                                )
                            )
                        )
                    )
                }

            entriesDiv.append {
                div("loritta-items-list") {
                    div(classes = "loritta-items-wrapper legacy-items-wrapper") {
                        for ((background, variations) in backgrounds) {
                            div(classes = "shop-item-entry rarity-${background.rarity.name.lowercase()}") {
                                div {
                                    style = "position: relative;"

                                    div {
                                        style = "overflow: hidden; line-height: 0;"

                                        canvas("canvas-background-preview") {
                                            id = "canvas-preview-${background.id}"
                                            width = "800"
                                            height = "600"
                                            style = "width: 100px; height: auto;"
                                        }
                                    }
                                }
                            }
                        }
                    }

                    div(classes = "loritta-items-sidebar") {
                        div(classes = "canvas-preview-wrapper") {
                            canvas("canvas-preview-only-bg") {
                                style = """width: 350px;"""
                                width = "800"
                                height = "600"
                            }

                            canvas("canvas-preview") {
                                style = """width: 350px;"""
                                width = "800"
                                height = "600"
                            }
                        }

                        h2 {
                            id = "active-background-title"
                            style = "word-break: break-word; text-align: center;"
                        }
                        div {
                            id = "active-background-description"
                            style = "margin-bottom: 10px;"
                        }

                        div {
                            id = "select-active-background-file"
                            style = "display; none;"

                            input(InputType.file) {
                                id = "select-active-background-file-input"

                                onClickFunction = {
                                    val plan = UserPremiumPlans.getPlanFromValue(donationsWrapper.value)

                                    if (!plan.customBackground) {
                                        it.preventDefault()
                                        Stuff.showPremiumFeatureModal {
                                            h2 {
                                                + "Personalize o seu Perfil!"
                                            }
                                            p {
                                                + "Faça upgrade para o Plano Recomendado para poder enviar as suas próprias imagens!"
                                            }
                                        }
                                    }
                                }

                                onChangeFunction = {
                                    activateBackgroundButtonElement.removeClass("button-discord-disabled")
                                    activateBackgroundButtonElement.addClass("button-discord-success")
                                }
                            }
                        }

                        div {
                            id = "active-background-set"
                        }

                        div {
                            style = "text-align: center;"

                            button(classes = "activate-button button-discord button-discord-success pure-button") {
                                style = "font-size: 1.5em;"
                                +"Ativar"

                                onClickFunction = {
                                    if (!activateBackgroundButtonElement.hasClass("button-discord-disabled")) {
                                        activeBackground?.let { activeBackground ->
                                            if (activeBackground.id == "custom") {
                                                val plan = UserPremiumPlans.getPlanFromValue(donationsWrapper.value)

                                                if (!plan.customBackground) {
                                                    Stuff.showPremiumFeatureModal {
                                                        h2 {
                                                            + "Personalize o seu Perfil!"
                                                        }
                                                        p {
                                                            + "Faça upgrade para o Plano Recomendado para poder enviar as suas próprias imagens!"
                                                        }
                                                    }
                                                    return@let
                                                }

                                                val file = page.getElementById("select-active-background-file-input").asDynamic().files[0]

                                                if (file != null) {
                                                    val reader = FileReader()

                                                    reader.readAsDataURL(file)
                                                    reader.onload = {
                                                        val imageAsBase64 = reader.result
                                                        SaveUtils.prepareSave("profile_design", endpoint = "${loriUrl}api/v1/users/self-profile", extras = {
                                                            it["setActiveBackground"] = activeBackground.id
                                                            it["data"] = (imageAsBase64 as? String)
                                                        }, onFinish = {
                                                            if (it.statusCode in 200..299) {
                                                                activateBackgroundButtonElement.addClass("button-discord-disabled")
                                                                activateBackgroundButtonElement.removeClass("button-discord-success")

                                                                enabledBackground = activeBackground
                                                            }
                                                        })
                                                        asDynamic()
                                                    }
                                                    return@let
                                                }
                                            }

                                            SaveUtils.prepareSave("profile_design", endpoint = "${loriUrl}api/v1/users/self-profile", extras = {
                                                it["setActiveBackground"] = activeBackground.id
                                            }, onFinish = {
                                                if (it.statusCode in 200..299) {
                                                    activateBackgroundButtonElement.addClass("button-discord-disabled")
                                                    activateBackgroundButtonElement.removeClass("button-discord-success")

                                                    enabledBackground = activeBackground
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for ((background, variations) in backgrounds) {
                    val canvasPreview = document.select<HTMLCanvasElement>("#canvas-preview-${background.id}")

                    m.launch {
                        val backgroundVariation = variations.filterIsInstance<DefaultBackgroundVariation>().first()
                        val (image) = LockerUtils.prepareBackgroundCanvasPreview(m, backgroundsWrapper.dreamStorageServiceUrl, backgroundsWrapper.dreamStorageServiceNamespace, backgroundsWrapper.etherealGambiUrl, backgroundVariation, canvasPreview)

                        canvasPreview.parentElement!!.parentElement!!.onClick {
                            updateActiveBackground(profileWrapper, background, backgroundVariation, image, listOf())
                        }

                        if (background.id == (settingsWrapper.activeBackground ?: "defaultBlue")) {
                            enabledBackground = background
                            updateActiveBackground(profileWrapper, background, backgroundVariation, image, listOf())
                        }
                    }
                }
            }
            m.hideLoadingScreen()
        }
    }

    fun updateActiveBackground(
        profileWrapper: Image,
        background: Background,
        backgroundVariation: DefaultBackgroundVariation,
        backgroundImg: Image,
        fanArtArtists: List<FanArtArtist>
    ) {
        this.activeBackground = background

        if (enabledBackground == background) {
            activateBackgroundButtonElement.addClass("button-discord-disabled")
            activateBackgroundButtonElement.removeClass("button-discord-success")
        } else {
            activateBackgroundButtonElement.removeClass("button-discord-disabled")
            activateBackgroundButtonElement.addClass("button-discord-success")
        }

        if (background.id == "custom") {
            document.select<HTMLDivElement>("#select-active-background-file")
                .style.display = ""
        } else {
            document.select<HTMLDivElement>("#select-active-background-file")
                .style.display = "none"
        }

        val canvasCheckout = document.select<HTMLCanvasElement>(".canvas-preview")
        val canvasCheckoutOnlyBg = document.select<HTMLCanvasElement>(".canvas-preview-only-bg")

        val canvasPreviewContext = (canvasCheckout.getContext("2d")!! as CanvasRenderingContext2D)
        val canvasPreviewOnlyBgContext = (canvasCheckoutOnlyBg.getContext("2d")!! as CanvasRenderingContext2D)

        canvasPreviewContext
            .drawImage(
                backgroundImg,
                (backgroundVariation.crop?.x ?: 0).toDouble(),
                (backgroundVariation.crop?.y ?: 0).toDouble(),
                (backgroundVariation.crop?.width ?: backgroundImg.width).toDouble(),
                (backgroundVariation.crop?.height ?: backgroundImg.height).toDouble(),
                0.0,
                0.0,
                800.0,
                600.0
            )
        canvasPreviewOnlyBgContext
            .drawImage(
                backgroundImg,
                (backgroundVariation.crop?.x ?: 0).toDouble(),
                (backgroundVariation.crop?.y ?: 0).toDouble(),
                (backgroundVariation.crop?.width ?: backgroundImg.width).toDouble(),
                (backgroundVariation.crop?.height ?: backgroundImg.height).toDouble(),
                0.0,
                0.0,
                800.0,
                600.0
            )

        canvasPreviewContext.drawImage(profileWrapper, 0.0, 0.0)

        activeBackgroundTitleElement.textContent = locale["backgrounds.${background.id}.title"]
        activeBackgroundDescriptionElement.textContent = locale["backgrounds.${background.id}.description"]

        val artists = fanArtArtists.filter { it.id in background.createdBy }
        if (artists.isNotEmpty()) {
            activeBackgroundDescriptionElement.append {
                artists.forEach {
                    div {
                        val name = (it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id)

                        +"Criado por "
                        a(href = "/fanarts/${it.id}") {
                            +name
                        }
                    }
                }
            }
        }

        activeBackgroundSetElement.clear()
        if (background.set != null) {
            activeBackgroundSetElement.let {
                it.clear()
                it.append {
                    i {
                        +"Parte do conjunto "

                        b {
                            +(locale["sets.${background.set}"])
                        }
                    }
                }
            }
        }
    }
}