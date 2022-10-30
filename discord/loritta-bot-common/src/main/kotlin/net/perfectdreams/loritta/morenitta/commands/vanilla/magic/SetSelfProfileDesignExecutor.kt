package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext

object SetSelfProfileDesignExecutor : LoriToolsCommand.LoriToolsExecutor {
    override val args = "set self_profile_design <internalType>"

    override fun executes(): suspend CommandContext.() -> Boolean = task@{
        if (args.getOrNull(0) != "set")
            return@task false
        if (args.getOrNull(1) != "self_profile_design")
            return@task false

        val context = checkType<DiscordCommandContext>(this)
        loritta.pudding.transaction {
            context.lorittaUser.profile.settings.activeProfileDesign = ProfileDesign.findById(args[2])
        }

        context.reply(
            LorittaReply(
                "Profile Design alterado!"
            )
        )
        return@task true
    }
}