package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.MessageCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.messageCommand
import java.util.*

class TransformMentionsIntoIDsCommand(val loritta: LorittaBot) : MessageCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Transformmentionsintoids
    }

    override fun command() = messageCommand(I18N_PREFIX.Label, CommandCategory.DISCORD, UUID.fromString("2eb2a721-7328-41f2-a87b-2340431c3cb1"), TransformMentionsIntoIDsExecutor()) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    inner class TransformMentionsIntoIDsExecutor : LorittaMessageCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, message: Message) {
            val mentions = message.mentions.getMentions(MentionType.USER)

            if (mentions.isNotEmpty()) {
                context.reply(true) {
                    // We don't want ANY formatting
                    content = mentions.joinToString(" ") { it.id }
                }
            } else {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Transformmentionsintoids.NoUserMentionsInTheMessage),
                        Emotes.LoriSob
                    )
                }
            }
        }
    }
}