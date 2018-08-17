package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.Permission

class UnmuteCommand : AbstractCommand("unmute", listOf("desmutar", "desilenciar", "desilenciar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNMUTE_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944");
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

	override fun run(context: CommandContext, locale: BaseLocale) {
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

				val thread = MuteCommand.roleRemovalThreads[member.guild.id + "#" + member.user.id]
				thread?.interrupt()
				MuteCommand.roleRemovalThreads.remove(member.guild.id + "#" + member.user.id)

				if (mutedRoles != null) {
					member.guild.controller.removeSingleRoleFromMember(member, mutedRoles).complete()
				}
			}

			loritta.serversColl.updateOne(
					Filters.and(
							Filters.eq(
									"_id", context.guild.id
							),
							Filters.eq(
									"guildUserData.userId", context.userHandle.id
							)
					),
					Updates.combine(
							Updates.set(
									"guildUserData.$.muted", false
							),
							Updates.set(
									"guildUserData.$.temporaryMute", false
							),
							Updates.set(
									"guildUserData.$.expiresIn", 0
							)
					)
			)

			context.reply(
					LoriReply(
							locale["MUTE_MuteRemoved"],
							"\uD83C\uDF89"
					)
			)
		} else {
			this.explain(context);
		}
	}
}