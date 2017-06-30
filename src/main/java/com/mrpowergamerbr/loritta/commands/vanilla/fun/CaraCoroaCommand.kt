package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext

class CaraCoroaCommand : CommandBase() {
	override fun getLabel(): String {
		return "girarmoeda"
	}

	override fun getDescription(): String {
		return "Gire uma moeda e veja se irá cair cara ou coroa! Perfeito para descobrir quem irá ir primeiro em uma partida de futebas"
	}

	override fun run(context: CommandContext) {
		context.sendMessage(context.getAsMention(true) + if (Loritta.random.nextBoolean()) "\uD83D\uDE46\u200D **Cara!**" else "\uD83D\uDC51 **Coroa!**")
	}
}
