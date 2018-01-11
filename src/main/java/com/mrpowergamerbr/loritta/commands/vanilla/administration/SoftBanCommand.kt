package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission

class SoftBanCommand : AbstractCommand("softban", category = CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SOFTBAN_DESCRIPTION"]
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será banido",
				"dias" to "(Opcional) Quantos dias serão deletados, no máximo 7",
				"motivo" to "(Opcional) Motivo do Softban")
	}

	override fun getExample(): List<String> {
		return listOf("@Fulano", "@Fulano Algum motivo bastante aleatório", "@Fulano 1 Limpar mensagens do último dia");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			try {
				var id = context.args[0];

				if (context.rawArgs[0].startsWith("<") && context.message.mentionedUsers.isNotEmpty()) {
					id = context.message.mentionedUsers[0].id
				}

				var days = 7;
				if (context.args.size > 1 && context.args[1].toIntOrNull() != null) {
					days = context.args[1].toInt()
				}

				if (days > 7) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS"]);
					return;
				}
				if (0 > days) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS"]);
					return;
				}

				var reason: String? = null;
				if (context.args.size > 1) {
					reason = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
				}
				context.guild.controller.ban(id, days, locale["SOFTBAN_BY", context.userHandle.name + "#" + context.userHandle.discriminator] + if (reason != null) " (${locale["HACKBAN_REASON"]}: " + reason + ")" else "").complete()
				context.guild.controller.unban(id).complete()

				context.sendMessage(context.getAsMention(true) + locale["SOFTBAN_SUCCESS", id])
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_NO_PERM"])
			}
		} else {
			this.explain(context);
		}
	}
}