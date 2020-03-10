package net.perfectdreams.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.utils.DiscordUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object PurgeInactiveGuildsExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive guilds <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "guilds")
			return@task false

		val badGuildData = mutableListOf<Long>()

		transaction(Databases.loritta) {
			ServerConfigs.selectAll().forEach {
				if (it[ServerConfigs.id].value != -1L) { // -1 = coisas na DM
					val shardId = DiscordUtils.getLorittaClusterForGuildId(it[ServerConfigs.id].value)

					if (shardId.id == com.mrpowergamerbr.loritta.LorittaLauncher.loritta.lorittaCluster.id) {
						val guild = lorittaShards.getGuildById(it[ServerConfigs.id].value)

						if (guild == null)
							badGuildData.add(it[ServerConfigs.id].value)
					}
				}
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando guilds inativas..."
					)
			)

			val count = transaction(Databases.loritta) {
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId inList badGuildData
				}

				ServerConfigs.deleteWhere { ServerConfigs.id inList badGuildData }

				Giveaway.find {
					Giveaways.guildId inList badGuildData
				}
			}

			reply(
					LorittaReply(
							"Feito! $count guilds inativas foram deletadas!"
					)
			)
		} else {
			reply(
					LorittaReply(
							"Existem **${badGuildData.size} guilds** inativas (Deveriam existir no cluster atual... mas cadê?)"
					)
			)
		}

		return@task true
	}
}