package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

object PurgeInactiveGuildUsersExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive guild_users <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "guild_users")
			return@task false

		val inactiveUsersQuery = loritta.pudding.transaction {
			GuildProfiles.selectAll().where {
				GuildProfiles.money eq 0.toBigDecimal() and (GuildProfiles.xp.less(50L)) and (GuildProfiles.quickPunishment eq false)
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando usuários inativos..."
					)
			)

			val count = loritta.pudding.transaction {
				GuildProfiles.deleteWhere { GuildProfiles.money eq 0.toBigDecimal() and (GuildProfiles.xp.less(50L)) and (GuildProfiles.quickPunishment eq false) }
			}

			reply(
					LorittaReply(
							"Feito! $count usuários de guilds inativos foram deletados!"
					)
			)
		} else {
			val count = loritta.pudding.transaction {
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