package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.Permission

class HackBanCommand : CommandBase() {
	override fun getLabel(): String {
		return "hackban"
	}

	override fun getDescription(): String {
		return "Permite banir um usuário pelo ID dele antes de ele entrar no seu servidor!"
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN;
	}

	override fun run(context: CommandContext) {
		if (context.getHandle().hasPermission(Permission.MANAGE_SERVER)) {
			if (context.args.isNotEmpty()) {
				try {
					var id = context.args[0];

					var reason: String? = null;
					if (context.args.size > 1) {
						reason = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
					}
					context.guild.controller.ban(id, 0, "Hackbanned por " + context.userHandle.name + "#" + context.userHandle.discriminator + if (reason != null) " (Motivo: " + reason + ")" else "").complete();

					context.sendMessage(context.getAsMention(true) + "Usuário `$id` foi banido com sucesso!");
				} catch (e: Exception) {
					context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + " Não tenho permissão para banir este usuário!");
				}
			} else {
				this.explain(context);
			}
		}
	}
}