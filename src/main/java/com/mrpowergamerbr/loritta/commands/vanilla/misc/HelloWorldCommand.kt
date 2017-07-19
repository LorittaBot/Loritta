package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext

class HelloWorldCommand : CommandBase() {
    override fun getLabel(): String {
        return "helloworld"
    }

    override fun getDescription(): String {
        return "Um comando de teste para testar o sistema de localização da Loritta"
    }

    override fun run(context: CommandContext) {
        context.sendMessage(context.locale.HELLO_WORLD.format("\uD83D\uDE04"))
    }
}