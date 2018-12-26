package net.perfectdreams.loritta.api.platform

enum class PlatformType(val supportedFeatures: Array<PlatformFeature>) {
	DISCORD(
			PlatformFeature.values()
	),
	TWITTER(
			arrayOf(
					PlatformFeature.FILE_UPLOAD,
					PlatformFeature.IMAGE_UPLOAD,
					PlatformFeature.EMBED_LINKS
			)
	),
	TELEGRAM(
			arrayOf(
					PlatformFeature.FILE_UPLOAD,
					PlatformFeature.IMAGE_UPLOAD,
					PlatformFeature.INLINE_KEYBOARD,
					PlatformFeature.EMBED_LINKS,
					PlatformFeature.TYPING_STATUS
			)
	),
	AMINO(
			arrayOf(
					PlatformFeature.FILE_UPLOAD,
					PlatformFeature.IMAGE_UPLOAD,
					PlatformFeature.EMBED_LINKS,
					PlatformFeature.TYPING_STATUS
			)
	),
	MINECRAFT(
			arrayOf( // Tecnicamente...?
					PlatformFeature.IMAGE_UPLOAD,
					PlatformFeature.EMBED_LINKS
			)
	),
	UNKNOWN(
			arrayOf()
	);

	fun supports(vararg features: PlatformFeature): Boolean {
		return this.supportedFeatures.toMutableSet().containsAll(features.toMutableSet())
	}
}