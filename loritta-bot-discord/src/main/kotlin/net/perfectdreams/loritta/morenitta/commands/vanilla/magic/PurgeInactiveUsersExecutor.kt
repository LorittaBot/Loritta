package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll

object PurgeInactiveUsersExecutor : LoriToolsCommand.LoriToolsExecutor {
	override val args = "purge inactive users <now>"

	override fun executes(): suspend CommandContext.() -> Boolean = task@{
		if (args.getOrNull(0) != "purge")
			return@task false
		if (args.getOrNull(1) != "inactive")
			return@task false
		if (args.getOrNull(2) != "users")
			return@task false

		val inactiveUsersQuery = loritta.pudding.transaction {
			Profiles.selectAll().where {
				Profiles.money eq 0 and (Profiles.xp eq 0) and (Profiles.lastMessageSentAt eq 0) and (Profiles.marriage.isNull())
			}
		}

		if (args.getOrNull(3) == "now") {
			reply(
					LorittaReply(
							"Deletando usuários inativos..."
					)
			)

			val count = loritta.pudding.transaction {
				Profiles.deleteWhere { Profiles.money eq 0 and (Profiles.xp eq 0) and (Profiles.lastMessageSentAt eq 0) and (Profiles.marriage.isNull()) }
			}

			reply(
					LorittaReply(
							"Feito! $count usuários inativos foram deletados!"
					)
			)
		} else {
			val count = loritta.pudding.transaction {
				inactiveUsersQuery.count()
			}
			reply(
					LorittaReply(
							"Existem **$count usuários** inativos (sem sonhos, sem XP, nunca enviaram algo no chat e não são casados)"
					)
			)
		}

		return@task true
	}
}