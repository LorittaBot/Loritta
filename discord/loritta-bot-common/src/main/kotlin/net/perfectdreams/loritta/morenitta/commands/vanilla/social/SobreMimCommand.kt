package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.loritta
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.aboutme.description")
    override fun getExamplesKey() = LocaleKeyData("commands.command.aboutme.examples")

    override fun getUsage() = arguments {
        argument(ArgumentType.TEXT) {}
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "profile aboutme")

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