package net.perfectdreams.loritta.morenitta.utils.devious

import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelFlag
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.*
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DeviousConverter {
    const val CACHE_VERSION = 1
    private val logger = KotlinLogging.logger {}

    /**
     * Converts a [guild] to a Guild Create event
     */
    fun toJson(guild: Guild) = buildJsonObject {
        this.put("id", guild.idLong)
        this.put("name", guild.name)
        this.put("icon", guild.iconId)
        this.put("splash", guild.splashId)
        this.put("owner_id", guild.ownerIdLong)
        this.put("afk_channel_id", guild.afkChannel?.idLong)
        this.put("afk_timeout", guild.afkTimeout.seconds)
        // Unused by JDA: widget_enabled
        // Unused by JDA: widget_channel_id
        this.put("verification_level", guild.verificationLevel.key)
        this.put("default_message_notifications", guild.defaultNotificationLevel.key)
        this.put("explicit_content_filter", guild.explicitContentLevel.key)
        this.put("mfa_level", guild.requiredMFALevel.key)
        // Unused by JDA: application_id
        this.put("system_channel_id", guild.systemChannel?.idLong)
        // Unused by JDA: system_channel_flags
        this.put("rules_channel_id", guild.rulesChannel?.idLong)
        this.put("max_presences", guild.maxPresences)
        this.put("max_members", guild.maxMembers)
        this.put("description", guild.description)
        this.put("banner", guild.bannerId)
        this.put("premium_tier", guild.boostTier.key)
        this.put("premium_subscription_count", guild.boostCount)
        this.put("preferred_locale", guild.locale.locale)
        this.put("public_updates_channel_id", guild.communityUpdatesChannel?.idLong)
        // Unused by JDA: max_video_channel_users
        this.put("nsfw_level", guild.nsfwLevel.key)
        this.put("premium_progress_bar_enabled", guild.isBoostProgressBarEnabled)

        this.putJsonArray("features") {
            for (feature in guild.features) {
                add(feature)
            }
        }

        // GuildCreate exclusive fields
        // joined_at, doesn't seem to be provided by JDA
        // Unused by JDA: large
        this.put("member_count", guild.memberCount)
        this.putJsonArray("members") {
            // Instead of storing all guild members, we will only store Loritta's self instance
            // This way we avoid taking soooo damn long to save guild data, since we actually don't *need* to restore member data
            val membersToBeSaved = listOf(guild.selfMember)

            for (member in membersToBeSaved) {
                addJsonObject {
                    putJsonObject("user") {
                        val user = member.user

                        put("id", user.idLong)
                        put("username", user.name)
                        put("global_name", user.globalName)
                        put("discriminator", user.discriminator)
                        put("avatar", user.avatarId)
                        put("public_flags", user.flagsRaw)
                        put("bot", user.isBot)
                        put("system", user.isSystem)
                    }
                    put("nick", member.nickname)
                    put("avatar", member.avatarId)
                    if (member.hasTimeJoined())
                        put("joined_at", formatIso(member.timeJoined))
                    if (member.isBoosting)
                        put("premium_since", formatIso(member.timeBoosted))
                    // TODO: deaf
                    // TODO: mute
                    put("pending", member.isPending)
                    put("communication_disabled_until", formatIso(member.timeOutEnd))
                    putJsonArray("roles") {
                        for (role in member.roles) {
                            add(role.idLong)
                        }
                    }
                }
            }
        }
        this.putJsonArray("roles") {
            for (role in guild.roles) {
                addJsonObject {
                    put("id", role.idLong)
                    put("name", role.name)
                    put("color", role.colorRaw)
                    put("hoist", role.isHoisted)
                    put("icon", role.icon?.iconId)
                    put("unicode_emoji", role.icon?.emoji)
                    put("position", role.positionRaw)
                    put("permissions", role.permissionsRaw)
                    put("managed", role.isManaged)
                    put("mentionable", role.isMentionable)
                    putJsonObject("tags") {
                        put("bot_id", role.tags.botIdLong)
                        put("integration_id", role.tags.integrationIdLong)
                        put("premium_subscriber", role.tags.isBoost)
                    }
                }
            }
        }
        this.putJsonArray("channels") {
            for (channel in guild.channels) {
                addJsonObject {
                    put("id", channel.idLong)
                    put("type", channel.type.id)
                    put("guild_id", channel.guild.idLong)
                    put("name", channel.name)
                    put("flags", ChannelFlag.getRaw(channel.flags))
                    if (channel is IPositionableChannel) {
                        put("position", channel.positionRaw)
                    }
                    if (channel is ICategorizableChannel) {
                        put("parent_id", channel.parentCategoryIdLong)
                    }
                    if (channel is IAgeRestrictedChannel) {
                        put("nsfw", channel.isNSFW)
                    }
                    if (channel is StandardGuildMessageChannel) {
                        put("topic", channel.topic)
                    }
                    if (channel is MessageChannel) {
                        put("last_message_id", channel.latestMessageIdLong)
                    }
                    if (channel is AudioChannel) {
                        put("user_limit", channel.userLimit)
                        put("bitrate", channel.bitrate)
                        put("rtc_region", channel.regionRaw)
                    }
                    if (channel is ISlowmodeChannel) {
                        put("rate_limit_per_user", channel.slowmode)
                    }
                    if (channel is IThreadContainer) {
                        put("default_thread_rate_limit_per_user", channel.defaultThreadSlowmode)
                    }
                    if (channel is ForumChannel) {
                        put("default_forum_layout", channel.defaultLayout.key)
                    }
                    if (channel is IPostContainer) {
                        put("default_sort_order", channel.defaultSortOrder.key)
                        val emoji = channel.defaultReaction
                        if (emoji != null) {
                            putJsonObject("default_reaction_emoji") {
                                put("emoji_id", if (emoji.type == Emoji.Type.CUSTOM) emoji.asCustom().idLong else null)
                                put("emoji_name", if (emoji.type == Emoji.Type.UNICODE) emoji.asUnicode().name else null)
                            }
                        }

                        putJsonArray("available_tags") {
                            for (tag in channel.availableTags) {
                                addJsonObject {
                                    put("id", tag.idLong)
                                    put("name", tag.name)
                                    put("moderated", tag.isModerated)
                                    put("emoji_id", if (emoji?.type == Emoji.Type.CUSTOM) emoji.asCustom().idLong else null)
                                    put("emoji_name", if (emoji?.type == Emoji.Type.UNICODE) emoji.asUnicode().name else null)
                                }
                            }
                        }
                    }

                    if (channel is IPermissionContainer) {
                        putJsonArray("permission_overwrites") {
                            for (permissionOverwrite in channel.permissionOverrides) {
                                addJsonObject {
                                    put("id", permissionOverwrite.idLong)
                                    put("type", if (permissionOverwrite.isRoleOverride) 0 else 1)
                                    put("allow", permissionOverwrite.allowedRaw)
                                    put("deny", permissionOverwrite.deniedRaw)
                                }
                            }
                        }
                    }
                }
            }
        }
        this.putJsonArray("threads") {
            for (channel in guild.threadChannels) {
                addJsonObject {
                    put("id", channel.idLong)
                    put("parent_id", channel.parentChannel.idLong)
                    put("type", channel.type.id)
                    put("guild_id", channel.guild.idLong)
                    put("name", channel.name)
                    put("owner_id", channel.ownerIdLong)
                    put("member_count", channel.memberCount)
                    put("message_count", channel.messageCount)
                    put("total_message_sent", channel.totalMessageCount)
                    put("last_message_id", channel.latestMessageIdLong)
                    put("rate_limit_per_user", channel.slowmode)
                    putJsonObject("thread_metadata") {
                        put("locked", channel.isLocked)
                        put("archived", channel.isArchived)
                        if (channel.type == ChannelType.GUILD_PRIVATE_THREAD)
                            put("invitable", channel.isInvitable)
                        put("archive_timestamp", formatIso(channel.timeArchiveInfoLastModified))
                        put("create_timestamp", formatIso(channel.timeCreated))
                        put("auto_archive_duration", channel.autoArchiveDuration.minutes)
                    }
                }
            }
        }
        // we do not care i repeat we do not care
        this.putJsonArray("guild_scheduled_events") {}
        this.putJsonArray("emojis") {
            for (emoji in guild.emojis) {
                addJsonObject {
                    put("id", emoji.idLong)
                    put("name", emoji.name)
                    val roles = emoji.roles
                    if (roles.isNotEmpty()) {
                        putJsonArray("roles") {
                            emoji.roles.forEach {
                                add(it.idLong)
                            }
                        }
                    }
                    // TODO: User (but does it reaaaally matter?)
                    put("user", null)
                    put("animated", emoji.isAnimated)
                    put("managed", emoji.isManaged)
                    put("available", emoji.isAvailable)
                }
            }
        }
        this.putJsonArray("stickers") {
            for (sticker in guild.stickers) {
                addJsonObject {
                    put("id", sticker.idLong)
                    put("name", sticker.name)
                    put("format_type", sticker.formatType.id)
                    put("type", sticker.type.id)
                    put("description", sticker.description)
                    putJsonArray("tags") {
                        sticker.tags.forEach {
                            add(it)
                        }
                    }
                    put("available", sticker.isAvailable)
                    put("guild_id", sticker.guildIdLong)
                    // TODO: User (but does it reaaaally matter?)
                    put("user", null)
                }
            }
        }
        this.putJsonArray("voice_states") {
            for (voiceState in guild.voiceStates) {
                // Only add users to the voice state list if they are connected in a channel
                val channelId = voiceState.channel?.idLong ?: continue

                addJsonObject {
                    put("user_id", voiceState.member.idLong)
                    put("channel_id", channelId)
                    put("request_to_speak_timestamp", formatIso(voiceState.requestToSpeakTimestamp))
                    put("self_mute", voiceState.isSelfMuted)
                    put("self_deaf", voiceState.isSelfDeafened)
                    put("mute", voiceState.isMuted)
                    put("deaf", voiceState.isDeafened)
                    put("suppress", voiceState.isSuppressed)
                    put("session_id", voiceState.sessionId)
                    put("self_stream", voiceState.isStream)
                }
            }
        }
    }

    private fun formatIso(odt: OffsetDateTime?) = odt?.let { DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it) }
}