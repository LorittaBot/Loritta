package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake

class RedisKeys(val keyPrefix: String) {
    fun discordGuilds(guildId: Snowflake) = redisKey("discord_guilds:$guildId")
    fun discordGuildMembers(guildId: Snowflake) = redisKey("discord_guild_members:$guildId")
    fun discordGuildChannels(guildId: Snowflake) = redisKey("discord_guild_channels:$guildId")
    fun discordGuildRoles(guildId: Snowflake) = redisKey("discord_guild_roles:$guildId")
    fun discordGuildEmojis(guildId: Snowflake) = redisKey("discord_guild_emojis:$guildId")
    fun discordGuildVoiceStates(guildId: Snowflake) = redisKey("discord_guild_voice_states:$guildId")

    fun discordGatewayEvents(shardId: Int) = redisKey("discord_gateway_events:shard_$shardId")
    fun discordGatewayCommands(shardId: Int) = redisKey("discord_gateway_commands:shard_$shardId")

    fun redisKey(key: String) = "$keyPrefix:$key"
}