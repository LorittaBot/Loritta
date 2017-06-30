package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.MessageBuilder
import java.io.File
import java.io.IOException

class AngelCommand : CommandBase() {
	override fun getLabel(): String {
		return "angel"
	}

	override fun getDescription(): String {
		return "Mostra um anjo muito puro para este mundo cruel :^)"
	}

	override fun needsToUploadFiles(): Boolean {
		return true;
	}

	override fun run(context: CommandContext) {
		context.sendFile(File(Loritta.FOLDER + "angel.png"), "angel.png", MessageBuilder().append(" ").build())
	}
}