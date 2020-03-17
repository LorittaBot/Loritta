package com.mrpowergamerbr.loritta.profile

data class ProfileUserInfoData(
		val id: Long,
		val name: String,
		val discriminator: String,
		val avatarUrl: String
)