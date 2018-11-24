package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class UnwarnCommand : AbstractCommand("unwarn", listOf("desavisar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNWARN_Description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = context.getUserAt(0)

			if (user == null) {
				context.reply(
						LoriReply(
								locale["BAN_UserDoesntExist"],
								Constants.ERROR
						)
				)
				return
			}

			val member = context.guild.getMember(user)

			if (member != null) {
				if (!context.guild.selfMember.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_RoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}

				if (!context.handle.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_PunisherRoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}
			}

			val warns = transaction(Databases.loritta) {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.toMutableList()
			}

			if (warns.isEmpty()) {
				context.reply(
						LoriReply(
								locale["WARN_DoesntHaveWarns"],
								Constants.ERROR
						)
				)
				return
			}

			transaction(Databases.loritta) {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }
						.sortedByDescending { it.receivedAt }
						.first()
						.delete()
			}

			loritta save context.config

			context.reply(
					LoriReply(
							locale["WARN_WarnRemoved"],
							"\uD83C\uDF89"
					)
			)
		} else {
			this.explain(context)
		}
	}
}