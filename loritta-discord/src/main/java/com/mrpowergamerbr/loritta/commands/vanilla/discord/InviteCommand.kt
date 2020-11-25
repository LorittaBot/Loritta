package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory

class InviteCommand : AbstractCommand("invite", listOf("convidar", "convidarbot", "invitebot", "convite"), CommandCategory.DISCORD) {
    override fun getDescription(locale: LegacyBaseLocale): String {
        return locale.toNewLocale()["commands.discord.invite.info"]
    }

    override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
        var embed = EmbedBuilder()
                .setDescription(context.locale["commands.discord.invite.inviteInfo", loritta.discordInstanceConfig.discord.addBotUrl, "${loritta.instanceConfig.loritta.website.url}dashboard", "${loritta.instanceConfig.loritta.website.url}support"])
                .setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
                .setColor(Constants.LORITTA_AQUA)
                .build()

        context.sendMessage(embed)
    }
}