package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import org.jetbrains.exposed.dao.id.EntityID

object SetSelfBackgroundExecutor : LoriToolsCommand.LoriToolsExecutor {
    override val args = "set self_background <internalType>"

    override fun executes(): suspend CommandContext.() -> Boolean = task@{
        if (args.getOrNull(0) != "set")
            return@task false
        if (args.getOrNull(1) != "self_background")
            return@task false

        val context = checkType<DiscordCommandContext>(this)
        loritta.pudding.transaction {
            context.lorittaUser.profile.settings.activeBackgroundInternalName = EntityID(args[2], Backgrounds)
        }

        context.reply(
            LorittaReply(
                "Background alterado!"
            )
        )
        return@task true
    }
}