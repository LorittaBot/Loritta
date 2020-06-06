package net.perfectdreams.loritta.commands.actions

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.entities.User
import java.awt.Color

class BiteCommand: ActionCommand(labels = arrayOf("morder", "bite")) {

    override fun getResponse(locale: BaseLocale, first: User, second: User): String {
        return if (second.id != LorittaLauncher.loritta.discordConfig.discord.clientId) {
            locale["commands.actions.bite.response", first.asMention, second.asMention]
        } else {
            locale["commands.actions.bite.responseAntiIdiot", second.asMention, first.asMention]
        }
    }

    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.actions.bite.description"]
    }

    override fun getFolderName(): String {
        return "bite"
    }

    override fun getEmoji(): String {
        return "\uD83D\uDD2A"
    }

    override fun getEmbedColor(): Color {
        return Color(217, 99, 50)
    }
}