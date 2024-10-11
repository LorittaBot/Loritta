package net.perfectdreams.loritta.morenitta.utils

class CachedUserInfo(
	val id: Long,
	val name: String,
	val discriminator: String,
	val globalName: String?,
	val avatarId: String?
) {
	val avatarUrl: String?
		get() {
			return if (avatarId != null) {
				val extension = if (avatarId.startsWith("a_")) { // Avatares animados no Discord começam com "_a"
					"gif"
				} else { "png" }

				"https://cdn.discordapp.com/avatars/${id}/${avatarId}.${extension}"
			} else null
		}

	val defaultAvatarUrl: String
		get() {
			val avatarId = id % 5

			return "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
		}

	val effectiveAvatarUrl: String
		get() {
			return avatarUrl ?: defaultAvatarUrl
		}

	/**
	 * Gets the effective avatar URL in the specified [format]
	 *
	 * @see getEffectiveAvatarUrl
	 */
	fun getEffectiveAvatarUrl(format: ImageFormat) = getEffectiveAvatarUrl(format, 128)

	/**
	 * Gets the effective avatar URL in the specified [format] and [ímageSize]
	 *
	 * @see getEffectiveAvatarUrlInFormat
	 */
	fun getEffectiveAvatarUrl(format: ImageFormat, imageSize: Int): String {
		val extension = format.extension

		return if (avatarId != null) {
			"https://cdn.discordapp.com/avatars/$id/$avatarId.${extension}?size=$imageSize"
		} else {
			val avatarId = id % 5
			// This only exists in png AND doesn't have any other sizes
			"https://cdn.discordapp.com/embed/avatars/$avatarId.png"
		}
	}
}