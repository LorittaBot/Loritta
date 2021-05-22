package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.awt.Color

class McStatusCommand : AbstractCommand("mcstatus", category = CommandCategory.MINECRAFT) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.mcstatus.description")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        val body = HttpRequest.get("https://status.mojang.com/check").body()

        val builder = EmbedBuilder()
                .setTitle("📡 ${locale["commands.command.mcstatus.mojangStatus"]}", "https://help.mojang.com/")
                .setColor(Color.GREEN)

        val json = JsonParser.parseString(body)
        for (section in json.array) {
            val service = section.obj.entrySet().first()
            val status = service.value.string
            val prefix = if (service.key.contains("minecraft")) "<:minecraft_logo:412575161041289217> " else "<:mojang:383612358129352704> "
            val emoji = if (status == "green") "✅" else "❌"
            builder.addField(prefix + service.key, "$emoji $status", true)
        }

        context.sendMessage(builder.build())
    }
}