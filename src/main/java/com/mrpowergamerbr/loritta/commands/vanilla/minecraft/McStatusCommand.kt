package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class McStatusCommand : CommandBase("mcstatus") {
    override fun getDescription(locale: BaseLocale): String {
        return locale.MCSTATUS_DESCRIPTION.msgFormat();
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.MINECRAFT;
    }

    override fun run(context: CommandContext) {
        var body = HttpRequest.get("https://mcapi.ca/mcstatus").body();

        var builder = EmbedBuilder()
                .setTitle("📡 ${context.locale.MCSTATUS_MOJANG_STATUS.msgFormat()}", "https://help.mojang.com/")
                .setColor(Color.GREEN);

        var json = JSON_PARSER.parse(body);
        for (section in json.asJsonObject.entrySet()) {
            var status = section.value.asJsonObject.get("status").asString;
            var prefix = if (section.key.contains("minecraft")) "<:grass:383612358318227457> " else "<:mojang:383612358129352704>";
            var emoji = if (status == "Online") "✅" else "❌";
            builder.addField(prefix + section.key, "${emoji} ${status}", true)
        }

        context.sendMessage(builder.build());
    }
}