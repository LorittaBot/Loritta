package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
sealed class DiscordChannel {
    abstract val id: Long
    abstract val name: String
}

@Serializable
data class TextDiscordChannel(
    override val id: Long,
    override val name: String
) : DiscordChannel()

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
    override val name: String
) : DiscordChannel()

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