package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class BomDiaECiaStatusCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("bomdiaecia status", "bd&c status", "bdc status"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.bomdiaeciastatus"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val user = user(0) ?: this.message.author

            val votes = loritta.newSuspendedTransaction {
                BomDiaECiaWinners.selectAll().where { BomDiaECiaWinners.userId eq user.id }.count()
            }

            if (user == this.message.author) {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.youWins", votes]
                    )
                )
            } else {
                reply(
                    LorittaReply(
                        locale["$LOCALE_PREFIX.userWins", user.asMention, votes]
                    )
                )
            }
        }
    }
}