package net.perfectdreams.spicymorenitta.routes

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.delay
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onChangeFunction
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
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.events.InputEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

class FanArtsRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/fanarts/{artist?}") {
    override val keepLoadingScreen: Boolean
        get() = true

    var currentArtistSortingMethod = ArtistSortingMethod.ALPHABETIC
    var currentFanArtSortingMethod = FanArtSortingMethod.OLD_TO_NEW
    var filterTag: String? = null
    var watchingUser: FanArtArtist? = null
    var fanArtArtists = listOf<FanArtArtist>()

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun onRender(call: ApplicationCall) {
        m.showLoadingScreen()
        currentArtistSortingMethod = ArtistSortingMethod.ALPHABETIC
        currentFanArtSortingMethod = FanArtSortingMethod.OLD_TO_NEW
        filterTag = null
        watchingUser = null

        SpicyMorenitta.INSTANCE.launch {
            val result = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/fan-arts?query=all")
            }

            val list = kotlinx.serialization.json.JSON.nonstrict.parseList<FanArtArtist>(result)

            fanArtArtists = list

            fixDummyNavbarHeight(call)
            switchContent(call)

            twoColumnLayout(
                    leftSidebar = {
                        leftSidebar(list)
                    },
                    rightSidebar = {
                        rightSidebar(list)
                    }
            )

            val artistId = call.parameters["artist"]
            var artistLookup: FanArtArtist? = null

            if (artistId != null)
                artistLookup = list.firstOrNull { it.id == artistId }

            if (artistLookup != null) {
                renderArtistFanArts(artistLookup)
            } else {
                renderFanArts(sortFanArts(list))
            }

            renderArtists(sortArtists(list))
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
            style = "max-width: 250px;"

            div {
                + "Organizar Artistas... "

                select {
                    id = "select-artist-order"
                    option {
                        value = "ascend-name"
                        +"por nome (A-Z)"
                    }
                    option {
                        value = "descend-name"
                        +"por nome (Z-A)"
                    }
                    option {
                        value = "descend-art"
                        +"por mais fan arts"
                    }

                    onChangeFunction = {
                        window.scrollTo(0.0, 0.0)
                        val selectFanArtOrder = document.select<HTMLSelectElement>("#select-artist-order").value

                        if (selectFanArtOrder == "ascend-name")
                            currentArtistSortingMethod = ArtistSortingMethod.ALPHABETIC

                        if (selectFanArtOrder == "descend-name")
                            currentArtistSortingMethod = ArtistSortingMethod.ALPHABETIC_REVERSED

                        if (selectFanArtOrder == "descend-art")
                            currentArtistSortingMethod = ArtistSortingMethod.FAN_ART_COUNT

                        renderArtists(sortArtists(list))
                    }
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

        hr {}

        div(classes = "artists") {

        }
    }

    fun DIV.rightSidebar(list: List<FanArtArtist>) {
        div(classes = "lori-holding") {
            img(classes = "lori-behind", src = "https://cdn.discordapp.com/attachments/544229872189309117/568465135170093086/Loritta_Fan_Arts_-_Miela.png")
            img(classes = "holding-image-1 icon-middle fan-art-in-hand", src = "https://loritta.website/assets/img/fanarts/Loritta_Anniversary_2019_-_Miela.png") {
                style = """opacity: 1;"""
            }
            img(classes = "holding-image-2 icon-middle fan-art-in-hand", src = "https://loritta.website/assets/img/fanarts/Loritta_3_-_Aniih.png") {
                style = """opacity: 0;"""
            }
            img(classes = "lori-arms", src = "https://cdn.discordapp.com/attachments/544229872189309117/568465133286719488/loritta_arms.png")
        }
        div {
            div {
                id = "artist-info"
            }

            div {
                + "Organizar Fan Arts... "

                select {
                    id = "select-fan-art-order"
                    option {
                        value = "ascend"
                        + "por data (ascendente)"
                    }
                    option {
                        value = "descend"
                        + "por data (descendente)"
                    }

                    onChangeFunction = {
                        window.scrollTo(0.0, 0.0)
                        val selectFanArtOrder = document.select<HTMLSelectElement>("#select-fan-art-order")

                        if (selectFanArtOrder.value == "ascend")
                            currentFanArtSortingMethod = FanArtSortingMethod.OLD_TO_NEW
                        else
                            currentFanArtSortingMethod = FanArtSortingMethod.NEW_TO_OLD

                        renderFanArts(sortFanArts(list))
                    }
                }
            }

            div {
                + "Mostrar Fan Arts... "

                select {
                    id = "select-fan-arts-type"
                    option {
                        value = "all"
                        + "Todas"
                    }
                    option {
                        value = "anniversary-2019"
                        + "Aniversário 2019"
                    }
                    option {
                        value = "april-fools-2019"
                        +"Primeiro de Abril 2019 (Furries)"
                    }
                    option {
                        value = "sweater-2019"
                        +"Moletons"
                    }
                    option {
                        value = "holiday-2019"
                        +"Férias de Inverno 2019"
                    }

                    onChangeFunction = {
                        window.scrollTo(0.0, 0.0)
                        val selectFanArtOrder = document.select<HTMLSelectElement>("#select-fan-arts-type").value

                        if (selectFanArtOrder == "all") {
                            filterTag = null
                            renderFanArts(sortFanArts(list))
                        } else {
                            filterTag = selectFanArtOrder
                            renderFanArts(sortFanArts(list))
                        }
                    }
                }
            }

            hr {}

            div {
                id = "fan-art-gallery"
            }
        }
    }

    fun sortArtists(artists: List<FanArtArtist>): List<FanArtArtist> {
        val sorted = when (currentArtistSortingMethod) {
            ArtistSortingMethod.ALPHABETIC -> artists.sortedBy { it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id }
            ArtistSortingMethod.ALPHABETIC_REVERSED -> artists.sortedByDescending { it.info.override?.name ?: it.user?.name ?: it.info.name ?: it.id }
            ArtistSortingMethod.FAN_ART_COUNT -> artists.sortedByDescending { it.fanArts.size }
            ArtistSortingMethod.BEST_ARTISTS -> listOf()
        }
        return sorted
    }

    fun sortFanArts(list: List<FanArtArtist>): List<FanArt> {
        var newList = list
        if (watchingUser != null)
            newList = newList.filter { it == watchingUser }

        var sorted = when (currentFanArtSortingMethod) {
            FanArtSortingMethod.OLD_TO_NEW -> newList.flatMap { it.fanArts }.sortedBy { it.createdAt.getTime() }
            FanArtSortingMethod.NEW_TO_OLD -> newList.flatMap { it.fanArts }.sortedByDescending { it.createdAt.getTime() }
        }

        val filterTag = filterTag
        if (filterTag != null)
            sorted = sorted.filter { filterTag in it.tags }
        return sorted
    }

    fun renderArtists(artists: List<FanArtArtist>) {
        val artistSidebar = document.select<HTMLDivElement>("#left-sidebar .artists")
        artistSidebar.clear()

        artistSidebar.append {
            div(classes = "entry") {
                style = "display: flex; align-items: center;"

                + "Ver todas as ${fanArtArtists.sumBy { it.fanArts.size }} fan arts"

                onClickFunction = {
                    val artistInfo = document.select<HTMLDivElement>("#artist-info")
                    artistInfo.clear()

                    watchingUser = null
                    renderFanArts(sortFanArts(fanArtArtists))
                    window.history.pushState(null, "", "/${m.websiteLocaleId}/fanarts")
                }
            }

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

    fun renderArtistFanArts(artist: FanArtArtist) {
        watchingUser = artist

        val artistInfo = document.select<HTMLDivElement>("#artist-info")
        artistInfo.clear()

        val fanArtGallery = document.select<HTMLDivElement>("#fan-art-gallery")
        fanArtGallery.clear()

        window.history.pushState(null, "", "/${m.websiteLocaleId}/fanarts/${artist.id}")

        artistInfo.append {
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
        fanArtGallery.clear()

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
        ALPHABETIC_REVERSED,
        FAN_ART_COUNT,
        BEST_ARTISTS
    }

    enum class FanArtSortingMethod {
        OLD_TO_NEW,
        NEW_TO_OLD
    }
}