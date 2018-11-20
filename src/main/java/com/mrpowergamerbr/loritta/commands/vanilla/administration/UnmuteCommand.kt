package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class UnmuteCommand : AbstractCommand("unmute", listOf("desmutar", "desilenciar", "desilenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNMUTE_DESCRIPTION"]
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
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS)
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
				val mutedRoles = context.guild.getRolesByName(context.locale["MUTE_ROLE_NAME"], false).firstOrNull()

				val thread = MuteCommand.roleRemovalJobs[member.guild.id + "#" + member.user.id]
				thread?.cancel()
				MuteCommand.roleRemovalJobs.remove(member.guild.id + "#" + member.user.id)

				if (mutedRoles != null) {
					member.guild.controller.removeSingleRoleFromMember(member, mutedRoles).queue()
				}
			}

			transaction(Databases.loritta) {
				Mutes.deleteWhere {
					(Mutes.guildId eq context.guild.idLong) and (Mutes.userId eq member.user.idLong)
				}
			}

			context.reply(
					LoriReply(
							locale["MUTE_MuteRemoved"],
							"\uD83C\uDF89"
					)
			)
		} else {
			this.explain(context)
		}
	}
}