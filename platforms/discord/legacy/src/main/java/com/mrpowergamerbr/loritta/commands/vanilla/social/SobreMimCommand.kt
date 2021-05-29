package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.aboutme.description")
    override fun getExamplesKey() = LocaleKeyData("commands.command.aboutme.examples")

    override fun getUsage() = arguments {
        argument(ArgumentType.TEXT) {}
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val settings = loritta.newSuspendedTransaction { context.lorittaUser.profile.settings }
        if (context.args.isNotEmpty()) {
            loritta.newSuspendedTransaction {
	            settings.aboutMe = context.args.joinToString(" ")
            }

            context.sendMessage(context.getAsMention(true) + context.locale["commands.command.aboutme.changed", settings.aboutMe])
        } else {
            this.explain(context)
        }
    }
}