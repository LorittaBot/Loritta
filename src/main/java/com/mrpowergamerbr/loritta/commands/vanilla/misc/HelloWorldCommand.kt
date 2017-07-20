package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.save

class HelloWorldCommand : CommandBase() {
    override fun getLabel(): String {
        return "helloworld"
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.HELLO_WORLD_DESCRIPTION
    }

    override fun run(context: CommandContext) {
		if (context.userHandle.id == Loritta.config.ownerId && context.args.isNotEmpty()) {
			var newLocale = context.args[0]

			context.config.localeId = newLocale

			loritta save context.config

			context.sendMessage(context.locale.USING_LOCALE.msgFormat(context.config.localeId))
			return
		}
        context.sendMessage(context.locale.HELLO_WORLD.msgFormat("\uD83D\uDE04"))
    }
}