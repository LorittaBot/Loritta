package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.galleryofdreams.common.data.api.GalleryOfDreamsDataResponse
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.DailyShopView.BackgroundItemWrapper
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.DailyShopView.ProfileDesignItemWrapper
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.DailyShopView.ShopItemWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.dailyshop.DailyShopResult
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmPurchaseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick

fun FlowContent.dailyShopItems(
    i18nContext: I18nContext,
    locale: BaseLocale,
    dailyShop: DailyShopResult,
    cachedGalleryOfDreamsDataResponse: GalleryOfDreamsDataResponse
) {
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
                is BackgroundItemWrapper -> shopItem.hasBought(dailyShop.backgroundsWrapper)
                is ProfileDesignItemWrapper -> shopItem.hasBought(dailyShop.boughtProfileDesigns)
            }

            val itemName = locale["${shopItem.localePrefix}.${shopItem.internalName}.title"]

            // By default, the "close" button will ALWAYS show up, while the "buy" button will only show up if we haven't bought it
            val buttons = mutableListOf<FlowContent.() -> (Unit)>(
                {
                    defaultModalCloseButton(i18nContext)
                }
            )

            if (!bought) {
                buttons.add {
                    discordButton(ButtonStyle.PRIMARY) {
                        openModalOnClick(
                            createEmbeddedConfirmPurchaseModal(
                                i18nContext,
                                shopItem.price?.toLong() ?: 0,
                                dailyShop.profile?.money ?: 0L
                            ) {
                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/daily-shop/buy"
                                attributes["bliss-vals-json"] = buildJsonObject {
                                    when (shopItem) {
                                        is BackgroundItemWrapper -> put("type", "background")
                                        is ProfileDesignItemWrapper -> put("type", "profileDesign")
                                    }
                                    put("internalName", shopItem.internalName)
                                }.toString()
                                attributes["bliss-swap:200"] = "body (innerHTML) -> #loritta-items-wrapper (innerHTML)"
                            }
                        )
                        text(i18nContext.get(DashboardI18nKeysData.PurchaseModal.Buy))
                    }
                }
            }

            val modal = createEmbeddedModal(
                itemName,
                true,
                {
                    div(classes = "loritta-item-preview-wrapper") {
                        div(classes = "canvas-preview-wrapper-wrapper") {
                            div(classes = "canvas-preview-wrapper") {
                                when (shopItem) {
                                    is BackgroundItemWrapper -> {
                                        div(classes = "canvas-preview-only-bg") {
                                            img(src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/background-preview/${shopItem.internalName}?profileDesign=${dailyShop.activeProfileDesignId}") {
                                                style = "width: 400px; aspect-ratio: 4/3;"
                                            }
                                        }

                                        div(classes = "canvas-preview") {
                                            img(src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${dailyShop.activeProfileDesignId}&background=${shopItem.internalName}") {
                                                style = "width: 400px; aspect-ratio: 4/3;"
                                            }
                                        }
                                    }
                                    is ProfileDesignItemWrapper -> {
                                        div(classes = "canvas-preview-profile-design") {
                                            img(src = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${shopItem.internalName}&background=${dailyShop.activeBackgroundId}") {
                                                style = "width: 400px; aspect-ratio: 4/3;"
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        div {
                            style = "flex-grow: 1;"
                            p {
                                when (shopItem) {
                                    is BackgroundItemWrapper -> text(locale["backgrounds.${shopItem.internalName}.description"])
                                    is ProfileDesignItemWrapper -> text(locale["profileDesigns.${shopItem.internalName}.description"])
                                }
                            }

                            if (shopItem.set != null) {
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
                                                            text(locale["sets.${shopItem.set}"])
                                                        }
                                                    }
                                                } else TextReplaceControls.AppendControlAsIsResult
                                            }
                                        )
                                    }
                                }
                            }

                            val createdBy = shopItem.createdBy
                            if (createdBy != null) {
                                val artists = cachedGalleryOfDreamsDataResponse.artists
                                    .filter { it.slug in createdBy }

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
                    }
                },
                buttons
            )

            div(classes = "shop-item-entry rarity-${shopItem.rarity.name.lowercase()}") {
                openModalOnClick(modal)

                div {
                    style = "position: relative;"

                    div {
                        style = "overflow: hidden; line-height: 0;"

                        img {
                            src = when (shopItem) {
                                is BackgroundItemWrapper -> {
                                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/background-preview/${shopItem.internalName}"
                                }

                                is ProfileDesignItemWrapper -> {
                                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/profile-preview?type=${shopItem.internalName}&background=${dailyShop.activeBackgroundId}"
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
                        svgIcon(SVGIcons.CheckFat)
                        span {
                            text(locale["website.dailyShop.itemAlreadyBought"])
                        }
                    } else {
                        +"${shopItem.price} Sonhos"
                    }
                }
            }
        }
    }
}