package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import org.jetbrains.exposed.sql.transactions.transaction

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
    override fun getUsage(): String {
        return "<nova mensagem>"
    }

    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["SOBREMIM_DESCRIPTION"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        val settings = transaction(Databases.loritta) { context.lorittaUser.profile.settings }
        if (context.args.isNotEmpty()) {
            transaction(Databases.loritta) {
	            settings.aboutMe = context.args.joinToString(" ")
            }

            context.sendMessage(context.getAsMention(true) + context.legacyLocale["SOBREMIM_CHANGED", settings.aboutMe])
        } else {
            this.explain(context)
        }
    }
}