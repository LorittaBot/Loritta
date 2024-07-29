package net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.GroupChannel
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.dv8tion.jda.api.entities.sticker.Sticker.StickerFormat
import net.dv8tion.jda.api.utils.TimeUtil
import net.dv8tion.jda.internal.utils.Helpers
import net.perfectdreams.loritta.discordchatmessagerenderer.ImageFormat
import java.awt.Color
import java.time.OffsetDateTime
import java.util.*

interface DiscordSnowflakeEntity {
    val id: Long

    val timeCreated: OffsetDateTime
        get() = TimeUtil.getTimeCreated(id)
}

/**
 * A saved message
 *
 * While this is similar to Discord's JSON message format, it isn't identical, and that's a good thing, because we want to store other things!
 */
@Serializable
data class SavedMessage(
    override val id: Long,
    /**
     * In what "place" the message was sent in, a place may be a guild, a private group channel, a private channel, so on and so forth
     *
     * We don't split up into channel + guilds because this is a single message, on a single channel
     */
    val placeContext: SavedPlaceContext,
    val author: SavedUser,
    val member: SavedMember?,
    val timeEdited: Instant?,
    val content: String,
    val embeds: List<SavedEmbed>,
    val attachments: List<SavedAttachment>,
    val stickers: List<SavedSticker>,
    val mentions: SavedMentions,
    val reactions: List<SavedReaction>
) : DiscordSnowflakeEntity {
    val isEdited = timeEdited != null
}

@Serializable
data class SavedMentions(
    val users: List<SavedUser>,
    val roles: List<SavedRole>
)

@Serializable
sealed class SavedChannel {
    abstract val id: Long
}

@Serializable
sealed class SavedPlaceContext

@Serializable
sealed class SavedGuild : SavedPlaceContext(), DiscordSnowflakeEntity {
    abstract override val id: Long
    abstract val channelId: Long
    abstract val channelName: String
    abstract val channelType: ChannelType
}

@Serializable
@SerialName("attached_guild")
data class SavedAttachedGuild(
    override val id: Long,
    override val channelId: Long,
    override val channelName: String,
    override val channelType: ChannelType,
    val name: String,
    val iconId: String?
) : SavedGuild(), DiscordSnowflakeEntity {
    /**
     * Gets the guild's icon URL in the specified [format] and [ímageSize]
     *
     * @see getEffectiveAvatarUrlInFormat
     */
    fun getIconUrl(size: Int, format: ImageFormat): String? {
        val iconId = this.iconId ?: return null
        return String.format(Guild.ICON_URL, this.id, iconId, format.extension)
    }
}

@Serializable
@SerialName("detached_guild")
data class SavedDetachedGuild(
    override val id: Long,
    override val channelId: Long,
    override val channelName: String,
    override val channelType: ChannelType,
) : SavedGuild(), DiscordSnowflakeEntity

@Serializable
@SerialName("private_channel")
data class SavedPrivateChannel(override val id: Long) : SavedPlaceContext(), DiscordSnowflakeEntity

@Serializable
@SerialName("group_channel")
data class SavedGroupChannel(
    override val id: Long,
    val name: String,
    val iconId: String?
) : SavedPlaceContext(), DiscordSnowflakeEntity {
    /**
     * The URL of the group channel icon image.
     * If no icon has been set, this returns `null`.
     *
     * @return Possibly-null String containing the group channel's icon URL.
     */
    fun getIconUrl(size: Int): String? {
        val iconId = this.iconId
        return if (iconId == null) null else String.format(GroupChannel.ICON_URL, id, iconId)
    }
}

@Serializable
data class SavedUser(
    override val id: Long,
    val name: String,
    val discriminator: String,
    val globalName: String?,
    val avatarId: String?,
    val isBot: Boolean,
    val isSystem: Boolean,
    val flagsRaw: Int
) : DiscordSnowflakeEntity {
    val flags: EnumSet<User.UserFlag>
        get() = User.UserFlag.getFlags(flagsRaw)
    val effectiveName: String
        get() = globalName ?: name

    /**
     * Gets the effective avatar URL in the specified [format] and [imageSize]
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

@Serializable
data class SavedMember(
    val nickname: String?,
    val roles: List<SavedRole>,
) {
    val colorRaw: Int
        get() {
            for (r in roles) {
                val colorRaw = r.colorRaw
                if (colorRaw != Role.DEFAULT_COLOR_RAW) return colorRaw
            }
            return Role.DEFAULT_COLOR_RAW
        }

    val color: Color?
        get() {
            val raw: Int = colorRaw
            return if (raw != Role.DEFAULT_COLOR_RAW) Color(raw) else null
        }

    val iconUrl: String?
        get() {
            for (r in roles) {
                return r.iconUrl ?: continue
            }
            return null
        }
}

@Serializable
data class SavedRole(
    override val id: Long,
    val name: String,
    val colorRaw: Int,
    val icon: SavedRoleIcon?
) : DiscordSnowflakeEntity {
    val iconUrl: String?
        get() {
            val icon = this.icon
            return if (icon is SavedCustomRoleIcon) {
                String.format(RoleIcon.ICON_URL, id, icon.iconId)
            } else null
        }
}

@Serializable
sealed class SavedRoleIcon

@Serializable
@SerialName("custom_icon")
data class SavedCustomRoleIcon(
    val iconId: String
) : SavedRoleIcon()

@Serializable
@SerialName("unicode_icon")
data class SavedUnicodeRoleIcon(
    val emoji: String
) : SavedRoleIcon()

@Serializable
data class SavedEmbed(
    val type: EmbedType,
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
    val waveformBase64: String?
)

@Serializable
data class SavedSticker(
    val id: Long,
    val formatType: StickerFormat,
    val name: String,
) {
    companion object {
        // We use the media.discordapp.net instead of using JDA's Sticker.ICON_URL because the media proxy allows us to change GIF stickers
        // into PNG stickers
        private const val ICON_URL = "https://media.discordapp.net/stickers/%s.%s"
    }

    val iconUrl: String
        get() = Helpers.format(ICON_URL, id, formatType.getExtension()) + "?passthrough=false"
}

@Serializable
data class SavedReaction(
    val countNormal: Int,
    val countBurst: Int,
    val emoji: SavedPartialEmoji
) {
    val count
        get() = countNormal + countBurst
}

@Serializable
sealed class SavedPartialEmoji

@Serializable
@SerialName("custom_emoji")
class SavedCustomPartialEmoji(
    override val id: Long,
    val name: String,
    val isAnimated: Boolean
) : SavedPartialEmoji(), DiscordSnowflakeEntity

@Serializable
@SerialName("unicode_emoji")
class SavedUnicodePartialEmoji(
    val name: String
) : SavedPartialEmoji()