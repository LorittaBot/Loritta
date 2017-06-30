package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext

class LorittaUnbanCommand : CommandBase() {
	override fun getLabel(): String {
		return "lorittaunban";
	}

	override fun getDescription(): String {
		return "Desbanir usuários de usar a Loritta"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC;
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 1) {
			var monster = context.args[0].toLowerCase(); // ID
			var profile = LorittaLauncher.getInstance().getLorittaProfileForUser(monster);

			profile.isBanned = false;
			profile.banReason = null;

			LorittaLauncher.getInstance().ds.save(profile);

			context.sendMessage(context.getAsMention(true) + "Usuário desbanido com sucesso!")
		} else {
			this.explain(context);
		}
	}
}