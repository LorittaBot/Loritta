package net.perfectdreams.loritta.morenitta.utils.devious

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.*
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion
import net.dv8tion.jda.internal.entities.channel.concrete.ThreadChannelImpl
import net.dv8tion.jda.internal.entities.channel.mixin.attribute.IPermissionContainerMixin
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DeviousConverter {
    const val CACHE_VERSION = 1
    val INITIAL_SESSION_ID = UUID.fromString("07c70756-adfc-4229-b20d-2039f34bd146")
    val GATEWAY_EXTRAS_ID = UUID.fromString("6a8702f0-4c50-4875-8555-4aee0609184d")

    private val parentChannelField = ThreadChannelImpl::class.java.getDeclaredField("parentChannel")
        .apply {
            this.isAccessible = true
        }

    /**
     * Converts a [guild] to a GuildCreateEvent object
     */
    fun toSerializableGuildCreateEvent(guild: Guild, selfUser: User): GuildCreateEvent {
        // We attempt to really optimize this to make Loritta shutdown very fast
        // We don't use the JSON DSL builder because building objects and then converting them with Json.encodeToString is faster
        // We use streamUnordered to avoid any list copying. Emoji/Stickers use "forEachUnordered" because they don't have unordered streams
        // (However it seems that behind the scenes forEach/forEachUnordered for stickers/emojis are the same)
        val membersToBeSaved = listOf(guild.selfMember)

        val members = membersToBeSaved.map { member ->
            Member(
                user = selfUser,
                nick = member.nickname,
                avatar = member.avatarId,
                joined_at = if (member.hasTimeJoined()) formatIso(member.timeJoined) else null,
                premium_since = if (member.isBoosting) formatIso(member.timeBoosted) else null,
                pending = member.isPending,
                communication_disabled_until = formatIso(member.timeOutEnd),
                roles = member.roles.map { it.idLong }
            )
        }

        val roles = mutableListOf<Role>()

        guild.roleCache.streamUnordered().forEach { role ->
            roles.add(
                Role(
                    id = role.idLong,
                    name = role.name,
                    color = role.colorRaw,
                    hoist = role.isHoisted,
                    icon = role.icon?.iconId,
                    unicode_emoji = role.icon?.emoji,
                    position = role.positionRaw,
                    permissions = role.permissionsRaw,
                    managed = role.isManaged,
                    mentionable = role.isMentionable,
                    tags = RoleTags(
                        bot_id = role.tags.botIdLong,
                        integration_id = role.tags.integrationIdLong,
                        premium_subscriber = role.tags.isBoost
                    )
                )
            )
        }

        val channels = mutableListOf<Channel>()

        guild.channelCache.streamUnordered().forEach { channel ->
            channels.add(
                Channel(
                    id = channel.idLong,
                    type = channel.type.id,
                    guild_id = channel.guild.idLong,
                    name = channel.name,
                    flags = ChannelFlag.getRaw(channel.flags),
                    position = if (channel is IPositionableChannel) channel.positionRaw else null,
                    parent_id = if (channel is ICategorizableChannel) channel.parentCategoryIdLong else null,
                    nsfw = if (channel is IAgeRestrictedChannel) channel.isNSFW else null,
                    topic = if (channel is StandardGuildMessageChannel) channel.topic else null,
                    last_message_id = if (channel is MessageChannel) channel.latestMessageIdLong else null,
                    user_limit = if (channel is AudioChannel) channel.userLimit else null,
                    bitrate = if (channel is AudioChannel) channel.bitrate else null,
                    rtc_region = if (channel is AudioChannel) channel.regionRaw else null,
                    rate_limit_per_user = if (channel is ISlowmodeChannel) channel.slowmode else null,
                    default_thread_rate_limit_per_user = if (channel is IThreadContainer) channel.defaultThreadSlowmode else null,
                    default_forum_layout = if (channel is ForumChannel) channel.defaultLayout.key else null,
                    default_sort_order = if (channel is IPostContainer) channel.defaultSortOrder.key else null,
                    default_reaction_emoji = if (channel is IPostContainer) {
                        channel.defaultReaction?.let { emoji ->
                            ReactionEmoji(
                                emoji_id = if (emoji.type == net.dv8tion.jda.api.entities.emoji.Emoji.Type.CUSTOM) emoji.asCustom().idLong else null,
                                emoji_name = if (emoji.type == net.dv8tion.jda.api.entities.emoji.Emoji.Type.UNICODE) emoji.asUnicode().name else null
                            )
                        }
                    } else null,
                    available_tags = if (channel is IPostContainer) {
                        channel.availableTagCache.map { tag ->
                            Tag(
                                id = tag.idLong,
                                name = tag.name,
                                moderated = tag.isModerated,
                                emoji_id = if (tag.emoji?.type == net.dv8tion.jda.api.entities.emoji.Emoji.Type.CUSTOM) tag.emoji!!.asCustom().idLong else null,
                                emoji_name = if (tag.emoji?.type == net.dv8tion.jda.api.entities.emoji.Emoji.Type.UNICODE) tag.emoji!!.asUnicode().name else null
                            )
                        }
                    } else null,
                    permission_overwrites = if (channel is IPermissionContainerMixin<*>) {
                        val permissionOverwrites = mutableListOf<PermissionOverwrite>()

                        for (permissionOverwrite in channel.permissionOverrideMap.valueCollection()) {
                            permissionOverwrites.add(
                                PermissionOverwrite(
                                    id = permissionOverwrite.idLong,
                                    type = if (permissionOverwrite.isRoleOverride) 0 else 1,
                                    allow = permissionOverwrite.allowedRaw,
                                    deny = permissionOverwrite.deniedRaw
                                )
                            )
                        }

                        permissionOverwrites
                    } else if (channel is IPermissionContainer) {
                        val permissionOverwrites = mutableListOf<PermissionOverwrite>()

                        for (permissionOverwrite in channel.permissionOverrides) {
                            permissionOverwrites.add(
                                PermissionOverwrite(
                                    id = permissionOverwrite.idLong,
                                    type = if (permissionOverwrite.isRoleOverride) 0 else 1,
                                    allow = permissionOverwrite.allowedRaw,
                                    deny = permissionOverwrite.deniedRaw
                                )
                            )
                        }

                        permissionOverwrites
                    } else {
                        null
                    }
                )
            )
        }

        val threads = mutableListOf<Thread>()

        guild.threadChannelCache.streamUnordered().forEach { thread ->
            // We do this because "getParentChannel()" calls the cache, even tho we don't need a super up-to-date reference of the channel anyway... we only care about the channel ID!
            val parentChannel = parentChannelField.get(thread) as IThreadContainerUnion

            threads.add(
                Thread(
                    id = thread.idLong,
                    parent_id = parentChannel.idLong,
                    type = thread.type.id,
                    guild_id = thread.guild.idLong,
                    name = thread.name,
                    owner_id = thread.ownerIdLong,
                    member_count = thread.memberCount,
                    message_count = thread.messageCount,
                    total_message_sent = thread.totalMessageCount,
                    last_message_id = thread.latestMessageIdLong,
                    rate_limit_per_user = thread.slowmode,
                    thread_metadata = ThreadMetadata(
                        locked = thread.isLocked,
                        archived = thread.isArchived,
                        invitable = if (thread.type == ChannelType.GUILD_PRIVATE_THREAD) thread.isInvitable else null,
                        archive_timestamp = formatIso(thread.timeArchiveInfoLastModified),
                        create_timestamp = formatIso(thread.timeCreated),
                        auto_archive_duration = thread.autoArchiveDuration.minutes
                    )
                )
            )
        }

        val emojis = mutableListOf<Emoji>()

        guild.emojiCache.forEachUnordered { emoji ->
            emojis.add(
                Emoji(
                    id = emoji.idLong,
                    name = emoji.name,
                    roles = if (emoji.roles.isNotEmpty()) emoji.roles.map { it.idLong } else null,
                    animated = emoji.isAnimated,
                    managed = emoji.isManaged,
                    available = emoji.isAvailable
                )
            )
        }

        val stickers = mutableListOf<Sticker>()

        guild.stickerCache.forEachUnordered { sticker ->
            stickers.add(
                Sticker(
                    id = sticker.idLong,
                    name = sticker.name,
                    format_type = sticker.formatType.id,
                    type = sticker.type.id,
                    description = sticker.description,
                    tags = sticker.tags.joinToString(", "),
                    available = sticker.isAvailable,
                    guild_id = sticker.guildIdLong,
                    user = null // TODO: User (but does it reaaaally matter?)
                )
            )
        }

        val voiceStates = guild.voiceStates.mapNotNull { voiceState ->
            voiceState.channel?.let { channel ->
                VoiceState(
                    user_id = voiceState.member.idLong,
                    channel_id = channel.idLong,
                    request_to_speak_timestamp = formatIso(voiceState.requestToSpeakTimestamp),
                    self_mute = voiceState.isSelfMuted,
                    self_deaf = voiceState.isSelfDeafened,
                    mute = voiceState.isMuted,
                    deaf = voiceState.isDeafened,
                    suppress = voiceState.isSuppressed,
                    session_id = voiceState.sessionId,
                    self_stream = voiceState.isStream
                )
            }
        }

        return GuildCreateEvent(
            id = guild.idLong,
            name = guild.name,
            icon = guild.iconId,
            splash = guild.splashId,
            owner_id = guild.ownerIdLong,
            afk_channel_id = guild.afkChannel?.idLong,
            afk_timeout = guild.afkTimeout.seconds,
            verification_level = guild.verificationLevel.key,
            default_message_notifications = guild.defaultNotificationLevel.key,
            explicit_content_filter = guild.explicitContentLevel.key,
            mfa_level = guild.requiredMFALevel.key,
            system_channel_id = guild.systemChannel?.idLong,
            rules_channel_id = guild.rulesChannel?.idLong,
            max_presences = guild.maxPresences,
            max_members = guild.maxMembers,
            vanity_url_code = guild.vanityCode,
            description = guild.description,
            banner = guild.bannerId,
            premium_tier = guild.boostTier.key,
            premium_subscription_count = guild.boostCount,
            preferred_locale = guild.locale.locale,
            public_updates_channel_id = guild.communityUpdatesChannel?.idLong,
            nsfw_level = guild.nsfwLevel.key,
            premium_progress_bar_enabled = guild.isBoostProgressBarEnabled,
            features = guild.features,
            member_count = guild.memberCount,
            members = members,
            roles = roles,
            channels = channels,
            threads = threads,
            guild_scheduled_events = emptyList(), // Not used
            emojis = emojis,
            stickers = stickers,
            voice_states = voiceStates
        )
    }

    private fun formatIso(odt: OffsetDateTime?) = odt?.let { DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it) }

    @Serializable
    data class GuildCreateEvent(
        val id: Long,
        val name: String,
        val icon: String?,
        val splash: String?,
        val owner_id: Long,
        val afk_channel_id: Long?,
        val afk_timeout: Int,
        val verification_level: Int,
        val default_message_notifications: Int,
        val explicit_content_filter: Int,
        val mfa_level: Int,
        val system_channel_id: Long?,
        val rules_channel_id: Long?,
        val max_presences: Int?,
        val max_members: Int,
        val vanity_url_code: String?,
        val description: String?,
        val banner: String?,
        val premium_tier: Int,
        val premium_subscription_count: Int?,
        val preferred_locale: String,
        val public_updates_channel_id: Long?,
        val nsfw_level: Int,
        val premium_progress_bar_enabled: Boolean,
        val features: Set<String>,
        val member_count: Int,
        val members: List<Member>,
        val roles: List<Role>,
        val channels: List<Channel>,
        val threads: List<Thread>,
        val guild_scheduled_events: List<JsonElement>, // Assuming this is empty
        val emojis: List<Emoji>,
        val stickers: List<Sticker>,
        val voice_states: List<VoiceState>
    )

    @Serializable
    data class Member(
        val user: User,
        val nick: String?,
        val avatar: String?,
        val joined_at: String?,
        val premium_since: String?,
        val pending: Boolean,
        val communication_disabled_until: String?,
        val roles: List<Long>
    )

    @Serializable
    data class User(
        val id: Long,
        val username: String,
        val global_name: String?,
        val discriminator: String,
        val avatar: String?,
        val public_flags: Int,
        val bot: Boolean,
        val system: Boolean
    )

    @Serializable
    data class Role(
        val id: Long,
        val name: String,
        val color: Int,
        val hoist: Boolean,
        val icon: String?,
        val unicode_emoji: String?,
        val position: Int,
        val permissions: Long,
        val managed: Boolean,
        val mentionable: Boolean,
        val tags: RoleTags
    )

    @Serializable
    data class RoleTags(
        val bot_id: Long?,
        val integration_id: Long?,
        val premium_subscriber: Boolean
    )

    @Serializable
    data class Channel(
        val id: Long,
        val type: Int,
        val guild_id: Long,
        val name: String?,
        val flags: Int,
        val position: Int?,
        val parent_id: Long?,
        val nsfw: Boolean?,
        val topic: String?,
        val last_message_id: Long?,
        val user_limit: Int?,
        val bitrate: Int?,
        val rtc_region: String?,
        val rate_limit_per_user: Int?,
        val default_thread_rate_limit_per_user: Int?,
        val default_forum_layout: Int?,
        val default_sort_order: Int?,
        val default_reaction_emoji: ReactionEmoji?,
        val available_tags: List<Tag>?,
        val permission_overwrites: List<PermissionOverwrite>?
    )

    @Serializable
    data class ReactionEmoji(
        val emoji_id: Long?,
        val emoji_name: String?
    )

    @Serializable
    data class Tag(
        val id: Long,
        val name: String,
        val moderated: Boolean,
        val emoji_id: Long?,
        val emoji_name: String?
    )

    @Serializable
    data class PermissionOverwrite(
        val id: Long,
        val type: Int,
        val allow: Long,
        val deny: Long
    )

    @Serializable
    data class Thread(
        val id: Long,
        val parent_id: Long,
        val type: Int,
        val guild_id: Long,
        val name: String,
        val owner_id: Long,
        val member_count: Int,
        val message_count: Int,
        val total_message_sent: Int,
        val last_message_id: Long?,
        val rate_limit_per_user: Int?,
        val thread_metadata: ThreadMetadata
    )

    @Serializable
    data class ThreadMetadata(
        val locked: Boolean,
        val archived: Boolean,
        val invitable: Boolean?,
        val archive_timestamp: String?,
        val create_timestamp: String?,
        val auto_archive_duration: Int
    )

    @Serializable
    data class Emoji(
        val id: Long,
        val name: String,
        val roles: List<Long>?,
        val animated: Boolean,
        val managed: Boolean,
        val available: Boolean
    )

    @Serializable
    data class Sticker(
        val id: Long,
        val name: String,
        val format_type: Int,
        val type: Int,
        val description: String?,
        val tags: String?,
        val available: Boolean,
        val guild_id: Long,
        val user: String? // Assuming null is okay
    )

    @Serializable
    data class VoiceState(
        val user_id: Long,
        val channel_id: Long,
        val request_to_speak_timestamp: String?,
        val self_mute: Boolean,
        val self_deaf: Boolean,
        val mute: Boolean,
        val deaf: Boolean,
        val suppress: Boolean,
        val session_id: String?,
        val self_stream: Boolean
    )
}