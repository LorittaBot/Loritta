package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save

class LorittaUnbanCommand : AbstractCommand("lorittaunban") {
	override fun getDescription(): String {
		return "Desbanir usuários de usar a Loritta"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC;
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size >= 1) {
			var monster = context.args[0].toLowerCase(); // ID
			var profile = LorittaLauncher.getInstance().getLorittaProfileForUser(monster);

			profile.isBanned = false;
			profile.banReason = null;

			loritta save profile

			context.sendMessage(context.getAsMention(true) + "Usuário desbanido com sucesso!")
		} else {
			this.explain(context);
		}
	}
}