package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class InviteCommand : AbstractCommand("convidar", listOf("invite", "convidarbot", "invitebot"), CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["INVITE_DESCRIPTION"]
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        var embed = EmbedBuilder()
                .setDescription(context.locale["INVITE_INFO", "https://discordapp.com/oauth2/authorize?client_id=297153970613387264&scope=bot&permissions=2080374975", "http://loritta.website/auth", "https://discord.gg/V7Kbh4z"])
                .setThumbnail("https://loritta.website/assets/img/loritta_gabizinha_v1.png")
                .setColor(Color(0, 193, 223))
                .build()

        context.sendMessage(embed)
    }
}