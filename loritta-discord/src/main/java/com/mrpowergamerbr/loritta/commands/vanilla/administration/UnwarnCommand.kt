package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class UnwarnCommand : AbstractCommand("unwarn", listOf("desavisar"), CommandCategory.ADMIN) {
	companion object {
		private val LOCALE_PREFIX = "commands.moderation"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["UNWARN_Description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944", "312632996119445504 2")
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = AdminUtils.checkForUser(context) ?: return

			val member = context.guild.getMember(user)

			if (member != null) {
				if (!AdminUtils.checkForPermissions(context, member))
					return
			}

			val warns = transaction(Databases.loritta) {
				Warn.find { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.toList()
			}

			if (warns.isEmpty()) {
				context.reply(
						LoriReply(
								context.locale["$LOCALE_PREFIX.unwarn.noWarnsFound", "`${context.config.commandPrefix}warnlist`"],
								Constants.ERROR
						)
				)
				return
			}


			var warnIndex: Int = 0

			if (context.args.size >= 2) {
				if (context.args[1].toIntOrNull() == null) {
					context.reply(
							LoriReply(
									"${context.legacyLocale["INVALID_NUMBER", context.args[1]]}",
									Constants.ERROR
							)
					)
					return	
				}
				
				warnIndex = context.args[1].toInt()
			} else warnIndex = warns.size

			if (warnIndex > warns.size) {
				context.reply(
						LoriReply(
								context.locale["$LOCALE_PREFIX.unwarn.notEnoughWarns", warnIndex, "`${context.config.commandPrefix}warnlist`"],
								Constants.ERROR
						)
				)
				return
			}

			val warn = warns[warnIndex - 1]

			transaction(Databases.loritta) {
				warn.delete()
			}

			context.reply(
					LoriReply(
							context.locale["$LOCALE_PREFIX.unwarn.warnRemoved"] + " ${Emotes.LORI_HMPF}",
							"\uD83C\uDF89"
					)
			)
		} else {
			this.explain(context)
		}
	}
}