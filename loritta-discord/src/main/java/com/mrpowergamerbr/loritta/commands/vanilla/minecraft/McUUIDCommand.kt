package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.*

class McUUIDCommand : AbstractCommand("mcuuid", category = CommandCategory.MINECRAFT) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["commands.minecraft.mcuuid.description"]
    }

    // TODO: Fix Usage

    override fun getExamples(): List<String> {
        return Arrays.asList("Monerk")
    }

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        if (context.args.size > 0) {
            val player = context.args[0]

	        val data = HttpRequest.get("https://api.mojang.com/users/profiles/minecraft/$player").body()

	        try {
                val json = JsonParser.parseString(data).asJsonObject

	            context.sendMessage(context.getAsMention(true) + context.locale["commands.minecraft.mcuuid.result", player, LorittaUtils.getUUID(json["id"].string)])
            } catch (e: IllegalStateException) {
                context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["commands.minecraft.mcuuid.invalid", player])
            }
        } else {
            this.explain(context)
        }
    }
}