package com.mrpowergamerbr.loritta.utils

import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*

/**
 * Guarda todos os shards da Loritta
 */
class LorittaShards {
	lateinit var shardManager: ShardManager

	fun getGuildById(id: String): Guild? = shardManager.getGuildById(id)

	fun getGuilds(): List<Guild> = shardManager.guilds

	fun getGuildCount(): Int = shardManager.guilds.size

	fun getUserCount(): Int = shardManager.users.size

	fun getEmoteCount(): Int = shardManager.emotes.size

	fun getChannelCount(): Int = shardManager.textChannels.size + shardManager.voiceChannels.size

	fun getTextChannelCount(): Int = shardManager.textChannels.size

	fun getVoiceChannelCount(): Int  = shardManager.voiceChannels.size

	fun getUsers(): List<User> = shardManager.users

	fun getUserById(id: String?): User? {
		if (id == null)
			return null

		return shardManager.getUserById(id)
	}

	fun retrieveUserById(id: String?): User? {
		if (id == null)
			return null

		return getUserById(id) ?: shardManager.retrieveUserById(id).complete()
	}

	fun getMutualGuilds(user: User): List<Guild> = shardManager.getMutualGuilds(user)

	fun getEmoteById(id: String?): Emote? {
		if (id == null)
			return null

		return shardManager.getEmoteById(id)
	}

	fun getTextChannelById(id: String?): TextChannel? {
		if (id == null)
			return null

		return shardManager.getTextChannelById(id)
	}

	/**
	 * Atualiza a presença do bot em todas as shards
	 */
	fun setGame(game: Game) = shardManager.setGame(game)

	fun getShards(): List<JDA> {
		return shardManager.shards
	}
}