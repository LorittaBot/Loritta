package net.perfectdreams.loritta.morenitta.website.views.user

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Rarity
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedConfirmPurchaseModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.views.ProfileDashboardView
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class DailyShopView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    private val profile: Profile?,
    private val activeBackgroundId: String,
    private val shopId: Long,
    private val dailyShop: DailyShopResult,
    private val boughtBackgrounds: ProfileSectionsResponse.BackgroundsWrapper,
    private val boughtProfileDesigns: List<ProfileDesign>
) : ProfileDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    "daily-shop"
) {
    override fun DIV.generateRightSidebarContents() {
        div {
            id = "daily-shop"
            div(classes = "hero-wrapper") {
                img(classes = "hero-image") {
                    src = "https://stuff.loritta.website/loritta-daily-shop-allouette.png"
                }

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
                            attributes["loritta-item-shop-timer"] = "true"
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
                                                    attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/user/@me/dashboard/daily-shop/buy"
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
                                                    div(classes = "canvas-preview-wrapper") {
                                                        img(classes = "canvas-preview-only-bg", src = "/background/${shopItem.internalName}") {
                                                            style = "width: 400px; aspect-ratio: 4/3;"
                                                        }

                                                        img(classes = "canvas-preview", src = "/api/v1/users/@me/profile") {
                                                            style = "width: 400px; aspect-ratio: 4/3;"
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
                                                    attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/user/@me/dashboard/daily-shop/buy"
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
                                                    div(classes = "canvas-preview-wrapper") {
                                                        img(classes = "canvas-preview-only-bg", src = "/api/v1/users/@me/profile?type=${shopItem.internalName}&background=${activeBackgroundId}") {
                                                            style = "width: 400px; aspect-ratio: 4/3;"
                                                        }

                                                        img(classes = "canvas-preview", src = "/api/v1/users/@me/profile?type=${shopItem.internalName}&background=${activeBackgroundId}") {
                                                            style = "width: 400px; aspect-ratio: 4/3;"
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
                                                    "/api/v1/users/@me/profile?type=${shopItem.internalName}"
                                                }
                                            }

                                            // The aspect ratio makes the design not be wonky when the image is not loaded
                                            style = "width: 100%; height: auto; aspect-ratio: 4/3;"
                                        }
                                    }

                                    div(classes = "item-entry-information rarity-${shopItem.rarity.name.toLowerCase()}") {
                                        div(classes = "item-entry-title rarity-${shopItem.rarity.name.toLowerCase()}") {
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

    fun getShopResetsEpochMilli(): Long {
        val midnight = LocalTime.MIDNIGHT
        val today = LocalDate.now(ZoneOffset.UTC)
        val todayMidnight = LocalDateTime.of(today, midnight)
        val tomorrowMidnight = todayMidnight.plusDays(1)
        return tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    inline fun FlowContent.lorittaBackgroundPreview(backgroundVariation: BackgroundVariation, crossinline block : DIV.() -> Unit = {}) {
        div {
            attributes["loritta-background-preview"] = ""
            attributes["loritta-background-variation"] = EmbeddedSpicyModalUtils.encodeURIComponent(Json.encodeToString(backgroundVariation))
            apply(block)
        }
    }

    override fun getTitle() = locale["website.dailyShop.title"]

    sealed class ShopItemWrapper {
        abstract val internalName: String
        abstract val rarity: Rarity
        abstract val tag: String?
        abstract val localePrefix: String?
        abstract val price: Int?
    }

    class BackgroundItemWrapper(backgroundEntry: DailyShopBackgroundEntry) : ShopItemWrapper() {
        val background = backgroundEntry.backgroundWithVariations.background
        val variations = backgroundEntry.backgroundWithVariations.variations
        override val internalName = background.id
        override val rarity = background.rarity
        override val tag = backgroundEntry.tag
        override val localePrefix = "backgrounds"
        override val price = rarity.getBackgroundPrice()

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

        /**
         * Checks if the user has already bought the item or not
         *
         * @return if the user already has the item
         */
        fun hasBought(profileDesigns: List<ProfileDesign>) = profileDesigns.any { it.internalName == internalName }
    }
}