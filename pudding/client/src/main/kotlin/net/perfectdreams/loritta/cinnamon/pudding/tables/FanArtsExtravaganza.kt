package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FanArtsExtravaganza : LongIdTable() {
	val fanArtAvatarImageUrl = text("fan_art_avatar_image_url")
	val artistName = text("artist_name").nullable()
	val artistId = text("artist_id").nullable() // GalleryOfDreams slug
	val enabled = bool("enabled").index()
	val defaultAvatar = bool("default_avatar").index()
	val active = bool("active").index()
}