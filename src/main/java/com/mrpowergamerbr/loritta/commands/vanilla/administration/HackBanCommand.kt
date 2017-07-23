package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission

class HackBanCommand : CommandBase() {
	override fun getLabel(): String {
		return "hackban"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.HACKBAN_DESCRIPTION.msgFormat()
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório");
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

				var reason: String? = null;
				if (context.args.size > 1) {
					reason = context.args.toList().subList(1, context.args.size).joinToString(separator = " ");
				}
				context.guild.controller.ban(id, 0, context.locale.HACKBAN_REASON.msgFormat(context.userHandle.name + "#" + context.userHandle.discriminator) + if (reason != null) " (${context.locale.HACKBAN_REASON.msgFormat()}: " + reason + ")" else "").complete();

				context.sendMessage(context.getAsMention(true) + context.locale.HACKBAN_SUCCESS.msgFormat(id));
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.HACKBAN_NO_PERM.msgFormat());
			}
		} else {
			this.explain(context);
		}
	}
}