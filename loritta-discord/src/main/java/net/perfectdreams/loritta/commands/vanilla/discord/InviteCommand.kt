package net.perfectdreams.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class InviteCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("invite", "convidar", "convidarbot", "invitebot", "convite"), CommandCategory.DISCORD) {
    companion object {
        private const val LOCALE_PREFIX = "commands.discord.invite"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.info")

        executesDiscord {
            val context = this

            val embed = EmbedBuilder()
                    .setDescription(context.locale.getList("$LOCALE_PREFIX.inviteInfo", com.mrpowergamerbr.loritta.utils.loritta.discordInstanceConfig.discord.addBotUrl, "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}dashboard", "${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}support").joinToString("\n"))
                    .setThumbnail("${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
                    .setColor(Constants.LORITTA_AQUA)
                    .build()

            context.sendMessage(embed)
        }
    }
}