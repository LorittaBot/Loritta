package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import org.jetbrains.exposed.sql.selectAll

class DiscordBotListStatusCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("dbl status", "upvote status"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.dblstatus"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val user = user(0) ?: this.message.author

            val votes = loritta.newSuspendedTransaction {
                BotVotes.selectAll().where { BotVotes.userId eq user.id }.count()
            }

            if (user == this.message.author) {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.youVoted", votes]
                    )
                )
            } else {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.userVoted", user.asMention, votes]
                    )
                )
            }
        }
    }
}