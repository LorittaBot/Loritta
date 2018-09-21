package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class McStatusCommand : AbstractCommand("mcstatus", category = CommandCategory.MINECRAFT) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["MCSTATUS_DESCRIPTION"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        var body = HttpRequest.get("https://api.c2g.space/minecraft/status").body();

        var builder = EmbedBuilder()
                .setTitle("📡 ${locale["MCSTATUS_MOJANG_STATUS"]}", "https://help.mojang.com/")
                .setColor(Color.GREEN);

        var json = jsonParser.parse(body);
        for (section in json.asJsonObject.entrySet()) {
            var status = section.value.asJsonObject.get("online").asString;
            var url = section.value.asJsonObject.get("url").asString;
            var prefix = if (url.contains("minecraft")) "<:minecraft_logo:412575161041289217> " else "<:mojang:383612358129352704> "
            var emoji = if (status == "false") "❌" else "✅";
            var endpointStatus = if (status == "false") "Offline" else status;
            builder.addField(prefix + url, "${emoji} (${endpointStatus})", true)
        }

        context.sendMessage(builder.build());
    }
}
