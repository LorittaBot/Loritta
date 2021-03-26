package net.perfectdreams.loritta.commands.vanilla.misc

import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.tables.BotVotes
import org.jetbrains.exposed.sql.select

class DiscordBotListStatusCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("dbl status", "upvote status"), CommandCategory.MISC) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.dblstatus"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {

            val context = this

            val votes = loritta.newSuspendedTransaction {
                BotVotes.select { BotVotes.userId eq context.user.idLong }.count()
            }

            context.reply(
                LorittaReply(
                    locale["$LOCALE_PREFIX.youVoted", votes]
                )
            )
        }
    }
}