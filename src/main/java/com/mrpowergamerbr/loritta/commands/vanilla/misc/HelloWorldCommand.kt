package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.save

class HelloWorldCommand : CommandBase() {
    override fun getLabel(): String {
        return "helloworld"
    }

    override fun getDescription(context: CommandContext?): String {
		if (context == null) { return "Teste" }
        return context!!.locale.HELLO_WORLD_DESCRIPTION
    }

    override fun run(context: CommandContext) {
		if (context.userHandle.id == Loritta.config.ownerId && context.args.isNotEmpty()) {
			var newLocale = context.args[0]

			context.config.localeId = newLocale

			loritta save context.config
			return
		}
        context.sendMessage(context.locale.HELLO_WORLD.msgFormat("\uD83D\uDE04"))
    }
}