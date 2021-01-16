package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.social.aboutme.description"]
    }

    override fun getUsage() = arguments {
        argument(ArgumentType.TEXT) {}
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val settings = loritta.newSuspendedTransaction { context.lorittaUser.profile.settings }
        if (context.args.isNotEmpty()) {
            loritta.newSuspendedTransaction {
	            settings.aboutMe = context.args.joinToString(" ")
            }

            context.sendMessage(context.getAsMention(true) + context.locale["commands.social.aboutme.changed", settings.aboutMe])
        } else {
            this.explain(context)
        }
    }
}