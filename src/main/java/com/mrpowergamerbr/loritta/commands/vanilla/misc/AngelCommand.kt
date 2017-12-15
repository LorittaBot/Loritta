package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.io.File

class AngelCommand : AbstractCommand("angel") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.ANGEL_DESCRIPTION.msgFormat()
	}

	override fun needsToUploadFiles(): Boolean {
		return true;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		context.sendFile(File(Loritta.ASSETS + "angel.png"), "angel.png", context.getAsMention(true))
	}
}