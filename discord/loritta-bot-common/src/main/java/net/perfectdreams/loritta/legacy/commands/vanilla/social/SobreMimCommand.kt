package net.perfectdreams.loritta.legacy.commands.vanilla.social

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class SobreMimCommand : AbstractCommand("aboutme", listOf("sobremim"), CommandCategory.SOCIAL) {
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