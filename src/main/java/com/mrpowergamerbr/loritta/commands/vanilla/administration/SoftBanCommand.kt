package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.Permission

class SoftBanCommand : CommandBase() {
	override fun getLabel(): String {
		return "softban"
	}

	override fun getDescription(): String {
		return "Faz um \"softban\" em um usuário, ou seja, o usuário é banido e desbanido logo em seguida, usado para deletar as mensagens do usuário."
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("menção/ID" to "ID ou menção do usuário que será banido",
				"dias" to "(Opcional) Quantos dias serão deletados, no mínimo 7",
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
				if (context.args[1].toIntOrNull() != null) {
					days = context.args[1].toInt()
				}

				if (days > 7) {
					context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " É impossível softbanir alguém por mais de 7 dias!");
					return;
				}
				if (0 > days) {
					context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " É impossível softbanir alguém por menos de 0 dias! (E como isso iria funcionar?)");
					return;
				}

				var reason: String? = null;
				if (context.args.size > 1) {
					reason = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
				}
				context.guild.controller.ban(id, days, "Softbanned por " + context.userHandle.name + "#" + context.userHandle.discriminator + if (reason != null) " (Motivo: " + reason + ")" else "").complete();
				context.guild.controller.unban(id).complete()

				context.sendMessage(context.getAsMention(true) + "Usuário `$id` foi softbanned com sucesso!");
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " Não tenho permissão para softbanir este usuário!");
			}
		} else {
			this.explain(context);
		}
	}
}