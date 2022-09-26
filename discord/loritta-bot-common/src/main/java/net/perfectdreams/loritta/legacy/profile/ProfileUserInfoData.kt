package net.perfectdreams.loritta.legacy.profile

data class ProfileUserInfoData(
		val id: Long,
		val name: String,
		val discriminator: String,
		val avatarUrl: String
)