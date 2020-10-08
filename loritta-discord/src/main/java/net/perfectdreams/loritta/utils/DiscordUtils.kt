package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User

object DiscordUtils {
	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getLorittaClusterForGuildId(id: Long): GeneralConfig.LorittaClusterConfig {
		val shardId = getShardIdFromGuildId(id)
		return getLorittaClusterForShardId(shardId)
	}

	/**
	 * Gets a Discord Shard ID from the provided Guild ID
	 *
	 * @return the shard ID
	 */
	fun getShardIdFromGuildId(id: Long): Long {
		val maxShard = loritta.discordConfig.discord.maxShards
		return (id shr 22).rem(maxShard)
	}

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster
	 */
	fun getLorittaClusterForShardId(id: Long): GeneralConfig.LorittaClusterConfig {
		val lorittaShard = loritta.config.clusters.firstOrNull { id in it.minShard..it.maxShard }
		return lorittaShard ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
	}

	/**
	 * Gets the cluster where the guild that has the specified ID is in
	 *
	 * @return the cluster ID
	 */
	fun getLorittaClusterIdForShardId(id: Long) = getLorittaClusterForShardId(id).id

	/**
	 * Gets the URL for the specified Loritta Cluster
	 *
	 * @return the url in a "test.example.com" format
	 */
	fun getUrlForLorittaClusterId(id: Long): String {
		if (id == 1L)
			return loritta.instanceConfig.loritta.website.url.substring(loritta.instanceConfig.loritta.website.url.indexOf("//") + 2).removeSuffix("/")

		return loritta.instanceConfig.loritta.website.clusterUrl.format(id)
	}

	suspend fun extractUserFromString(
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

		// Ok, então só pode ser um ID do Discord!
		try {
			if (extractUserViaUserIdRetrieval) {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(input)

				if (user != null) // Pelo visto é!
					return user
			}
		} catch (e: Exception) {
		}

		return null
	}
}