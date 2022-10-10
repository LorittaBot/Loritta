package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake

class RedisKeys(val keyPrefix: String) {
    fun discordGuilds() = redisKey("discord_guilds")
    fun discordChannels() = redisKey("discord_channels")

    fun discordGuildMembers(guildId: Snowflake) = redisKey("discord_guild_members:$guildId")
    fun discordGuildChannels(guildId: Snowflake) = redisKey("discord_guild_channels:$guildId")
    fun discordGuildRoles(guildId: Snowflake) = redisKey("discord_guild_roles:$guildId")
    fun discordGuildEmojis(guildId: Snowflake) = redisKey("discord_guild_emojis:$guildId")
    fun discordGuildVoiceStates(guildId: Snowflake) = redisKey("discord_guild_voice_states:$guildId")

    fun discordUsers() = redisKey("discord_users")

    fun discordGatewayEvents(shardId: Int) = redisKey("discord_gateway_events:shard_$shardId")
    fun discordGatewayCommands(shardId: Int) = redisKey("discord_gateway_commands:shard_$shardId")
    fun discordGatewaySessions(shardId: Int) = redisKey("discord_gateway_sessions:shard_$shardId")

    fun lorittaRaffle(type: String) = redisKey("raffle:raffle_$type")
    fun youTubeWebhooks() = redisKey("youtube_webhooks")

    fun redisKey(key: String) = "$keyPrefix:$key"
}