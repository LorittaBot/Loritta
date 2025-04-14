package net.perfectdreams.loritta.website.backend.utils

import kotlinx.html.DIV
import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlinx.html.id

object NitroPayAdGenerator {
    const val SIDEBAR_AD_MEDIA_QUERY = "(min-height: 642px) and (min-width: 900px)"
    const val DESKTOP_LARGE_AD_MEDIA_QUERY = "(min-width: 1900px)"
    const val DESKTOP_AD_MEDIA_QUERY = "(min-width: 1025px) and (max-width: 1900px)"
    const val PHONE_AD_MEDIA_QUERY = "(min-width: 320px) and (max-width: 767px)"
    const val TABLET_AD_MEDIA_QUERY = "(min-width: 768px) and (max-width: 1024px)"

    const val FULL_WIDTH_CONTENT_DESKTOP_MEDIA_QUERY = "(min-width: 1025px)"
    const val FULL_WIDTH_CONTENT_PHONE_MEDIA_QUERY = "(min-width: 320px) and (max-width: 767px)"
    const val FULL_WIDTH_CONTENT_TABLET_MEDIA_QUERY = "(min-width: 768px) and (max-width: 1024px)"

    const val RIGHT_SIDEBAR_DESKTOP_MEDIA_QUERY = "(min-width: 1240px) and (max-width: 1900px)"
    const val RIGHT_SIDEBAR_PHONE_MEDIA_QUERY = "(max-width: 1239px)"

    val ALL_SIZES_EXCEPT_PHONES = (NitroPayAdDisplay.values().toMutableList() - NitroPayAdDisplay.PHONE)
    val ALL_SIZES = NitroPayAdDisplay.values().toList()
}

data class NitroPayAdSize(
        val width: Int,
        val height: Int
)

fun DIV.generateNitroPayAd(adSlot: String, displayTypes: List<NitroPayAdDisplay>) {
    if (NitroPayAdDisplay.DESKTOP in displayTypes) {
        generateNitroPayAd(
            "$adSlot-desktop",
            listOf(
                NitroPayAdSize(
                    728,
                    90
                ),
                NitroPayAdSize(
                    970,
                    90
                ),
                NitroPayAdSize(
                    970,
                    250
                )
            ),
            NitroPayAdGenerator.FULL_WIDTH_CONTENT_DESKTOP_MEDIA_QUERY
        )
    }

    if (NitroPayAdDisplay.PHONE in displayTypes) {
        generateNitroPayAd(
            "$adSlot-phone",
            listOf(
                NitroPayAdSize(
                    300,
                    250
                ),
                NitroPayAdSize(
                    320,
                    50
                )
            ),
            NitroPayAdGenerator.FULL_WIDTH_CONTENT_PHONE_MEDIA_QUERY
        )
    }

    if (NitroPayAdDisplay.TABLET in displayTypes) {
        generateNitroPayAd(
            "$adSlot-tablet",
            listOf(
                NitroPayAdSize(
                    728,
                    90
                ),
                NitroPayAdSize(
                    970,
                    90
                ),
                NitroPayAdSize(
                    970,
                    250
                ),
                NitroPayAdSize(
                    300,
                    250
                ),
                NitroPayAdSize(
                    320,
                    50
                )
            ),
            NitroPayAdGenerator.FULL_WIDTH_CONTENT_TABLET_MEDIA_QUERY
        )
    }
}

fun HtmlBlockTag.generateNitroPayAd(
        adId: String,
        adSizes: List<NitroPayAdSize>,
        mediaQuery: String? = null
) {
    div(classes = "centralized-ad") {
        div(classes = "nitropay-ad") {
            id = adId

            attributes["data-nitropay-ad-type"] = "standard_banner"
            attributes["data-nitropay-ad-sizes"] = adSizes.joinToString(", ") { "${it.width}x${it.height}" }
            if (mediaQuery != null)
                attributes["data-nitropay-media-query"] = mediaQuery
        }
    }
}