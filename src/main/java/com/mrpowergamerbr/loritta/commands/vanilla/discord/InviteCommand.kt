package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.EmbedBuilder

class InviteCommand : AbstractCommand("invite", listOf("convidar", "convidarbot", "invitebot"), CommandCategory.DISCORD) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale["INVITE_DESCRIPTION"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        var embed = EmbedBuilder()
                .setDescription(context.legacyLocale["INVITE_INFO", Loritta.config.addBotUrl, "${Loritta.config.websiteUrl}dashboard", "${Loritta.config.websiteUrl}support"])
                .setThumbnail("${Loritta.config.websiteUrl}assets/img/loritta_gabizinha_v1.png")
                .setColor(Constants.LORITTA_AQUA)
                .build()

        context.sendMessage(embed)
    }
}