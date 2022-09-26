package net.perfectdreams.loritta.legacy.commands.vanilla.discord

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory

class InviteCommand : AbstractCommand("invite", listOf("convidar", "convidarbot", "invitebot", "convite"), CommandCategory.DISCORD) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.invite.info")

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        var embed = EmbedBuilder()
                .setDescription(context.locale.getList("commands.command.invite.inviteInfo", loritta.discordInstanceConfig.discord.addBotUrl, "${loritta.instanceConfig.loritta.website.url}dashboard", "${loritta.instanceConfig.loritta.website.url}support").joinToString("\n"))
                .setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
                .setColor(Constants.LORITTA_AQUA)
                .build()

        context.sendMessage(embed)
    }
}