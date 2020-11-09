package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
    override fun getUsage(): String {
        return "<nova mensagem>"
    }

    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["SOBREMIM_DESCRIPTION"]
    }

    override fun getUsage(locale: LegacyBaseLocale) = arguments {
        argument(ArgumentType.TEXT) {}
    }

    override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
        val settings = loritta.newSuspendedTransaction { context.lorittaUser.profile.settings }
        if (context.args.isNotEmpty()) {
            loritta.newSuspendedTransaction {
	            settings.aboutMe = context.args.joinToString(" ")
            }

            context.sendMessage(context.getAsMention(true) + context.legacyLocale["SOBREMIM_CHANGED", settings.aboutMe])
        } else {
            this.explain(context)
        }
    }
}