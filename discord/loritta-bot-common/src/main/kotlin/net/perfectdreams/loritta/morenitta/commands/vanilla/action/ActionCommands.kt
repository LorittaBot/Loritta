package net.perfectdreams.loritta.morenitta.commands.vanilla.action

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay.*
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordUser
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.awt.Color

typealias ActionCommandScope = ActionCommandDSL.() -> Unit
typealias ActionCommandContext = (BaseLocale, User, User) -> String

abstract class ActionCommand(val attributes: RoleplayActionAttributes, loritta: LorittaBot, labels: List<String>): DiscordAbstractCommandBase(loritta, labels, CommandCategory.ROLEPLAY) {
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

                val receiver = (userOrFail(0) as JDAUser).handle

                OutdatedCommandUtils.sendOutdatedCommandMessage(this, this.locale, "roleplay ${labels.first()}")

                RoleplayCommand.executeCompat(
                    CommandContextCompat.LegacyDiscordCommandContextCompat(this),
                    attributes,
                    receiver
                )
            }
        }
    }
}

class ActionCommandDSL(val command: ActionCommand) {
    var description: String = "commands.command.${command.labels.first()}.description"

    lateinit var color: Color
    lateinit var emoji: String
    lateinit var response: ActionCommandContext

    fun response(block: ActionCommandContext) {
        this.response = block
    }
}

fun ActionCommand.action(block: ActionCommandScope) =
    ActionCommandDSL(this).apply(block)


