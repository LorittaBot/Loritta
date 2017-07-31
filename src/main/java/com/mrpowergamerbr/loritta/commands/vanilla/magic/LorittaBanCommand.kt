package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.time.Instant
import java.util.ArrayList

class LorittaBanCommand : CommandBase() {
	override fun getLabel(): String {
		return "lorittaban";
	}

	override fun getDescription(): String {
		return "Banir usuários de usar a Loritta"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC;
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.args.size >= 2) {
			var monster = context.args[0].toLowerCase(); // ID
			context.args[0] = "";
			var reason = context.args.joinToString(" ");
			var profile = LorittaLauncher.getInstance().getLorittaProfileForUser(monster);

			profile.isBanned = true;
			profile.banReason = reason;

			LorittaLauncher.getInstance().ds.save(profile);

			context.sendMessage(context.getAsMention(true) + "Usuário banido com sucesso!")
		} else {
			this.explain(context);
		}
	}
}