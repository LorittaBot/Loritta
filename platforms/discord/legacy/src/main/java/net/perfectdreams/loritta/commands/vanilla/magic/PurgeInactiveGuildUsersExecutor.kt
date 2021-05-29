package net.perfectdreams.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PurgeInactiveGuildUsersExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive guild_users <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "guild_users")
			return@task false

		val inactiveUsersQuery = transaction(Databases.loritta) {
			GuildProfiles.select {
				GuildProfiles.money eq 0.toBigDecimal() and (GuildProfiles.xp.less(50L)) and (GuildProfiles.quickPunishment eq false)
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando usuários inativos..."
					)
			)

			val count = transaction(Databases.loritta) {
				GuildProfiles.deleteWhere { GuildProfiles.money eq 0.toBigDecimal() and (GuildProfiles.xp.less(50L)) and (GuildProfiles.quickPunishment eq false) }
			}

			reply(
					LorittaReply(
							"Feito! $count usuários de guilds inativos foram deletados!"
					)
			)
		} else {
			val count = transaction(Databases.loritta) {
				inactiveUsersQuery.count()
			}
			reply(
					LorittaReply(
							"Existem **$count usuários de guilds** inativos (sem dinheiro local, sem XP e não estão mais no servidor)"
					)
			)
		}

		return@task true
	}
}