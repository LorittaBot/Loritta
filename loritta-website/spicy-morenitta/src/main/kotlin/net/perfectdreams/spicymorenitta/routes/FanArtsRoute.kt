package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.delay
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parseList
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.utils.FanArt
import net.perfectdreams.spicymorenitta.utils.FanArtArtist
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.InputEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

class FanArtsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/fanarts/{artist?}") {
    override val keepLoadingScreen: Boolean
        get() = true

    var currentMethod = ArtistSortingMethod.ALPHABETIC

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        m.showLoadingScreen()
        currentMethod = ArtistSortingMethod.ALPHABETIC

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/fan-arts?query=all")
            }

            println(result)

            val list = kotlinx.serialization.json.JSON.nonstrict.parseList<FanArtArtist>(result)

            fixDummyNavbarHeight(call)
            switchContent(call)

            twoColumnLayout(
                    leftSidebar = {
                        leftSidebar(list)
                    },
                    rightSidebar = {
                        rightSidebar()
                    }
            )

            val artistId = call.parameters["artist"]
            var artistLookup: FanArtArtist? = null

            if (artistId != null) {
                artistLookup = list.firstOrNull { it.id == artistId }
            }

            if (artistLookup != null) {
                renderArtistFanArts(artistLookup)
            } else {
                showAllFanArts(list, true)
            }

            renderArtists(list.sortedBy { it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id })
            m.hideLoadingScreen()

            SpicyMorenitta.INSTANCE.launch {
                while (true) {
                    val holdingImage1 = document.select<HTMLDivElement>("#right-sidebar .holding-image-1")
                    val holdingImage2 = document.select<HTMLDivElement>("#right-sidebar .holding-image-2")
                    delay(5_000)
                    repeat(100) {
                        holdingImage1.style.opacity = ((holdingImage1.style.opacity.toDouble()) - 0.01).toString()
                        holdingImage2.style.opacity = ((holdingImage2.style.opacity.toDouble()) + 0.01).toString()
                        delay(25)
                    }

                    // Alterar fan art do holdingImage1, já que agora ele está escondido
                    holdingImage1.setAttribute("src", "https://loritta.website/assets/img/fanarts/" + list.flatMap { it.fanArts }.random().fileName)

                    delay(5_000)
                    repeat(100) {
                        holdingImage2.style.opacity = ((holdingImage2.style.opacity.toDouble()) - 0.01).toString()
                        holdingImage1.style.opacity = ((holdingImage1.style.opacity.toDouble()) + 0.01).toString()
                        delay(25)
                    }

                    // Alterar fan art do holdingImage2, já que agora ele está escondido
                    holdingImage2.setAttribute("src", "https://loritta.website/assets/img/fanarts/" + list.flatMap { it.fanArts }.random().fileName)
                }
            }
        }
    }

    fun DIV.leftSidebar(list: List<FanArtArtist>) {
        div {
            style = "cursor: pointer; max-width: 250px;"
            div {
                +"Organizar por antiga -> nova"

                onClickFunction = {
                    window.scrollTo(0.0, 0.0)
                    showAllFanArts(list, true)
                }
            }
            div {
                +"Organizar por nova -> antiga"

                onClickFunction = {
                    window.scrollTo(0.0, 0.0)
                    showAllFanArts(list, false)
                }
            }
            div {
                +"Organizar artistas por nome"

                onClickFunction = {
                    currentMethod = ArtistSortingMethod.ALPHABETIC
                    renderArtists(sortArtists(list))
                }
            }
            div {
                +"Organizar mais fan arts -> menos fan arts"

                onClickFunction = {
                    currentMethod = ArtistSortingMethod.FAN_ART_COUNT
                    renderArtists(sortArtists(list))
                }
            }
            div {
                +"Mostrar artistas especiais"

                onClickFunction = {
                    currentMethod = ArtistSortingMethod.BEST_ARTISTS
                    renderArtists(listOf())
                }
            }
            div {
                +"Mostrar apenas fan arts de aniversário"

                onClickFunction = {
                    val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
                    fanArtGallery.clear()

                    window.scrollTo(0.0, 0.0)
                    renderFanArts(list.flatMap { it.fanArts }.filter { it.tags.contains("anniversary-2019") })
                }
            }
            div {
                +"Mostrar apenas fan arts de Primeiro de Abril (Furries)"

                onClickFunction = {
                    val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
                    fanArtGallery.clear()

                    window.scrollTo(0.0, 0.0)
                    renderFanArts(list.flatMap { it.fanArts }.filter { it.tags.contains("april-fools-2019") })
                }
            }
            div {
                +"Mostrar apenas fan arts de \"vamos fazer moletons da Lori porque agora dá para comprar\""

                onClickFunction = {
                    val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
                    fanArtGallery.clear()

                    window.scrollTo(0.0, 0.0)
                    renderFanArts(list.flatMap { it.fanArts }.filter { it.tags.contains("sweater-2019") })
                }
            }
        }
        div {
            + "Busca de artista por nome"
        }
        input(InputType.text) {
            onInputFunction = { event ->
                event as InputEvent
                val value = event.target!!.asDynamic().value
                println(value)
                renderArtists(sortArtists(list).filter { (it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id).contains(value as String, true) })
            }
        }

        div(classes = "artists") {

        }
    }

    fun DIV.rightSidebar() {
        div(classes = "lori-holding") {
            img(classes = "lori-behind", src = "https://cdn.discordapp.com/attachments/544229872189309117/568465135170093086/Loritta_Fan_Arts_-_Miela.png")
            img(classes = "holding-image-1 icon-middle", src = "https://loritta.website/assets/img/fanarts/Loritta_Anniversary_2019_-_Miela.png") {
                style = """position: absolute;
width: 148px;
height: auto;
left: 99px;
bottom: 72px;
filter: drop-shadow(rgba(0, 0, 0, 0.3) 0 0 15px);
opacity: 1;"""
            }
            img(classes = "holding-image-2 icon-middle", src = "https://loritta.website/assets/img/fanarts/Loritta_3_-_Aniih.png") {
                style = """position: absolute;
width: 148px;
height: auto;
left: 99px;
bottom: 72px;
filter: drop-shadow(rgba(0, 0, 0, 0.3) 0 0 15px);
opacity: 0;"""
            }
            img(classes = "lori-arms", src = "https://cdn.discordapp.com/attachments/544229872189309117/568465133286719488/loritta_arms.png")
        }
        div {
            id = "fan-art-gallery"
        }
    }

    fun sortArtists(artists: List<FanArtArtist>): List<FanArtArtist> {
        val sorted = when (currentMethod) {
            ArtistSortingMethod.ALPHABETIC -> artists.sortedBy { it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id }
            ArtistSortingMethod.FAN_ART_COUNT -> artists.sortedByDescending { it.fanArts.size }
            ArtistSortingMethod.BEST_ARTISTS -> listOf()
        }
        return sorted
    }

    fun renderArtists(artists: List<FanArtArtist>) {
        val artistSidebar = document.select<HTMLDivElement>("#left-sidebar .artists")
        artistSidebar.clear()

        artistSidebar.append {
            artists.forEach {
                div(classes = "entry") {
                    style = "display: flex; align-items: center;"
                    img(
                            src = (it.user?.effectiveAvatarUrl
                                    ?: "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1")
                    ) {
                        width = "32"
                        height = "32"
                        style = "border-radius: 999999px; margin-right: 6px;"
                    }
                    div {
                        style = "overflow: hidden;"
                        div(classes = "title") {
                            +(it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id)
                        }

                        div(classes = "subtitle") {
                            +"${it.fanArts.size} fan art${if (it.fanArts.size != 1) "s" else ""}"
                        }
                    }
                    onClickFunction = { event ->
                        renderArtistFanArts(it)
                    }
                }
            }
        }
    }

    fun showAllFanArts(list: List<FanArtArtist>, oldToNew: Boolean) {
        val sorted = list.flatMap { it.fanArts }.sortedBy { it.createdAt.getTime() }.toMutableList()
        if (!oldToNew)
            sorted.reverse()

        val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
        fanArtGallery.clear()

        renderFanArts(sorted)
    }

    fun renderArtistFanArts(artist: FanArtArtist) {
        val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
        fanArtGallery.clear()

        window.history.pushState(null, "", "/${m.websiteLocaleId}/fanarts/${artist.id}")

        fanArtGallery.append {
            div(classes = "user-info") {
                img(src = artist.user?.effectiveAvatarUrl ?: "https://cdn.discordapp.com/emojis/523176710439567392.png?v=1")

                div(classes = "text") {
                    div(classes = "name") {
                        + (artist.info.override?.name ?: artist.user?.name ?: artist.info.name ?: artist.id)
                    }
                    div {
                        + "*Sobre Mim do usuário aqui*"
                    }
                }
            }
        }

        renderFanArts(artist.fanArts.sortedBy { it.createdAt.getTime() })
    }

    fun renderFanArts(sorted: List<FanArt>) {
        val grouped = sorted.groupBy({ it.createdAt.getMonth().toString() + "-" + it.createdAt.getFullYear() }, { it })

        val dyn = {}.asDynamic()
        dyn["month"] = "long"

        val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")

        fanArtGallery.append {
            grouped.forEach {
                h2(classes = "left-horizontal-line uppercase") {
                    val date = it.value[0].createdAt

                    +(date.toLocaleString("pt-br", dyn) + " de ${date.getFullYear()}")
                }
                div(classes = "fan-arts-wrapper") {
                    for (fanArt in it.value) {
                        img(
                                classes = "fan-art"
                        ) {
                            attributes["lazy-load-url"] = "https://loritta.website/assets/img/fanarts/${fanArt.fileName}"
                            height = "100"
                            title = fanArt.createdAt.toDateString()
                        }
                    }
                }
            }
        }
        m.setUpLazyLoad()
    }

    enum class ArtistSortingMethod {
        ALPHABETIC,
        FAN_ART_COUNT,
        BEST_ARTISTS
    }
}