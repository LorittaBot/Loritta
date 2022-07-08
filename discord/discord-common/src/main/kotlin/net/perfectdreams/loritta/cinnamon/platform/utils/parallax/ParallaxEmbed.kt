package net.perfectdreams.loritta.cinnamon.platform.utils.parallax

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParallaxEmbed(
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val image: ParallaxImage? = null,
    val thumbnail: ParallaxThumbnail? = null,
    val author: ParallaxAuthor? = null,
    val footer: ParallaxFooter? = null,
    val fields: List<ParallaxField> = listOf(),
    val color: Int? = null,
) {
    @Serializable
    data class ParallaxImage(
        val url: String
    )

    @Serializable
    data class ParallaxThumbnail(
        val url: String
    )

    @Serializable
    data class ParallaxAuthor(
        val name: String,
        val url: String? = null,
        @SerialName("icon_url")
        val iconUrl: String? = null
    )

    @Serializable
    data class ParallaxFooter(
        val text: String,
        @SerialName("icon_url")
        val iconUrl: String? = null
    )

    @Serializable
    data class ParallaxField(
        val name: String,
        val value: String,
        val inline: Boolean = false
    )
}