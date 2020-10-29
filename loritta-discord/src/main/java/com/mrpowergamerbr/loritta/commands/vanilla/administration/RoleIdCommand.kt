package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory

class RoleIdCommand : AbstractCommand("roleid", listOf("cargoid", "iddocargo"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.roleId.description"]
	}

	override fun getUsage(): String {
		return "CargoMencionado"
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.toNewLocale().getList("$LOCALE_PREFIX.roleId.examples")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			val argument = context.rawArgs.joinToString(" ")

			val mentionedRoles = context.message.mentionedRoles // Se o usuário mencionar o cargo, vamos mostrar o ID dos cargos mencionados

			val list = mutableListOf<LorittaReply>()

			if (mentionedRoles.isNotEmpty()) {

				list.add(LorittaReply(
						message = context.locale["$LOCALE_PREFIX.roleId.identifiers", argument],
						prefix = "\uD83D\uDCBC"
				))

				mentionedRoles.mapTo(list) {
					LorittaReply(
							message = "*${it.name}* - `${it.id}`",
							mentionUser = false
					)
				}
			} else {
				val roles = context.guild.roles.filter { it.name.contains(argument, true) }

				list.add(LorittaReply(
						message = context.locale["$LOCALE_PREFIX.roleId.rolesThatContains", argument],
						prefix = "\uD83D\uDCBC"
				))

				if (roles.isEmpty()) {
					list.add(
							LorittaReply(
									message = "*${context.locale["$LOCALE_PREFIX.roleId.emptyRoles"]}*",
									mentionUser = false,
									prefix = "\uD83D\uDE22"
							)
					)
				} else {
					roles.mapTo(list) {
						LorittaReply(
								message = "*${it.name}* - `${it.id}`",
								mentionUser = false
						)
					}
				}

			}
			context.reply(*list.toTypedArray())
		} else {
			context.explain()
		}
	}

	companion object {
		const val LOCALE_PREFIX = "commands.moderation"
	}

}