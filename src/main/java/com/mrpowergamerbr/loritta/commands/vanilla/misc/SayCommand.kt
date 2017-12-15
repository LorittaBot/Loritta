package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

class SayCommand : AbstractCommand("falar", listOf("say")) {
    override fun getDescription(): String {
        return "Faça eu falar uma mensagem!";
    }

    override fun getUsage(): String {
        return "mensagem"
    }

    override fun getExample(): List<String> {
        return Arrays.asList("Eu sou fofa! :3")
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.MISC;
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        if (context.args.size > 0) {
            var message = context.rawArgs.joinToString(" ").escapeMentions()
            context.sendMessage(context.getAsMention(true) + message);
        } else {
            this.explain(context);
        }
    }
}