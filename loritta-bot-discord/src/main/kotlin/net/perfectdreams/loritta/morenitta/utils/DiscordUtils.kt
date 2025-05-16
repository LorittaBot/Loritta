package net.perfectdreams.loritta.morenitta.utils

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig

object DiscordUtils {
	// From JDA
	val USER_MENTION_REGEX = Regex("<@!?(\\d+)>")

	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getLorittaClusterForGuildId(loritta: LorittaBot, id: Long): LorittaConfig.LorittaClustersConfig.LorittaClusterConfig {
		val shardId = getShardIdFromGuildId(loritta, id)
		return getLorittaClusterForShardId(loritta, shardId)
	}

	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getShardIdFromGuildId(loritta: LorittaBot, id: Long) = getShardIdFromGuildId(id, loritta.config.loritta.discord.maxShards)

	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getShardIdFromGuildId(id: Long, maxShards: Int) = (id shr 22).rem(maxShards).toInt()

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster
	 */
	fun getLorittaClusterForShardId(loritta: LorittaBot, id: Int): LorittaConfig.LorittaClustersConfig.LorittaClusterConfig {
		val lorittaShard = loritta.config.loritta.clusters.instances.firstOrNull { id in it.minShard..it.maxShard }
		return lorittaShard ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
	}

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster ID
	 */
	fun getLorittaClusterIdForShardId(loritta: LorittaBot, id: Int) = getLorittaClusterForShardId(loritta, id).id

	/**
	 * Gets the URL for the specified Loritta Cluster
	 *
	 * @return the url in a "https://test.example.com/" format
	 */
	fun getUrlForLorittaClusterId(loritta: LorittaBot, id: Int) = loritta.config.loritta.clusters.instances.first() {
		it.id == id
	}.websiteUrl.format(id)

	/**
	 * Gets the Internal URL for the specified Loritta Cluster
	 *
	 * @return the url in a "https://test.example.com/" format
	 */
	fun getInternalUrlForLorittaClusterId(loritta: LorittaBot, id: Int) = loritta.config.loritta.clusters.instances.first() {
		it.id == id
	}.websiteInternalUrl.format(id)

	suspend fun extractUserFromString(
		loritta: LorittaBot,
		input: String,
		usersInContext: List<User>? = null,
		guild: Guild? = null,
		extractUserViaMention: Boolean = true,
		extractUserViaNameAndDiscriminator: Boolean = true,
		extractUserViaEffectiveName: Boolean = true,
		extractUserViaUsername: Boolean = true,
		extractUserViaUserIdRetrieval: Boolean = true
	): User? {
		if (input.isEmpty()) // If empty, just ignore it
			return null

		// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
		if (usersInContext != null && extractUserViaMention) {
			for (user in usersInContext) {
				if (user.asMention == input.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}
		}

		// Extract user IDs in mentions that the user isn't present in the usersInContext list
		try {
			if (extractUserViaUserIdRetrieval && extractUserViaMention) {
				val match = USER_MENTION_REGEX.find(input)
				if (match != null) {
					val user = loritta.lorittaShards.retrieveUserById(match.groupValues[1])

					if (user != null) // Pelo visto é!
						return user
				}
			}
		} catch (e: Exception) {
		}

		// First, we will check if it is an ID and then we will check if it is a name
		// This avoids users using another user's ID as a name, causing havoc
		// (Example: "+pay 297153970613387264", which would've meant that you want to transfer to Loritta
		// but if someone has the name "297153970613387264", it would've been transferred to them, not Lori!)
		// Check if it is a Discord ID
		try {
			if (extractUserViaUserIdRetrieval) {
				val user = loritta.lorittaShards.retrieveUserById(input)

				if (user != null) // Pelo visto é!
					return user
			}
		} catch (e: Exception) {
		}

		// Vamos tentar procurar pelo username + discriminator
		if (guild != null) {
			if (extractUserViaNameAndDiscriminator) {
				// TODO: Support names with space (maybe impossible)
				val split = input.split("#")
				if (split.size == 2) {
					val discriminator = split.last()
					val name = split.dropLast(1).joinToString(" ")
					try {
						val matchedMember = guild.getMemberByTag(name, discriminator)
						if (matchedMember != null)
							return matchedMember.user
					} catch (e: IllegalArgumentException) {} // We don't really care if it is in a invalid format
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (extractUserViaEffectiveName) {
				val matchedMembers = guild.getMembersByEffectiveName(input, true)
				val matchedMember = matchedMembers.firstOrNull()
				if (matchedMember != null)
					return matchedMember.user
			}

			// Se não, vamos procurar só pelo username mesmo
			if (extractUserViaUsername) {
				val matchedMembers = guild.getMembersByName(input, true)
				val matchedMember = matchedMembers.firstOrNull()
				if (matchedMember != null)
					return matchedMember.user
			}
		}

		return null
	}
}