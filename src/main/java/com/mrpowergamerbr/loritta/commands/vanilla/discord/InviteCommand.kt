package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class InviteCommand : CommandBase() {
    override fun getLabel(): String {
        return "convidar";
    }

    override fun getAliases(): MutableList<String> {
        return Arrays.asList("invite");
    }

    override fun getDescription(locale: BaseLocale): String {
        return locale.INVITE_DESCRIPTION.msgFormat()
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.SOCIAL;
    }

    override fun run(context: CommandContext) {
        var embed = EmbedBuilder()
                .setDescription(context.locale.INVITE_INFO.msgFormat("https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975", "http://loritta.website/auth", "https://discord.gg/3rXgN8x"))
                .setThumbnail("http://loritta.website/assets/img/loritta_guild_v4.png")
                .setColor(Color(0, 193, 223))
                .build()

        context.sendMessage(embed)
    }
}