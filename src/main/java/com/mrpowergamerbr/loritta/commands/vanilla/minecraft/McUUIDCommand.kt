package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.util.*

class McUUIDCommand : AbstractCommand("mcuuid", category = CommandCategory.MINECRAFT) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["MCUUID_DESCRIPTION"]
    }

    override fun getUsage(): String {
        return "nickname"
    }

    override fun getExamples(): List<String> {
        return Arrays.asList("Monerk")
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        if (context.args.size > 0) {
            val player = context.args[0]

	        val data = HttpRequest.get("https://api.mojang.com/users/profiles/minecraft/$player").body()

	        try {
                val json = jsonParser.parse(data).asJsonObject

	            context.sendMessage(context.getAsMention(true) + context.legacyLocale["MCUUID_RESULT", player, LorittaUtils.getUUID(json["id"].string)])
            } catch (e: IllegalStateException) {
                context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["MCUUID_INVALID", player])
            }
        } else {
            this.explain(context)
        }
    }
}