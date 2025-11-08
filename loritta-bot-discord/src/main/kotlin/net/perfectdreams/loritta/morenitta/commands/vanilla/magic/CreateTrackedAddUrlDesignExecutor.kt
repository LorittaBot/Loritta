package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AddBotURL
import net.perfectdreams.loritta.morenitta.websitedashboard.AuthenticationState
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.AuthenticationStateUtils

object CreateTrackedAddUrlDesignExecutor : LoriToolsCommand.LoriToolsExecutor {
    override val args = "create_tracked_add_url <source> <medium> <campaign> <content>"

    override fun executes(): suspend CommandContext.() -> Boolean = task@{
        if (args.getOrNull(0) != "create_tracked_add_url")
            return@task false
        val context = checkType<DiscordCommandContext>(this)

        val url = LorittaDiscordOAuth2AddBotURL(
            loritta,
            null,
            AuthenticationStateUtils.createStateAsBase64(
                AuthenticationState(
                    source = args[1].let { if (it == "null") null else it },
                    medium = args[2].let { if (it == "null") null else it },
                    campaign = args[3].let { if (it == "null") null else it },
                    content = args[4].let { if (it == "null") null else it }
                ),
                loritta
            )
        )

        context.reply(
            LorittaReply(
                url.toString()
            )
        )
        return@task true
    }
}