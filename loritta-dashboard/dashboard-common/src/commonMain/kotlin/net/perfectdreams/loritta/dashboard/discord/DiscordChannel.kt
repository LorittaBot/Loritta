package net.perfectdreams.loritta.dashboard.discord

import kotlinx.serialization.Serializable

@Serializable
sealed class DiscordChannel {
    abstract val id: Long
    abstract val name: String
}

sealed class GuildMessageChannel : DiscordChannel() {
    abstract val canTalk: Boolean
}

@Serializable
data class TextDiscordChannel(
    override val id: Long,
    override val name: String,
    override val canTalk: Boolean
) : GuildMessageChannel()

@Serializable
data class VoiceDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()

@Serializable
data class CategoryDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()

@Serializable
data class NewsDiscordChannel(
    override val id: Long,
    override val name: String,
    override val canTalk: Boolean
) : GuildMessageChannel()

@Serializable
data class ForumDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()

@Serializable
data class StageDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()

@Serializable
data class UnknownDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()