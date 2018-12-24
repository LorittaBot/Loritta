package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role

class TempRoleCommand : AbstractCommand("temprole", listOf("cargotemp"), CommandCategory.MAGIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["HACKBAN_DESCRIPTION"]
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val split = context.rawArgs.joinToString(" ").split("|").map { it.trim() }
			val type = split.getOrNull(0)
			val rolesStr = split.getOrNull(1)
			val usersStr = split.getOrNull(2)

			// add/remove
			if (type == "add" && rolesStr != null && usersStr != null) {
				// Cargos deverão ser processados dependendo se o cara marcou o cargo ou só colocou parte do nome
				val roles = mutableListOf<Role>()
				val users = mutableListOf<Member>()

				run {
					val rolesSplit = rolesStr.split(", ")
					val names = mutableListOf<String>()

					for (part in rolesSplit) {
						if (part.startsWith("<@&") && part.endsWith(">")) {
							roles.add(context.guild.getRoleById(part.substring(3, part.length - 1)))
							continue
						}
						if (part.isValidSnowflake()) {
							roles.add(context.guild.getRoleById(part))
							continue
						}
						names += part
					}

					for (name in names) {
						if (name.isNotEmpty())
							roles.addAll(context.guild.getRolesByName(name, true))
					}
				}

				run {
					val usersSplit = usersStr.split(", ")
					val names = mutableListOf<String>()

					for (part in usersSplit) {
						if (part.replace("!", "").startsWith("<@") && usersStr.endsWith(">")) {
							users.add(context.guild.getMemberById(part.replace("!", "").substring(2, part.length - 1)))
							continue
						}
						if (part.isValidSnowflake()) {
							users.add(context.guild.getMemberById(part))
							continue
						}
						names += part
					}

					for (name in names) {
						if (name.isNotEmpty())
							users.addAll(context.guild.getMembersByEffectiveName(name, true))
					}
				}

				context.sendMessage(roles.joinToString(" ", transform = { it.asMention }))
				context.sendMessage(users.joinToString(" ", transform = { it.asMention }))

				// Cargos adicionados!
			}
		} else {
			this.explain(context)
		}
	}
}