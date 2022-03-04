package net.perfectdreams.showtime.backend.utils

import kotlinx.html.HtmlBlockTag
import kotlinx.html.div
import kotlinx.html.id

object NitroPayAdGenerator {
    const val SIDEBAR_AD_MEDIA_QUERY = "(min-height: 642px) and (min-width: 900px)"
    const val DESKTOP_LARGE_AD_MEDIA_QUERY = "(min-width: 1900px)"
    const val DESKTOP_AD_MEDIA_QUERY = "(min-width: 1025px) and (max-width: 1900px)"
    const val PHONE_AD_MEDIA_QUERY = "(min-width: 320px) and (max-width: 767px)"
    const val TABLET_AD_MEDIA_QUERY = "(min-width: 768px) and (max-width: 1024px)"

    const val RIGHT_SIDEBAR_DESKTOP_MEDIA_QUERY = "(min-width: 1240px) and (max-width: 1900px)"
    const val RIGHT_SIDEBAR_PHONE_MEDIA_QUERY = "(max-width: 1239px)"
}

data class NitroPayAdSize(
        val width: Int,
        val height: Int
)

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