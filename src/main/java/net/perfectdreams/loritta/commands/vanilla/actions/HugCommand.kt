package net.perfectdreams.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class HugCommand : ActionCommand(arrayOf("hug", "abraço", "abraçar", "abraco", "abracar")) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.actions.hug.description"]
    }

    override fun getResponse(locale: BaseLocale, first: User, second: User): String {
        return locale["commands.actions.hug.response", first.asMention, second.asMention]
    }

    override fun getFolderName(): String {
        return "hug"
    }

    override fun getEmoji(): String {
        return "\uD83D\uDC99"
    }
}