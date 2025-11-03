package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL

class InviteCommand(loritta: LorittaBot) : AbstractCommand(loritta, "invite", listOf("convidar", "convidarbot", "invitebot", "convite"), net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.invite.info")

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        val embed = EmbedBuilder()
                .setDescription(
                    context.locale.getList(
                        "commands.command.invite.inviteInfo",
                        LorittaDiscordOAuth2AddBotURL(loritta).toString(),
                        loritta.config.loritta.dashboard.url,
                        "${loritta.config.loritta.website.url}support").joinToString("\n")
                )
                .setThumbnail("${loritta.config.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
                .setColor(Constants.LORITTA_AQUA)
                .build()

        context.sendMessageEmbeds(embed)
    }
}