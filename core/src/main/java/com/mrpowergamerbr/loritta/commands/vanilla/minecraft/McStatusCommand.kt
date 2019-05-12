package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class McStatusCommand : AbstractCommand("mcstatus", category = CommandCategory.MINECRAFT) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["MCSTATUS_DESCRIPTION"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        val body = HttpRequest.get("https://status.mojang.com/check").body()

	    val builder = EmbedBuilder()
                .setTitle("📡 ${locale["MCSTATUS_MOJANG_STATUS"]}", "https://help.mojang.com/")
                .setColor(Color.GREEN)

	    val json = jsonParser.parse(body)
	    for (section in json.array) {
		    val service = section.obj.entrySet().first()
		    val status = service.value.string
		    val prefix = if (service.key.contains("minecraft")) "<:minecraft_logo:412575161041289217> " else "<:mojang:383612358129352704> "
		    val emoji = if (status == "green") "✅" else "❌"
	        builder.addField(prefix + service.key, "${emoji} ${status}", true)
        }

        context.sendMessage(builder.build())
    }
}