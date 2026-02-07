package net.perfectdreams.loritta.morenitta.utils

class CachedUserInfo(
	val id: Long,
	val name: String,
	val discriminator: String,
	val globalName: String?,
	val avatarId: String?
) {
	val avatarUrl: String?
		get() = this.avatarId?.let { DiscordCDNUtils.getAvatarUrl(this.id, this.avatarId, null, null) }

	val defaultAvatarUrl: String
		get() = DiscordCDNUtils.getDefaultAvatarUrl(this.id)

	val effectiveAvatarUrl: String
		get() = DiscordCDNUtils.getEffectiveAvatarUrl(this.id, this.avatarId, null, null)

	/**
	 * Gets the effective avatar URL in the specified [format]
	 *
	 * @see getEffectiveAvatarUrl
	 */
	fun getEffectiveAvatarUrl(format: ImageFormat) = getEffectiveAvatarUrl(format, 128)

	/**
	 * Gets the effective avatar URL in the specified [format] and [imageSize]
	 */
	fun getEffectiveAvatarUrl(format: ImageFormat, imageSize: Int) = DiscordCDNUtils.getEffectiveAvatarUrl(this.id, this.avatarId, format, imageSize)
}