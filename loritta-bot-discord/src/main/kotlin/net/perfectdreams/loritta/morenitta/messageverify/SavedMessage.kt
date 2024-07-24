package net.perfectdreams.loritta.morenitta.messageverify

import kotlinx.serialization.Serializable

/**
 * A saved message
 *
 * While this is similar to Discord's JSON message format, it isn't identical, and that's a good thing, because we want to store other things!
 */
@Serializable
data class SavedMessage(
    val id: Long,
    val channelId: Long,
    val guildId: Long?,
    val guild: SavedGuild?,
    val author: SavedAuthor,
    val content: String,
    val embeds: List<SavedEmbed>,
    val attachments: List<SavedAttachment>
) {
    @Serializable
    data class SavedGuild(
        val id: Long,
        val name: String,
        val iconId: String?
    )

    @Serializable
    data class SavedAuthor(
        val id: Long,
        val name: String,
        val discriminator: String,
        val globalName: String?,
        val avatarId: String?,
        val isBot: Boolean,
        val isSystem: Boolean,
        val flags: Int
    )

    @Serializable
    data class SavedEmbed(
        val title: String?,
        val description: String?,
        val url: String?,
        val color: Int?,
        val author: SavedAuthor?,
        val fields: List<SavedField>,
        val footer: SavedFooter?,
        val image: SavedImage?,
        val thumbnail: SavedThumbnail?,
    ) {
        @Serializable
        data class SavedAuthor(
            val name: String?,
            val url: String?,
            val iconUrl: String?,
            val proxyIconUrl: String?
        )

        @Serializable
        data class SavedField(
            val name: String?,
            val value: String?,
            val isInline: Boolean
        )

        @Serializable
        data class SavedThumbnail(
            val url: String?,
            val proxyUrl: String?,
            val width: Int,
            val height: Int
        )

        @Serializable
        data class SavedImage(
            val url: String?,
            val proxyUrl: String?,
            val width: Int,
            val height: Int
        )

        @Serializable
        data class SavedFooter(
            val text: String?,
            val iconUrl: String?,
            val proxyIconUrl: String?
        )
    }

    @Serializable
    data class SavedAttachment(
        val id: Long,
        val fileName: String,
        val description: String?,
        val contentType: String?,
        val size: Int,
        val url: String,
        val proxyUrl: String?,
        val width: Int?,
        val height: Int?,
        val ephemeral: Boolean,
        val duration: Double,
        val waveformBase64: String?,
        val dataBase64: String?,
    )
}