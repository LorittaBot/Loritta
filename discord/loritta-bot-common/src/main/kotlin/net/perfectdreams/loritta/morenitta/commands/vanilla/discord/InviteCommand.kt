package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.morenitta.LorittaBot

class InviteCommand(loritta: LorittaBot) : AbstractCommand(loritta, "invite", listOf("convidar", "convidarbot", "invitebot", "convite"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.invite.info")

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        var embed = EmbedBuilder()
                .setDescription(context.locale.getList("commands.command.invite.inviteInfo", loritta.config.loritta.discord.addBotUrl, "${loritta.config.loritta.website.url}dashboard", "${loritta.config.loritta.website.url}support").joinToString("\n"))
                .setThumbnail("${loritta.config.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
                .setColor(Constants.LORITTA_AQUA)
                .build()

        context.sendMessage(embed)
    }
}