package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission

class SoftBanCommand : CommandBase() {
	override fun getLabel(): String {
		return "softban"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.SOFTBAN_DESCRIPTION.msgFormat()
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será banido",
				"dias" to "(Opcional) Quantos dias serão deletados, no máximo 7",
				"motivo" to "(Opcional) Motivo do Softban")
	}

	override fun getExample(): List<String> {
		return listOf("@Fulano", "@Fulano Algum motivo bastante aleatório", "@Fulano 1 Limpar mensagens do último dia");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN;
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
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
					context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS.msgFormat());
					return;
				}
				if (0 > days) {
					context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS.msgFormat());
					return;
				}

				var reason: String? = null;
				if (context.args.size > 1) {
					reason = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
				}
				context.guild.controller.ban(id, days, context.locale.SOFTBAN_BY.msgFormat(context.userHandle.name + "#" + context.userHandle.discriminator) + if (reason != null) " (${context.locale.HACKBAN_REASON.msgFormat()}: " + reason + ")" else "").complete();
				context.guild.controller.unban(id).complete()

				context.sendMessage(context.getAsMention(true) + context.locale.SOFTBAN_SUCCESS.msgFormat(id));
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.SOFTBAN_NO_PERM.msgFormat())
			}
		} else {
			this.explain(context);
		}
	}
}