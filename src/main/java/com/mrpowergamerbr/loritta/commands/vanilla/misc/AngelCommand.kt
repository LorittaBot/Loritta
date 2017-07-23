package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.io.File

class AngelCommand : CommandBase() {
	override fun getLabel(): String {
		return "angel"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.ANGEL_DESCRIPTION.msgFormat()
	}

	override fun needsToUploadFiles(): Boolean {
		return true;
	}

	override fun run(context: CommandContext) {
		context.sendFile(File(Loritta.FOLDER + "angel.png"), "angel.png", context.getAsMention(true))
	}
}