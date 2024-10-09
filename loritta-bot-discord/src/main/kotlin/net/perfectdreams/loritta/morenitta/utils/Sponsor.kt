package net.perfectdreams.loritta.morenitta.utils

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement

data class Sponsor(
		val name: String,
		val slug: String,
		val paid: Double,
		val link: String,
		val banners: JsonElement
) {
	fun getRectangularBannerUrl(): String {
		val bannerAsArray = banners.array
		return bannerAsArray.first { it["type"].string == "pc" }["url"].string
	}

	fun getSquareBannerUrl(): String {
		val bannerAsArray = banners.array
		return bannerAsArray.firstOrNull { it["type"].string == "mobile" }?.get("url")?.string ?: getRectangularBannerUrl()
	}
}