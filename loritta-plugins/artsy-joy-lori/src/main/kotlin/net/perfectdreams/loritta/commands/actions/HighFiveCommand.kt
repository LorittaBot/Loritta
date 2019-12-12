package net.perfectdreams.loritta.commands.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.entities.User
import java.awt.Color

class HighFiveCommand : ActionCommand(arrayOf("highfive", "hifive", "tocaaqui")) {
    override fun getEmbedColor(): Color {
        return Color(27, 224, 96)
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.actions.highfive.description"]
    }

    override fun getResponse(locale: BaseLocale, first: User, second: User): String {
        return locale["commands.actions.highfive.response", first.asMention, second.asMention]
    }

    override fun getFolderName(): String {
        return "highfive"
    }

    override fun getEmoji(): String {
        return "\uD83D\uDD90"
    }
}