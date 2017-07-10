package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData

class RankCommand : CommandBase() {
	override fun getLabel():String {
		return "rank";
	}

	override fun getDescription(): String {
		return "Veja o ranking do servidor atual!";
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.SOCIAL;
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		val list = mutableListOf<RankWrapper>()
		context.config.userData
				.forEach { list.add(RankWrapper(it.key, it.value)) }

		list.sortBy { it.userData.xp }
		list.reverse()

		var text = "```cs\n\uD83C\uDFC6 Rank | \uD83D\uDCDB Nome\n";

		var idx = 0;

		var currentIndex = 0;
		var userData: LorittaServerUserData? = null;

		for (entry in list) {
			if (entry.id == context.userHandle.id) {
				userData = entry.userData;
				break;
			}
			currentIndex++;
		}

		for (entry in list) {
			if (idx >= 10) {
				break;
			}
			var member = context.guild.getMemberById(entry.id)

			text += "\n[${idx + 1}]   " + (if (idx != 9) " " else "") + " > #";
			if (member != null) {
				text += member.effectiveName
				if (userData == entry.userData) {
					text += " (\uD83D\uDC48 Sua posição no ranking!)"
				}
			} else {
				text += "Usuário saiu do servidor... 😢"
			}
			text += "\n            \uD83C\uDF1F XP total: " + entry.userData.xp + " | \uD83D\uDCAB Nível atual: " + entry.userData.getCurrentLevel().currentLevel
			idx++;
		}

		var emoji = "\uD83D\uDE42";

		if (currentIndex >= 10) {
			emoji = "\uD83D\uDE10";
		}

		text += "\n____________________________\n\n";
		text += "$emoji Rank: ${currentIndex + 1} - \uD83C\uDF1F Seu XP: ${userData!!.xp}"
		text += "```"
		context.sendMessage(text)
	}

	data class RankWrapper(
			val id: String,
			val userData: LorittaServerUserData)
}