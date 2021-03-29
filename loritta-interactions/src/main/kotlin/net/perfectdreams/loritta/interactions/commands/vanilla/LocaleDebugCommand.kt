package net.perfectdreams.loritta.interactions.commands.vanilla

import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.required
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.utils.locale.LocaleManager

class LocaleDebugCommand(val m: LorittaInteractions) : SlashCommand(this) {
    companion object : SlashCommandDeclaration(
        name = "localedebug",
        description = "Shows locale keys"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val localeKey = string("locale_key", "The key of the locale")
                .required()
                .register()
        }
    }

    override suspend fun executes(context: SlashCommandContext) {
        val localeKey = options.localeKey.get(context)

        context.sendMessage {
            content = "`$localeKey`: ${m.localeManager.getLocaleById(LocaleManager.DEFAULT_LOCALE_ID)[localeKey]}"
        }
    }
}