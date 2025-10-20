package net.perfectdreams.loritta.morenitta.website.views.dashboard.user

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedConfirmPurchaseModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class DailyShopView(
    lorittaWebsite: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    private val profile: Profile?,
    private val activeProfileDesignId: String,
    private val activeBackgroundId: String,
    private val shopId: Long,
    private val dailyShop: DailyShopResult,
    private val boughtBackgrounds: ProfileSectionsResponse.BackgroundsWrapper,
    private val boughtProfileDesigns: List<ProfileDesign>
) : ProfileDashboardView(
    lorittaWebsite,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    "daily-shop"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            id = "daily-shop"
            div(classes = "hero-wrapper") {
                etherealGambiImg("https://stuff.loritta.website/loritta-daily-shop-allouette.png", classes = "hero-image", sizes = "(max-width: 900px) 100vw, 360px") {}

                div(classes = "hero-text") {
                    h1 {
                        text(locale["website.dailyShop.title"])
                    }

                    p {
                        +"Bem-vind@ a loja diária de itens! O lugar para comprar itens para o seu \"+perfil\" da Loritta!"
                    }
                    p {
                        +"Todo o dia as 00:00 UTC (21:00 no horário do Brasil) a loja é atualizada com novos itens! Então volte todo o dia para verificar ^-^"
                    }
                }
            }

            div {
                // TODO - htmx-adventures: ad here
            }

            div {
                id = "daily-shop-items-with-timer-wrapper"

                div(classes = "shop-reset-timer") {
                    div(classes = "horizontal-line") {}

                    i(classes = "fas fa-stopwatch stopwatch") {}

                    div(classes = "shop-timer") {
                        div(classes = "shop-timer-date") {
                            id = "when-will-be-the-next-update"
                            attributes["data-component-mounter"] = "loritta-item-shop-timer"
                            attributes["loritta-item-shop-resets-at"] = getShopResetsEpochMilli().toString()
                            attributes["loritta-item-shop-i18n-hours"] = i18nContext.language.textBundle.strings[I18nKeys.Time.Hours.key]!!
                            attributes["loritta-item-shop-i18n-minutes"] = i18nContext.language.textBundle.strings[I18nKeys.Time.Minutes.key]!!
                            attributes["loritta-item-shop-i18n-seconds"] = i18nContext.language.textBundle.strings[I18nKeys.Time.Seconds.key]!!
                            text("...")
                        }
                        div(classes = "shop-timer-subtitle") {
                            +locale["website.dailyShop.untilTheNextShopUpdate"]
                        }
                    }
                }

                div {
                    // This is here instead of the parent div to avoid the timer inheriting these properties
                    attributes["hx-trigger"] = "refreshItemShop from:body"
                    attributes["hx-get"] = ""
                    attributes["hx-select"] = "#daily-shop-items-with-timer-wrapper"
                    attributes["hx-target"] = "#daily-shop-items-with-timer-wrapper"
                    attributes["hx-swap"] = "outerHTML"
                    id = "daily-shop-items-wrapper"

                    div(classes = "loritta-items-wrapper") {
                        // A list containing all of the items in the shop
                        // We are now going to sort it by rarity
                        val allItemsInTheShop = dailyShop.profileDesigns.map { ProfileDesignItemWrapper(it) } + dailyShop.backgrounds.map {
                            BackgroundItemWrapper(it)
                        }

                        val sortedByRarityAllItemsInTheShop = allItemsInTheShop
                            .sortedWith(compareByDescending(ShopItemWrapper::rarity).thenBy(ShopItemWrapper::internalName))

                        for (shopItem in sortedByRarityAllItemsInTheShop) {
                            val bought = when (shopItem) {
                                is BackgroundItemWrapper -> shopItem.hasBought(boughtBackgrounds)
                                is ProfileDesignItemWrapper -> shopItem.hasBought(boughtProfileDesigns)
                            }

                            div(classes = "shop-item-entry rarity-${shopItem.rarity.name.lowercase()}") {
                                when (shopItem) {
                                    is BackgroundItemWrapper -> {
                                        val buttons = mutableListOf<BUTTON.() -> (Unit)>(
                                            {
                                                classes += "no-background-theme-dependent-dark-text"

                                                closeModalOnClick()
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                                            }
                                        )

                                        if (!bought) {
                                            buttons.add {
                                                classes += "primary"
                                                openEmbeddedConfirmPurchaseModalOnClick(
                                                    i18nContext,
                                                    shopItem.price.toLong(),
                                                    profile?.money ?: 0L
                                                ) {
                                                    attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/daily-shop/buy"
                                                    attributes["hx-vals"] = buildJsonObject {
                                                        put("type", "background")
                                                        put("internalName", shopItem.internalName)
                                                    }.toString()
                                                    attributes["hx-swap"] = "none"
                                                }

                                                text("Comprar")
                                            }
                                        }

                                        openEmbeddedModalOnClick(
                                            locale["backgrounds.${shopItem.internalName}.title"],
                                            true,
                                            {
                                                div(classes = "loritta-item-preview-wrapper") {
                                                    div(classes = "canvas-preview-wrapper-wrapper") {
                                                        div(classes = "canvas-preview-wrapper") {
                                                            img(
                                                                classes = "canvas-preview-only-bg",
                                                                src = "/background/${shopItem.internalName}?profileDesign=${activeProfileDesignId}"
                                                            ) {
                                                                style = "width: 400px; aspect-ratio: 4/3;"
                                                            }

                                                            img(
                                                                classes = "canvas-preview",
                                                                src = "/api/v1/users/@me/profile?type=${activeProfileDesignId}&background=${shopItem.internalName}"
                                                            ) {
                                                                style = "width: 400px; aspect-ratio: 4/3;"
                                                            }
                                                        }
                                                    }

                                                    div {
                                                        style = "flex-grow: 1;"
                                                        p {
                                                            text(locale["backgrounds.${shopItem.internalName}.description"])
                                                        }

                                                        if (shopItem.background.set != null) {
                                                            div {
                                                                i {
                                                                    handleI18nString(
                                                                        locale["website.dailyShop.partOfTheSet"],
                                                                        {
                                                                            text(it)
                                                                        },
                                                                        { num ->
                                                                            if (num == "0") {
                                                                                TextReplaceControls.ComposableFunctionResult {
                                                                                    b {
                                                                                        text(locale["sets.${shopItem.background.set}"])
                                                                                    }
                                                                                }
                                                                            } else TextReplaceControls.AppendControlAsIsResult
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        val artists = lorittaWebsite.loritta.cachedGalleryOfDreamsDataResponse!!.artists
                                                            .filter { it.slug in shopItem.background.createdBy }

                                                        if (artists.isNotEmpty()) {
                                                            div {
                                                                b {
                                                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShop.MadeBy))
                                                                    text(" ")
                                                                }

                                                                for ((index, artist) in artists.withIndex()) {
                                                                    if (index != 0) {
                                                                        text(", ")
                                                                    }

                                                                    i {
                                                                        a(
                                                                            classes = "fan-arts",
                                                                            href = "https://fanarts.perfectdreams.net/pt/artists/${artist.slug}",
                                                                            target = "_blank"
                                                                        ) {
                                                                            text(artist.name)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            buttons
                                        )
                                    }

                                    is ProfileDesignItemWrapper -> {
                                        val buttons = mutableListOf<BUTTON.() -> (Unit)>(
                                            {
                                                classes += "no-background-theme-dependent-dark-text"

                                                closeModalOnClick()
                                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
                                            }
                                        )

                                        if (!bought) {
                                            buttons.add {
                                                classes += "primary"
                                                openEmbeddedConfirmPurchaseModalOnClick(
                                                    i18nContext,
                                                    shopItem.price.toLong(),
                                                    profile?.money ?: 0L
                                                ) {
                                                    attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/daily-shop/buy"
                                                    attributes["hx-vals"] = buildJsonObject {
                                                        put("type", "profile-design")
                                                        put("internalName", shopItem.internalName)
                                                    }.toString()
                                                    attributes["hx-swap"] = "none"
                                                }

                                                text("Comprar")
                                            }
                                        }

                                        openEmbeddedModalOnClick(
                                            locale["profileDesigns.${shopItem.internalName}.title"],
                                            true,
                                            {
                                                div(classes = "loritta-item-preview-wrapper") {
                                                    div(classes = "canvas-preview-wrapper-wrapper") {
                                                        div(classes = "canvas-preview-wrapper") {
                                                            img(
                                                                classes = "canvas-preview-profile-design",
                                                                src = "/api/v1/users/@me/profile?type=${shopItem.internalName}&background=${activeBackgroundId}"
                                                            ) {
                                                                style = "width: 400px; aspect-ratio: 4/3;"
                                                            }
                                                        }
                                                    }

                                                    div {
                                                        style = "flex-grow: 1;"
                                                        p {
                                                            text(locale["profileDesigns.${shopItem.internalName}.description"])
                                                        }

                                                        if (shopItem.profileDesign.set != null) {
                                                            div {
                                                                i {
                                                                    handleI18nString(
                                                                        locale["website.dailyShop.partOfTheSet"],
                                                                        {
                                                                            text(it)
                                                                        },
                                                                        { num ->
                                                                            if (num == "0") {
                                                                                TextReplaceControls.ComposableFunctionResult {
                                                                                    b {
                                                                                        text(locale["sets.${shopItem.profileDesign.set}"])
                                                                                    }
                                                                                }
                                                                            } else TextReplaceControls.AppendControlAsIsResult
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        val artists = lorittaWebsite.loritta.cachedGalleryOfDreamsDataResponse!!.artists
                                                            .filter { it.slug in shopItem.profileDesign.createdBy }

                                                        if (artists.isNotEmpty()) {
                                                            div {
                                                                b {
                                                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.DailyShop.MadeBy))
                                                                    text(" ")
                                                                }

                                                                for ((index, artist) in artists.withIndex()) {
                                                                    if (index != 0) {
                                                                        text(", ")
                                                                    }

                                                                    i {
                                                                        a(
                                                                            classes = "fan-arts",
                                                                            href = "https://fanarts.perfectdreams.net/pt/artists/${artist.slug}",
                                                                            target = "_blank"
                                                                        ) {
                                                                            text(artist.name)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            buttons
                                        )
                                    }
                                }

                                div {
                                    style = "position: relative;"

                                    div {
                                        style = "overflow: hidden; line-height: 0;"

                                        img {
                                            src = when (shopItem) {
                                                is BackgroundItemWrapper -> {
                                                    "/background/${shopItem.internalName}"
                                                }

                                                is ProfileDesignItemWrapper -> {
                                                    "/api/v1/users/@me/profile?type=${shopItem.internalName}&background=${activeBackgroundId}"
                                                }
                                            }

                                            // The aspect ratio makes the design not be wonky when the image is not loaded
                                            style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                                        }
                                    }

                                    div(classes = "item-entry-information rarity-${shopItem.rarity.name.lowercase()}") {
                                        div(classes = "item-entry-title rarity-${shopItem.rarity.name.lowercase()}") {
                                            +(locale["${shopItem.localePrefix}.${shopItem.internalName}.title"])
                                        }
                                        div(classes = "item-entry-type") {
                                            +locale["${shopItem.localePrefix}.name"]
                                        }
                                    }

                                    if (shopItem.tag != null) {
                                        div(classes = "item-new-tag") {
                                            +locale[shopItem.tag!!]
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
        }
    }

    private fun getShopResetsEpochMilli(): Long {
        val midnight = LocalTime.MIDNIGHT
        val today = LocalDate.now(ZoneOffset.UTC)
        val todayMidnight = LocalDateTime.of(today, midnight)
        val tomorrowMidnight = todayMidnight.plusDays(1)
        return tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    override fun getTitle() = locale["website.dailyShop.title"]

    sealed class ShopItemWrapper {
        abstract val internalName: String
        abstract val rarity: Rarity
        abstract val tag: String?
        abstract val localePrefix: String?
        abstract val price: Int?
        abstract val set: String?
        abstract val createdBy: List<String>?
    }

    class BackgroundItemWrapper(backgroundEntry: DailyShopBackgroundEntry) : ShopItemWrapper() {
        val background = backgroundEntry.backgroundWithVariations.background
        val variations = backgroundEntry.backgroundWithVariations.variations
        override val internalName = background.id
        override val rarity = background.rarity
        override val tag = backgroundEntry.tag
        override val localePrefix = "backgrounds"
        override val price = rarity.getBackgroundPrice()
        override val set = backgroundEntry.backgroundWithVariations.background.set
        override val createdBy = backgroundEntry.backgroundWithVariations.background.createdBy

        /**
         * Checks if the user has already bought the item or not
         *
         * @return if the user already has the item
         */
        fun hasBought(backgroundsWrapper: ProfileSectionsResponse.BackgroundsWrapper) = backgroundsWrapper.backgrounds.any { it.background.id == internalName }
    }

    class ProfileDesignItemWrapper(val profileDesign: ProfileDesign) : ShopItemWrapper() {
        override val internalName = profileDesign.internalName
        override val rarity = profileDesign.rarity
        override val tag = profileDesign.tag
        override val localePrefix = "profileDesigns"
        override val price = rarity.getProfilePrice()
        override val set = profileDesign.set
        override val createdBy = profileDesign.createdBy

        /**
         * Checks if the user has already bought the item or not
         *
         * @return if the user already has the item
         */
        fun hasBought(profileDesigns: List<ProfileDesign>) = profileDesigns.any { it.internalName == internalName }
    }
}