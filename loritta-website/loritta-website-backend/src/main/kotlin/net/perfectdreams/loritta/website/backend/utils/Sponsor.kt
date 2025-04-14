package net.perfectdreams.loritta.website.backend.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.website.backend.utils.serialization.jsonObjectArray

@Serializable
data class Sponsor(
    val name: String,
    val slug: String,
    val paid: Double,
    val link: String,
    val banners: JsonElement
) {
    fun getRectangularBannerUrl(): String {
        val bannerAsArray = banners.jsonObjectArray
        return bannerAsArray.first { it["type"]!!.jsonPrimitive.content == "pc" }["url"]!!.jsonPrimitive.content
    }

    fun getSquareBannerUrl(): String {
        val bannerAsArray = banners.jsonObjectArray
        return bannerAsArray.firstOrNull { it["type"]!!.jsonPrimitive.content == "mobile" }?.get("url")?.jsonPrimitive?.content ?: getRectangularBannerUrl()
    }
}