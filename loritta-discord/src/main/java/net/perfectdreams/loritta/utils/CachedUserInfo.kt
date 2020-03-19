package net.perfectdreams.loritta.utils

class CachedUserInfo(
		val id: Long,
		val name: String,
		val discriminator: String,
		val avatarId: String?
) {
	val avatarUrl: String?
		get() {
			return if (avatarId != null) {
				val extension = if (avatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
					"gif"
				} else { "png" }

				"https://cdn.discordapp.com/avatars/${id}/${avatarId}.${extension}?size=256"
			} else null
		}

	val defaultAvatarUrl: String
		get() {
			val avatarId = id % 5

			return "https://cdn.discordapp.com/embed/avatars/$avatarId.png?size=256"
		}

	val effectiveAvatarUrl: String
		get() {
			return avatarUrl ?: defaultAvatarUrl
		}
}