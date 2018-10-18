package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
    override fun getUsage(): String {
        return "<nova mensagem>"
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale["SOBREMIM_DESCRIPTION"]
    }

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        val profile = transaction(Databases.loritta) { context.lorittaUser.profile }
        if (context.args.isNotEmpty()) {
            transaction(Databases.loritta) {
                profile.settings.aboutMe = context.args.joinToString(" ")
            }

            context.sendMessage(context.getAsMention(true) + context.locale["SOBREMIM_CHANGED", profile.settings.aboutMe])
        } else {
            this.explain(context);
        }
    }
}