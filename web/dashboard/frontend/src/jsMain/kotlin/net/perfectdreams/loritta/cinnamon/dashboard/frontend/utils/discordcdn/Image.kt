package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn

// From Kord
object Image {
    public sealed class Format(public val extensions: List<String>) {
        protected constructor(vararg extensions: String) : this(extensions.toList())

        public val extension: String get() = extensions.first()

        public object JPEG : Format("jpeg", "jpg")
        public object PNG : Format("png")
        public object WEBP : Format("webp")
        public object GIF : Format("gif")

        public companion object {
            public val values: Set<Format>
                get() = setOf(
                    JPEG,
                    PNG,
                    WEBP,
                    GIF,
                )

            public fun isSupported(fileName: String): Boolean {
                return values.any {
                    it.extensions.any { extension -> fileName.endsWith(extension, true) }
                }
            }

            public fun fromContentType(type: String): Format = when (type) {
                "image/jpeg" -> JPEG
                "image/png" -> PNG
                "image/webp" -> WEBP
                "image/gif" -> GIF
                else -> error(type)
            }
        }
    }

    /**
     * Represents size of the [Image], for requesting different sizes of Image from the Discord.
     * Both height and width of the [Image] will always be smaller than or equal to [maxRes] of the [Size].
     */
    public enum class Size(public val maxRes: Int) {
        Size16(16),
        Size32(32),
        Size64(64),
        Size128(128),
        Size256(256),
        Size512(512),
        Size1024(1024),
        Size2048(2048),
        Size4096(4096),
    }
}