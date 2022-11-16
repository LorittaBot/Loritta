package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.locale.Gender
import net.perfectdreams.loritta.morenitta.utils.onReactionAdd
import net.perfectdreams.loritta.morenitta.utils.removeAllFunctions
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File

typealias ActionCommandScope = ActionCommandDSL.() -> Unit
typealias ActionCommandContext = (BaseLocale, User, User) -> String

abstract class ActionCommand(loritta: LorittaBot, labels: List<String>): DiscordAbstractCommandBase(loritta, labels, CommandCategory.ROLEPLAY) {

    abstract fun create(): ActionCommandDSL

    override fun command(): Command<CommandContext> = create {
        create().also {
            localizedDescription(it.description)
            localizedExamples("commands.category.action.examples")

            usage {
                argument(ArgumentType.USER) {
                    optional = false
                }
            }

            executesDiscord {
                if (args.isEmpty()) return@executesDiscord explain()

                OutdatedCommandUtils.sendOutdatedCommandMessage(this, this.locale, "roleplay ${labels.first()}")
            }
        }
    }
}

class ActionCommandDSL(val command: ActionCommand) {

    var description: String = "commands.command.${command.labels.first()}.description"
    var folderName: String? = command.labels.first()

    lateinit var color: Color
    lateinit var emoji: String
    lateinit var response: ActionCommandContext

    fun response(block: ActionCommandContext) {
        this.response = block
    }

}

fun ActionCommand.action(block: ActionCommandScope) =
    ActionCommandDSL(this).apply(block)


