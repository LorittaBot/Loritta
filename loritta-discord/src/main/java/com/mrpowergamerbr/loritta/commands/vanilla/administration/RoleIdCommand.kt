package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import java.util.*

class RoleIdCommand : AbstractCommand("roleid", listOf("cargoid", "iddocargo"), CommandCategory.ADMIN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.moderation.roleId.description")

	// TODO: Fix getUsage

	override fun getExamples(): List<String> {
		return Arrays.asList("Moderadores")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			var argument = context.rawArgs.joinToString(" ")

			val mentionedRoles = context.message.mentionedRoles // Se o usuário mencionar o cargo, vamos mostrar o ID dos cargos mencionados

			val list = mutableListOf<LorittaReply>()

			if (mentionedRoles.isNotEmpty()) {

				list.add(LorittaReply(
                        message = locale["commands.moderation.roleId.identifiers", argument],
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
                        message = locale["commands.moderation.roleId.rolesThatContains", argument],
                        prefix = "\uD83D\uDCBC"
                ))

				if (roles.isEmpty()) {
					list.add(
                            LorittaReply(
                                    message = "*${locale["commands.moderation.roleId.emptyRoles"]}*",
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
}