package net.perfectdreams.spicymorenitta.routes.user.dashboard

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.dom.clear
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JSON
import net.perfectdreams.loritta.api.utils.Rarity
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.DailyShopResult
import net.perfectdreams.loritta.serializable.ProfileDesign
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.FanArtArtist
import net.perfectdreams.spicymorenitta.utils.LockerUtils
import net.perfectdreams.spicymorenitta.utils.TingleModal
import net.perfectdreams.spicymorenitta.utils.TingleOptions
import net.perfectdreams.spicymorenitta.utils.awaitLoad
import net.perfectdreams.spicymorenitta.utils.generateAd
import net.perfectdreams.spicymorenitta.utils.locale.buildAsHtml
import net.perfectdreams.spicymorenitta.utils.loriUrl
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.utils.visibleModal
import org.w3c.dom.Audio
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.Image
import kotlin.js.Date

class DailyShopDashboardRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/user/@me/dashboard/daily-shop") {
    override val keepLoadingScreen: Boolean
        get() = true
    var generatedAt = -1L

    override fun onRender(call: ApplicationCall) {
        super.onRender(call)

        SpicyMorenitta.INSTANCE.launch {
            fixDummyNavbarHeight(call)
            /* m.fixLeftSidebarScroll {
                switchContent(call)
            } */

            m.launch {
                val timeUntilMidnight = getTimeUntilUTCMidnight()
                info("The page will be automatically updated @ $timeUntilMidnight")
                delay(timeUntilMidnight)
                m.showLoadingScreen()
                regen(true)
                m.hideLoadingScreen()
            }

            m.launch {
                while (true) {
                    val timeElement = document.select<HTMLDivElement?>("#when-will-be-the-next-update")
                    if (timeElement != null) {
                        val timeInSeconds = getTimeUntilUTCMidnight() / 1_000

                        val s = timeInSeconds % 60
                        val m = (timeInSeconds / 60) % 60
                        val h = (timeInSeconds / (60 * 60)) % 24

                        debug("time in seconds: $timeInSeconds")
                        debug("h: $h")
                        debug("m: $m")
                        debug("s: $s")

                        timeElement.clear()
                        timeElement.append {
                            span {
                                if (h != 0L) {
                                    +"${h + 1} Horas"
                                } else if (m != 0L) {
                                    +"$m Minutos"
                                } else if (s != 0L) {
                                    +"$s Segundos"
                                }
                            }
                        }
                    }

                    delay(1_000)
                }
            }

            regen(false)

            m.hideLoadingScreen()
        }
    }

    suspend fun regen(keepRechecking: Boolean) {
        // ===[ DAILY SHOP ]===
        val dailyJob = m.async {
            while (true) {
                val payload = http.get<String> {
                    url("${window.location.origin}/api/v1/economy/daily-shop")
                }

                val result = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(DailyShopResult.serializer(), payload)

                if (keepRechecking && generatedAt == result.generatedAt) {
                    info("Waiting for 5_000ms until we recheck the shop again, looks like it wasn't fully updated yet...")
                    delay(5_000)
                    continue
                }

                info("Shop was successfully updated! generatedAt = ${result.generatedAt}")
                generatedAt = result.generatedAt

                return@async result
            }
            throw RuntimeException("Should never happen!")
        }

        // ===[ USER BACKGROUNDS ]===
        val userBackgroundsJob = m.async {
            debug("Retrieving profiles & background info...")
            val payload = http.get<String> {
                url("${window.location.origin}/api/v1/users/@me/profiles,backgrounds,profileDesigns")
            }

            debug("Retrieved profiles & background info!")
            val result = kotlinx.serialization.json.JSON.nonstrict.decodeFromString(UserInfoResult.serializer(), payload)
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

        debug("await #1")
        val dailyShop = dailyJob.await()
        debug("await #2")
        val userBackgrounds = userBackgroundsJob.await()
        debug("await #3")
        val profileWrapper = profileWrapperJob.await()
        debug("await #4")

        val allArtists = (dailyShop.backgrounds.mapNotNull { it.createdBy } + dailyShop.profileDesigns.mapNotNull { it.createdBy })
                .flatten()
                .distinct()

        val fanArtArtistsJob = m.async {
            if (allArtists.isEmpty())
                return@async listOf<FanArtArtist>()

            val payload = http.get<String> {
                url("${window.location.origin}/api/v1/loritta/fan-arts?query=all&filter=${allArtists.joinToString(",")}")
            }

            JSON.nonstrict.decodeFromString(ListSerializer(FanArtArtist.serializer()), payload)
        }

        val fanArtArtists = fanArtArtistsJob.await()

        debug("Everything is retrieved! Let's go!")

        generateShop(dailyShop, userBackgrounds, profileWrapper, fanArtArtists)
    }

    fun getTimeUntilUTCMidnight(): Long {
        val date = Date()
        date.asDynamic().setUTCHours(24, 0, 0, 0)
        val now = Date().getTime()
        val diff = date.getTime().toLong() - now.toLong()
        return diff
    }

    fun generateShop(dailyShop: DailyShopResult, userInfoResult: UserInfoResult, profileWrapper: Image, fanArtArtists: List<FanArtArtist>) {
        info("Generating Shop...")
        val entriesDiv = document.select<HTMLDivElement>("#bundles-content")
        entriesDiv.clear()

        entriesDiv.append {
            div {
                id = "daily-shop"

                div {
                    style = "text-align: center;"

                    img(src = "https://loritta.website/assets/img/fanarts/Loritta_17_-_Allouette.png") {
                        style = "width: 400px;"
                    }

                    h1 {
                        + locale["website.dailyShop.title"]
                    }

                    p {
                        + "Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!"
                    }
                    p {
                        + "Todo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^"
                    }
                }

                div {
                    generateAd("5964074013", "Loritta Daily Shop")
                }

                div(classes = "shop-reset-timer") {
                    div(classes = "horizontal-line") {}

                    i(classes = "fas fa-stopwatch stopwatch") {}

                    div(classes = "shop-timer") {
                        div(classes = "shop-timer-date") {
                            id = "when-will-be-the-next-update"
                        }
                        div(classes = "shop-timer-subtitle") {
                            + locale["website.dailyShop.untilTheNextShopUpdate"]
                        }
                    }
                }

                div(classes = "loritta-items-wrapper") {
                    // A list containing all of the items in the shop
                    // We are now going to sort it by rarity
                    val allItemsInTheShop = (dailyShop.profileDesigns.map { ProfileDesignItemWrapper(it) } + dailyShop.backgrounds.map { BackgroundItemWrapper(it) })

                    val sortedByRarityAllItemsInTheShop = allItemsInTheShop.sortedByDescending { it.rarity }

                    for (shopItem in sortedByRarityAllItemsInTheShop) {
                        val bought = shopItem.hasBought(userInfoResult)

                        div(classes = "shop-item-entry rarity-${shopItem.rarity.name.toLowerCase()}") {
                            div {
                                style = "position: relative;"

                                div {
                                    style = "overflow: hidden; line-height: 0;"

                                    canvas("canvas-background-preview") {
                                        id = "canvas-preview-${shopItem.internalName}"
                                        width = "800"
                                        height = "600"
                                        // we try to keep the item shop with at least two columns if you are using 720p
                                        // 1080p can have at least three columns
                                        // we do that by setting a min-width (to avoid the items being waaaay too small) and a max-width (to avoid waaaay big items)
                                        // and a width: 24vw; just to reuse the window width
                                        style = "width: 24vw; min-width: 250px; max-width: 320px; height: auto;"
                                    }
                                }

                                div(classes = "item-entry-information rarity-${shopItem.rarity.name.toLowerCase()}") {
                                    div(classes = "item-entry-title rarity-${shopItem.rarity.name.toLowerCase()}") {
                                        +(locale["${shopItem.localePrefix}.${shopItem.internalName}.title"])
                                    }
                                    div(classes = "item-entry-type") {
                                        + locale["${shopItem.localePrefix}.name"]
                                    }
                                }

                                if (shopItem.tag != null) {
                                    div(classes = "item-new-tag") {
                                        + locale[shopItem.tag!!]
                                    }
                                }
                            }

                            div(classes = "item-user-information") {
                                if (bought) {
                                    i(classes = "fas fa-check") {
                                        style = "color: #80ff00;"
                                    }
                                    +" ${locale["website.dailyShop.itemAlreadyBought"]}"
                                } else {
                                    +"${shopItem.price} Sonhos"
                                }
                            }
                        }
                    }
                }
            }
        }

        // Setup the images for the item entires in the daily shop
        for (profileDesign in dailyShop.profileDesigns) {
            val bought = profileDesign.internalName in userInfoResult.profileDesigns.map { it.internalName }
            val canvasPreview = document.select<HTMLCanvasElement>("#canvas-preview-${profileDesign.internalName}")

            m.launch {
                val (image) = LockerUtils.prepareProfileDesignsCanvasPreview(m, profileDesign, canvasPreview)

                canvasPreview.parentElement!!.parentElement!!.onClick {
                    openProfileDesignInformation(userInfoResult, profileDesign, bought, image, fanArtArtists)
                }
            }
        }

        for (background in dailyShop.backgrounds) {
            val bought = background.internalName in userInfoResult.backgrounds.map { it.internalName }
            val canvasPreview = document.select<HTMLCanvasElement>("#canvas-preview-${background.internalName}")

            m.launch {
                val (image) = LockerUtils.prepareBackgroundCanvasPreview(m, dailyShop.dreamStorageServiceUrl, dailyShop.namespace, background, canvasPreview)

                canvasPreview.parentElement!!.parentElement!!.onClick {
                    openBackgroundInformation(userInfoResult, background, bought, StaticBackgroundImage(image), profileWrapper, fanArtArtists)
                }
            }
        }
    }

    @Serializable
    class UserInfoResult(
            val profile: Profile,
            var backgrounds: MutableList<Background>,
            var profileDesigns: MutableList<ProfileDesign>
    )

    @Serializable
    class Profile(
            val money: Long
    )

    fun openProfileDesignInformation(result: UserInfoResult, background: ProfileDesign, alreadyBought: Boolean, image: Image, fanArtArtists: List<FanArtArtist>) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true,
                        cssClass = arrayOf("tingle-modal--overflow")
                )
        )

        modal.setContent(
                document.create.div(classes = "item-shop-preview") {
                    div {
                        style = "flex-grow: 1;"
                        h1 {
                            style = "word-break: break-word; text-align: center;"

                            +(locale["profileDesigns.${background.internalName}.title"])
                        }
                        div {
                            style = "margin-bottom: 10px;"
                            +(locale["profileDesigns.${background.internalName}.description"])

                            if (background.createdBy != null) {
                                val artists = fanArtArtists.filter { it.id in background.createdBy!! }
                                if (artists.isNotEmpty()) {
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
                        }
                        if (background.set != null) {
                            div {
                                i {
                                    locale.buildAsHtml(locale["website.dailyShop.partOfTheSet"], { num ->
                                        if (num == 0) {
                                            b {
                                                + (locale["sets.${background.set}"])
                                            }
                                        }
                                    }) { + it }
                                }
                            }
                        }
                    }

                    div(classes = "canvas-preview-wrapper") {
                        canvas("canvas-preview-only-bg") {
                            style = """width: 400px;"""
                            width = "800"
                            height = "600"
                        }

                        canvas("canvas-preview") {
                            style = """width: 400px;"""
                            width = "800"
                            height = "600"
                        }
                    }
                }
        )

        val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")
        if (!alreadyBought) {
            val canBuy = result.profile.money >= background.rarity.getProfilePrice()
            val classes = if (canBuy) "button-discord-info" else "button-discord-disabled"
            modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Comprar", "buy-button-modal button-discord $classes pure-button button-discord-modal") {
                if (canBuy) {
                    m.launch {
                        m.showLoadingScreen()
                        val response = sendItemPurchaseRequest("profile-design", background.internalName)

                        if (response.status != HttpStatusCode.OK) {

                        }

                        visibleModal.select<HTMLElement>(".buy-button-modal")
                                .remove()

                        m.launch {
                            regen(false)
                            m.hideLoadingScreen()
                            cash.play()
                        }
                    }
                }
            }
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()

        val openModal = visibleModal

        val canvasCheckout = visibleModal.select<HTMLCanvasElement>(".canvas-preview")
        val canvasCheckoutOnlyBg = visibleModal.select<HTMLCanvasElement>(".canvas-preview-only-bg")

        val canvasPreviewContext = (canvasCheckout.getContext("2d")!! as CanvasRenderingContext2D)
        val canvasPreviewOnlyBgContext = (canvasCheckoutOnlyBg.getContext("2d")!! as CanvasRenderingContext2D)

        canvasPreviewOnlyBgContext
                .drawImage(
                        image,
                        0.0,
                        0.0,
                        image.width.toDouble(),
                        image.height.toDouble(),
                        0.0,
                        0.0,
                        800.0,
                        600.0
                )
    }

    fun openBackgroundInformation(result: UserInfoResult, background: Background, alreadyBought: Boolean, backgroundImg: BackgroundImage, profileWrapper: Image, fanArtArtists: List<FanArtArtist>) {
        val modal = TingleModal(
                TingleOptions(
                        footer = true,
                        cssClass = arrayOf("tingle-modal--overflow")
                )
        )

        modal.setContent(
                document.create.div(classes = "item-shop-preview") {
                    div {
                        style = "flex-grow: 1;"
                        h1 {
                            style = "word-break: break-word; text-align: center;"

                            +(locale["backgrounds.${background.internalName}.title"])
                        }
                        div {
                            style = "margin-bottom: 10px;"
                            +(locale["backgrounds.${background.internalName}.description"])

                            if (background.createdBy != null) {
                                val artists = fanArtArtists.filter { it.id in background.createdBy!! }
                                if (artists.isNotEmpty()) {
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
                        }
                        if (background.set != null) {
                            div {
                                i {
                                    locale.buildAsHtml(locale["website.dailyShop.partOfTheSet"], { num ->
                                        if (num == 0) {
                                            b {
                                                + (locale["sets.${background.set}"])
                                            }
                                        }
                                    }) { + it }
                                }
                            }
                        }
                    }

                    div(classes = "canvas-preview-wrapper") {
                        canvas("canvas-preview-only-bg") {
                            style = """width: 400px;"""
                            width = "800"
                            height = "600"
                        }

                        canvas("canvas-preview") {
                            style = """width: 400px;"""
                            width = "800"
                            height = "600"
                        }
                    }
                }
        )

        val cash = Audio("${loriUrl}assets/snd/css1_cash.wav")
        if (!alreadyBought) {
            val canBuy = result.profile.money >= background.rarity.getBackgroundPrice()
            val classes = if (canBuy) "button-discord-info" else "button-discord-disabled"
            modal.addFooterBtn("<i class=\"fas fa-gift\"></i> Comprar", "buy-button-modal button-discord $classes pure-button button-discord-modal") {
                if (canBuy) {
                    m.launch {
                        m.showLoadingScreen()
                        val response = sendItemPurchaseRequest("background", background.internalName)

                        if (response.status != HttpStatusCode.OK) {

                        }

                        visibleModal.select<HTMLElement>(".buy-button-modal")
                                .remove()

                        m.launch {
                            regen(false)
                            m.hideLoadingScreen()
                            cash.play()
                        }
                    }
                }
            }
        }

        modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
            modal.close()
        }

        modal.open()

        val openModal = visibleModal

        val canvasCheckout = visibleModal.select<HTMLCanvasElement>(".canvas-preview")
        val canvasCheckoutOnlyBg = visibleModal.select<HTMLCanvasElement>(".canvas-preview-only-bg")

        val canvasPreviewContext = (canvasCheckout.getContext("2d")!! as CanvasRenderingContext2D)
        val canvasPreviewOnlyBgContext = (canvasCheckoutOnlyBg.getContext("2d")!! as CanvasRenderingContext2D)

        if (backgroundImg is StaticBackgroundImage) {
            canvasPreviewContext
                    .drawImage(
                            backgroundImg.image,
                            (background.crop?.offsetX ?: 0).toDouble(),
                            (background.crop?.offsetY ?: 0).toDouble(),
                            (background.crop?.width ?: backgroundImg.image.width).toDouble(),
                            (background.crop?.height ?: backgroundImg.image.height).toDouble(),
                            0.0,
                            0.0,
                            800.0,
                            600.0
                    )
            canvasPreviewOnlyBgContext
                    .drawImage(
                            backgroundImg.image,
                            (background.crop?.offsetX ?: 0).toDouble(),
                            (background.crop?.offsetY ?: 0).toDouble(),
                            (background.crop?.width ?: backgroundImg.image.width).toDouble(),
                            (background.crop?.height ?: backgroundImg.image.height).toDouble(),
                            0.0,
                            0.0,
                            800.0,
                            600.0
                    )

            canvasPreviewContext.drawImage(profileWrapper, 0.0, 0.0)
        }
    }

    /**
     * Sends a daily shop item purchase request, asking to buy a [itemType] [internalName]
     *
     * @param itemType     what kind of item it is
     * @param internalName what is the internal name (ID) of the item
     * @return the http response
     */
    private suspend fun sendItemPurchaseRequest(itemType: String, internalName: String) = http.post<HttpResponse>("${loriUrl}api/v1/economy/daily-shop/buy/$itemType/$internalName") {
        body = "{}"
    }

    open class BackgroundImage

    class StaticBackgroundImage(val image: Image) : BackgroundImage()

    abstract class ShopItemWrapper {
        abstract val internalName: String
        abstract val rarity: Rarity
        abstract val tag: String?
        abstract val localePrefix: String?
        abstract val price: Int?

        /**
         * Checks if the user has already bought the item or not
         *
         * @return if the user already has the item
         */
        abstract fun hasBought(userInfoResult: UserInfoResult): Boolean
    }

    class BackgroundItemWrapper(background: Background) : ShopItemWrapper() {
        override val internalName = background.internalName
        override val rarity = background.rarity
        override val tag = background.tag
        override val localePrefix = "backgrounds"
        override val price = rarity.getBackgroundPrice()

        override fun hasBought(userInfoResult: UserInfoResult) = internalName in userInfoResult.backgrounds.map { it.internalName }
    }

    class ProfileDesignItemWrapper(profileDesign: ProfileDesign) : ShopItemWrapper() {
        override val internalName = profileDesign.internalName
        override val rarity = profileDesign.rarity
        override val tag = profileDesign.tag
        override val localePrefix = "profileDesigns"
        override val price = rarity.getProfilePrice()

        override fun hasBought(userInfoResult: UserInfoResult) = internalName in userInfoResult.profileDesigns.map { it.internalName }
    }
}